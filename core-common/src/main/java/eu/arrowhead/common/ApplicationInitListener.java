/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.core.CoreSystemService.InterfaceData;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;

public abstract class ApplicationInitListener {
	
	//=================================================================================================
	// members

	protected final Logger logger = LogManager.getLogger(ApplicationInitListener.class);
	
	private static final int MAX_NUMBER_OF_SERVICEREGISTRY_CONNECTION_RETRIES = 3;
	private static final int WAITING_PERIOD_BETWEEN_RETRIES_IN_SECONDS = 15;

	@Autowired
	protected SSLProperties sslProperties;
	
	@Autowired
	protected CoreSystemRegistrationProperties coreSystemRegistrationProperties;
	
	@Autowired
	protected HttpService httpService;
	
	@Autowired
	protected ApplicationContext applicationContext;
	
	protected PublicKey publicKey;
	
	protected boolean standaloneMode = false;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	@Order(10)
	public void onApplicationEvent(final ContextRefreshedEvent event) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InterruptedException {
		logger.debug("Initialization in onApplicationEvent()...");
		
		final CoreSystem coreSystem = coreSystemRegistrationProperties.getCoreSystem();
		logger.info("Core system name: {}", coreSystem.name());
		logger.info("Server mode: {}", getModeString());
		
		if (sslProperties.isSslEnabled()) {
			final KeyStore keyStore = initializeKeyStore();
			checkServerCertificate(keyStore, event.getApplicationContext());
			obtainKeys(keyStore, event.getApplicationContext());
		}
		
		registerCoreSystemServicesToServiceRegistry(event.getApplicationContext());
		initRequiredCoreSystemServiceUris(event.getApplicationContext());
		customInit(event);
		
		logger.debug("Initialization in onApplicationEvent() is done.");
	}

	//-------------------------------------------------------------------------------------------------
	@PreDestroy
	public void destroy() throws InterruptedException {
		logger.debug("destroy called...");
		
		final CoreSystem coreSystem = coreSystemRegistrationProperties.getCoreSystem();
		if (skipSROperations(coreSystem)) {
			return;
		}

		try {
			final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
			checkServiceRegistryConnection(scheme, 0, 1);
			
			int count = coreSystem.getServices().size();
			for (final CoreSystemService coreService : coreSystem.getServices()) {
				final UriComponents unregisterUri = createUnregisterUri(scheme, coreService, coreSystemRegistrationProperties.getCoreSystemDomainName(), coreSystemRegistrationProperties.getCoreSystemDomainPort());
				try {
					httpService.sendRequest(unregisterUri, HttpMethod.DELETE, Void.class);
				} catch (final InvalidParameterException ex) {
					// core service not found
					count--;
				}
			}
			
			logger.info("Core system {} revoked {} service(s).", coreSystem.name(), count);
		} catch (final Throwable t) {
			logger.error(t.getMessage());
			logger.debug(t);
		}
		
		try {
			customDestroy();
		} catch (final Throwable t) {
			logger.error(t.getMessage());
			logger.debug(t);
		}
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	protected void customInit(final ContextRefreshedEvent event) {}
	
	//-------------------------------------------------------------------------------------------------
	protected void customDestroy() {}
	
	//-------------------------------------------------------------------------------------------------
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of();
	}
	
	//-------------------------------------------------------------------------------------------------
	protected String getModeString() {
		return sslProperties.isSslEnabled() ? "SECURED" : "NOT SECURED";
	}
	
	//-------------------------------------------------------------------------------------------------
	private KeyStore initializeKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		logger.debug("initializeKeyStore started...");
		Assert.isTrue(sslProperties.isSslEnabled(), "SSL is not enabled.");
		final String messageNotDefined = " is not defined.";
		Assert.isTrue(!Utilities.isEmpty(sslProperties.getKeyStoreType()), CommonConstants.KEYSTORE_TYPE + messageNotDefined);
		Assert.notNull(sslProperties.getKeyStore(), CommonConstants.KEYSTORE_PATH + messageNotDefined);
		Assert.isTrue(sslProperties.getKeyStore().exists(), CommonConstants.KEYSTORE_PATH + " file is not found.");
		Assert.notNull(sslProperties.getKeyStorePassword(), CommonConstants.KEYSTORE_PASSWORD + messageNotDefined);
		
		final KeyStore keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
		keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());

		return keystore;
	}

	//-------------------------------------------------------------------------------------------------
	private void checkServerCertificate(final KeyStore keyStore, final ApplicationContext appContext) {
		logger.debug("checkServerCertificate started...");
		final X509Certificate serverCertificate = Utilities.getSystemCertFromKeyStore(keyStore);
		final String serverCN = Utilities.getCertCNFromSubject(serverCertificate.getSubjectDN().getName());
		if (!Utilities.isKeyStoreCNArrowheadValid(serverCN)) {
			logger.info("Server CN ({}) is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".", serverCN);
			throw new AuthException("Server CN (" + serverCN + ") is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".");
		}
		logger.info("Server CN: {}", serverCN);
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		context.put(CommonConstants.SERVER_COMMON_NAME, serverCN);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void obtainKeys(final KeyStore keyStore, final ApplicationContext appContext) {
		logger.debug("obtainKeys started...");
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);

		final X509Certificate serverCertificate = Utilities.getSystemCertFromKeyStore(keyStore);
		publicKey = serverCertificate.getPublicKey();
		context.put(CommonConstants.SERVER_PUBLIC_KEY, publicKey);

		final PrivateKey privateKey = Utilities.getPrivateKey(keyStore, sslProperties.getKeyPassword());
		context.put(CommonConstants.SERVER_PRIVATE_KEY, privateKey);

		context.put(CommonConstants.SERVER_CERTIFICATE, serverCertificate);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void registerCoreSystemServicesToServiceRegistry(final ApplicationContext appContext) throws InterruptedException {
		logger.debug("registerCoreSystemServicesToServiceRegistry started...");
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		standaloneMode = context.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		
		final CoreSystem coreSystem = coreSystemRegistrationProperties.getCoreSystem();
		if (skipSROperations(coreSystem)) {
			return;
		}
		
		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
		checkServiceRegistryConnection(scheme, MAX_NUMBER_OF_SERVICEREGISTRY_CONNECTION_RETRIES, WAITING_PERIOD_BETWEEN_RETRIES_IN_SECONDS);
		
		final UriComponents queryUri = createQueryUri(scheme);
		final UriComponents registerUri = createRegisterUri(scheme);
		final SystemRequestDTO coreSystemDTO = getCoreSystemRequestDTO();

		for (final CoreSystemService coreService : coreSystem.getServices()) {
			final ServiceQueryFormDTO queryForm = new ServiceQueryFormDTO.Builder(coreService.getServiceDefinition()).build();
			final ResponseEntity<ServiceQueryResultDTO> queryResponse = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, queryForm);
			for (final ServiceRegistryResponseDTO result : queryResponse.getBody().getServiceQueryData()) { // old, possibly obsolete entries
				final UriComponents unregisterUri = createUnregisterUri(scheme, coreService, result.getProvider().getAddress(), result.getProvider().getPort());
				httpService.sendRequest(unregisterUri, HttpMethod.DELETE, Void.class);
			}
			
			final ServiceRegistryRequestDTO request = getCoreSystemServiceRegistryRequestDTO(coreSystemDTO, coreService);
			httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
		}
		
		logger.info("Core system {} published {} service(s).", coreSystem.name(), coreSystem.getServices().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean skipSROperations(final CoreSystem coreSystem) {
		return standaloneMode || CoreSystem.SERVICEREGISTRY == coreSystem; 
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryConnection(final String scheme, final int retries, final int period) throws InterruptedException {
		logger.debug("checkServiceRegistryConnection started...");
	
		final UriComponents echoUri = createEchoUri(scheme);
		for (int i = 0; i <= retries; ++i) {
			try {
				httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
				logger.info("Service Registry is accessible...");
				break;
			} catch (final AuthException ex) {
				throw ex;
			} catch (final ArrowheadException ex) {
				if (i >= retries) {
					throw ex;
				} else {
					logger.info("Service Registry is unavailable at the moment, retrying in {} seconds...", period);
					Thread.sleep(period * CoreCommonConstants.CONVERSION_MILLISECOND_TO_SECOND);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getCoreSystemRequestDTO() {
		logger.debug("getCoreSystemRequestDTO started...");
		
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName(coreSystemRegistrationProperties.getCoreSystem().name().toLowerCase());
		result.setAddress(coreSystemRegistrationProperties.getCoreSystemDomainName());
		result.setPort(coreSystemRegistrationProperties.getCoreSystemDomainPort());
		
		if (sslProperties.isSslEnabled()) {
			result.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceRegistryRequestDTO getCoreSystemServiceRegistryRequestDTO(final SystemRequestDTO coreSystemDTO, final CoreSystemService coreSystemService) {
		logger.debug("getCoreSystemServiceRegistryRequestDTO started...");
		
		final List<String> interfaces = collectInterfaces(coreSystemService);
		final ServiceRegistryRequestDTO result = new ServiceRegistryRequestDTO();
		result.setProviderSystem(coreSystemDTO);
		result.setServiceDefinition(coreSystemService.getServiceDefinition());
		result.setServiceUri(coreSystemService.getServiceUri());
		result.setSecure(sslProperties.isSslEnabled() ? ServiceSecurityType.CERTIFICATE.name() : ServiceSecurityType.NOT_SECURE.name());
		result.setInterfaces(interfaces);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<String> collectInterfaces(final CoreSystemService service) {
		logger.debug("collectInterfaces started...");
		
		final List<InterfaceData> interfaces = service.getInterfaces();
		if (interfaces == null || interfaces.isEmpty()) {
			return sslProperties.isSslEnabled() ? List.of(CommonConstants.HTTP_SECURE_JSON) : List.of(CommonConstants.HTTP_INSECURE_JSON);
		}
		
		final String security = sslProperties.isSslEnabled() ? CommonConstants.SECURE_INTF : CommonConstants.INSECURE_INTF;
		final List<String> result = new ArrayList<>(interfaces.size());
		for (final InterfaceData interfaceData : interfaces) {
			result.add(interfaceData.getProtocol() + security + interfaceData.getFormat());
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createEchoUri(final String scheme) {
		logger.debug("createEchoUri started...");
				
		final String echoUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.ECHO_URI;
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), echoUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createRegisterUri(final String scheme) {
		logger.debug("createRegisterUri started...");
				
		final String registerUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI;
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registerUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createUnregisterUri(final String scheme, final CoreSystemService coreSystemService, final String address, final int port) {
		logger.debug("createUnregisterUri started...");
		
		final String unregisterUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI;
		final MultiValueMap<String,String> queryMap = new LinkedMultiValueMap<>(5);
		queryMap.put(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, List.of(coreSystemRegistrationProperties.getCoreSystem().name().toLowerCase()));
		queryMap.put(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, List.of(address));
		queryMap.put(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, List.of(String.valueOf(port)));
		queryMap.put(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, List.of(coreSystemService.getServiceDefinition()));
		queryMap.put(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_URI, List.of(coreSystemService.getServiceUri()));
		
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), queryMap, unregisterUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void initRequiredCoreSystemServiceUris(final ApplicationContext appContext) {
		logger.debug("initRequiredCoreSystemServiceUris started...");
		
		if (standaloneMode) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		
		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final UriComponents queryUri = createQueryUri(scheme);
		context.put(CoreCommonConstants.SR_QUERY_URI, queryUri);
		
		context.put(CoreCommonConstants.REQUIRED_URI_LIST, getRequiredCoreSystemServiceUris());
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryUri(final String scheme) {
		logger.debug("createQueryUri started...");
				
		final String registerUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_QUERY_URI;
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registerUriStr);
	}
}