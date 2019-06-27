package eu.arrowhead.common;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.http.HttpService;

@Component
public class ApplicationInitListener {
	
	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(ApplicationInitListener.class);
	
	private static final int MAX_NUMBER_OF_SERVICE_REGISTRY_CONNECTION_RETRIES = 3;
	private static final int WAITING_PERIOD_BETWEEN_RETRIES_IN_SECONDS = 15;
	private static final long WAITING_PERIOD_BETWEEN_RETRIES_IN_MILISECONDS = WAITING_PERIOD_BETWEEN_RETRIES_IN_SECONDS * 1000L;

	@Autowired
	private SSLProperties sslProperties;
	
	@Autowired
	private CoreSystemRegistrationProperties coreSystemRegistrationProperties;
	
	@Autowired
	private HttpService httpService;
	
	private PublicKey publicKey;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@EventListener
	@Order(10)
	public void onApplicationEvent(final ContextRefreshedEvent event) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InterruptedException {
		logger.debug("Initialization in onApplicationEvent()...");
		
		final CoreSystem coreSystem = coreSystemRegistrationProperties.getCoreSystem();
		logger.info("Core system name: {}", coreSystem.name());
		logger.info("Server mode: {}", sslProperties.isSslEnabled() ? "SECURED" : "NOT SECURED");
		
		if (sslProperties.isSslEnabled()) {
			final KeyStore keyStore = initializeKeyStore();
			checkServerCertificate(keyStore, event.getApplicationContext());
			obtainKeys(keyStore, event.getApplicationContext());
		}
		
		registerCoreSystemServicesToServiceRegistry(event.getApplicationContext());
		
		logger.debug("Initialization in onApplicationEvent() is done.");
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(CommonConstants.ARROWHEAD_CONTEXT)
	public Map<String,Object> getArrowheadContext() {
		return new ConcurrentHashMap<>();
	}
	
	//=================================================================================================
	// assistant methods
	
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
		final X509Certificate serverCertificate = Utilities.getFirstCertFromKeyStore(keyStore);
		final String serverCN = Utilities.getCertCNFromSubject(serverCertificate.getSubjectDN().getName());
		if (!Utilities.isKeyStoreCNArrowheadValid(serverCN)) {
			logger.info("Server CN ({}) is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".", serverCN);
			throw new AuthException("Server CN (" + serverCN + ") is not compliant with the Arrowhead certificate structure, since it does not have 5 parts, or does not end with \"arrowhead.eu\".");
		}
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		context.put(CommonConstants.SERVER_COMMON_NAME, serverCN);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void obtainKeys(final KeyStore keyStore, final ApplicationContext appContext) {
		logger.debug("obtainKeys started...");
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		
		publicKey = Utilities.getFirstCertFromKeyStore(keyStore).getPublicKey();
		context.put(CommonConstants.SERVER_PUBLIC_KEY, publicKey);
		
		final PrivateKey privateKey = Utilities.getPrivateKey(keyStore, sslProperties.getKeyPassword());
		context.put(CommonConstants.SERVER_PRIVATE_KEY, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void registerCoreSystemServicesToServiceRegistry(final ApplicationContext appContext) throws InterruptedException {
		logger.debug("registerCoreSystemServicesToServiceRegistry started...");
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		if (context.containsKey(CommonConstants.SERVER_STANDALONE_MODE)) {
			// skip the registration
			return;
		}
		
		final CoreSystem coreSystem = coreSystemRegistrationProperties.getCoreSystem();
		if (CoreSystem.SERVICE_REGISTRY == coreSystem) {
			// do nothing
			return;
		}
		
		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
		checkServiceRegistryConnection(scheme);
		
//		final SystemRequestDTO coreSystemDTO = getCoreSystemRequestDTO();
//		for (final CoreSystemService coreService : coreSystem.getServices()) {
//			final UriComponents registerUri = createRegisterUri(scheme);
//			final ServiceRegistryRequestDTO request = getCoreSystemServiceRegistryRequestDTO(coreSystemDTO, coreService);
//			final ResponseEntity<ServiceRegistryResponseDTO> response = httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
//			// TODO cont
//		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryConnection(final String scheme) throws InterruptedException {
		logger.debug("checkServiceRegistryConnection started...");
	
		final UriComponents echoUri = createEchoUri(scheme);
		for (int i = 0; i <= MAX_NUMBER_OF_SERVICE_REGISTRY_CONNECTION_RETRIES; ++i) {
			try {
				httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
				logger.info("Service Registry is accessible...");
				break;
			} catch (final AuthException ex) {
				throw ex;
			} catch (final ArrowheadException ex) {
				if (i == MAX_NUMBER_OF_SERVICE_REGISTRY_CONNECTION_RETRIES) {
					throw ex;
				} else {
					logger.info("Service Registry is unavailable at the moment, retrying in {} seconds...", WAITING_PERIOD_BETWEEN_RETRIES_IN_SECONDS);
					Thread.sleep(WAITING_PERIOD_BETWEEN_RETRIES_IN_MILISECONDS);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getCoreSystemRequestDTO() {
		logger.debug("getCoreSystemRequestDTO started...");
		
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName(coreSystemRegistrationProperties.getCoreSystem().name().toLowerCase());
		result.setAddress(coreSystemRegistrationProperties.getCoreSystemAddress());
		result.setPort(coreSystemRegistrationProperties.getCoreSystemPort());
		
		if (sslProperties.isSslEnabled()) {
			result.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceRegistryRequestDTO getCoreSystemServiceRegistryRequestDTO(final SystemRequestDTO coreSystemDTO, final CoreSystemService coreSystemService) {
		logger.debug("getCoreSystemServiceRegistryRequestDTO started...");
		
		final List<String> interfaces = sslProperties.isSslEnabled() ? List.of(CommonConstants.HTTP_SECURE_JSON) : List.of(CommonConstants.HTTP_INSECURE_JSON);
		final ServiceRegistryRequestDTO result = new ServiceRegistryRequestDTO();
		result.setProviderSystem(coreSystemDTO);
		result.setServiceDefinition(coreSystemService.getServiceDefinition());
		result.setServiceUri(coreSystemService.getServiceUri());
		result.setSecure(sslProperties.isSslEnabled() ? ServiceSecurityType.CERTIFICATE : ServiceSecurityType.NOT_SECURE);
		result.setInterfaces(interfaces);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createEchoUri(final String scheme) {
		logger.debug("createEchoUri started...");
				
		final String echoUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.ECHO_URI;
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), echoUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createRegisterUri(final String scheme) {
		logger.debug("createRegisterUri started...");
				
		final String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registerUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createUnregisterUri(final String scheme, final CoreSystemService coreSystemService) {
		logger.debug("createUnregisterUri started...");
		
		final String unregisterUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
		final MultiValueMap<String,String> queryMap = new LinkedMultiValueMap<>(4);
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, List.of(coreSystemRegistrationProperties.getCoreSystem().name().toLowerCase()));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS, List.of(coreSystemRegistrationProperties.getCoreSystemAddress()));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT, List.of(String.valueOf(coreSystemRegistrationProperties.getCoreSystemPort())));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, List.of(coreSystemService.getServiceDefinition()));
		
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), queryMap, unregisterUriStr);
	}
}