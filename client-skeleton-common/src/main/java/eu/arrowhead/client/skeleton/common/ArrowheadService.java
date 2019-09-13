package eu.arrowhead.client.skeleton.common;


import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.client.skeleton.common.util.ClientCommonConstants;
import eu.arrowhead.client.skeleton.common.util.CoreServiceUri;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.http.HttpService;

@Component("ArrowheadService")
public class ArrowheadService {
	
	//=================================================================================================
	// members
	
	@Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
	private String clientSystemName;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
	private String clientSystemAddress;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
	private int clientSystemPort;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_ADDRESS_WD)
	private String serviceReqistryAddress;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PORT_WD)
	private int serviceRegistryPort;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private SSLProperties sslProperties;
	
	@Autowired
	private HttpService httpService;
	
	private final Logger logger = LogManager.getLogger(ArrowheadService.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public CoreServiceUri getCoreServiceUri(final CoreSystemService coreSystemService) {
		if (!CommonConstants.PUBLIC_CORE_SYSTEM_SERVICES.contains(coreSystemService)) {
			logger.debug("'{}' core service is not a public service.", coreSystemService);
			return null;
		} else if (!arrowheadContext.containsKey(coreSystemService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX)) {
			logger.debug("'{}' core service is not contained by Arrowhead Context.", coreSystemService);
			return null;
		} else {
			return (CoreServiceUri) arrowheadContext.get(coreSystemService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	public void updateCoreServiceURIs(final CoreSystem coreSystem) {
		final List<CoreSystemService> publicServices = getPublicServicesOfCoreSystem(coreSystem);
		if (publicServices.isEmpty()) {
			logger.info("'{}' core system has no public service.", coreSystem.name());
			return;
		}
		
		for (final CoreSystemService coreService : publicServices) {			
			try {	
				final ResponseEntity<ServiceQueryResultDTO> response = queryServiceReqistryByCoreService(coreService);
				
				if (response.getStatusCode() != HttpStatus.OK) {
					logger.info("'{}' core service couldn't be retrieved due to the following reason: service registry response status {}", coreService.getServiceDefinition(), response.getStatusCode().name());
					arrowheadContext.remove(coreService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX);
					
				} else if (response.getBody().getServiceQueryData().isEmpty()) {
					logger.info("'{}' core service couldn't be retrieved due to the following reason: not registered by Serivce Registry", coreService.getServiceDefinition());
					arrowheadContext.remove(coreService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX);
					
				} else {
					final ServiceRegistryResponseDTO serviceRegistryResponseDTO = response.getBody().getServiceQueryData().get(0);
					arrowheadContext.put(coreService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX, new CoreServiceUri(serviceRegistryResponseDTO.getProvider().getAddress(),
							serviceRegistryResponseDTO.getProvider().getPort(), serviceRegistryResponseDTO.getServiceUri()));					
				}
				
			} catch (final  UnavailableServerException | AuthException ex) {
				logger.info("'{}' core service couldn't be retrieved due to the following reason: {}", coreService.getServiceDefinition(), ex.getMessage());
			}			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<String> echoCoreSystem(final CoreSystem coreSystem) {		
		String address = null;
		Integer port = null;
		String coreUri = null;
		
		if (coreSystem == CoreSystem.SERVICE_REGISTRY) {
			address = serviceReqistryAddress;
			port = serviceRegistryPort;
			coreUri = CommonConstants.SERVICE_REGISTRY_URI;
			
		} else {			
			final List<CoreSystemService> publicServices = getPublicServicesOfCoreSystem(coreSystem);			
			if (publicServices.isEmpty()) {
				logger.debug("'{}' core system has no public service.", coreSystem.name());
				return null;
				
			} else {				
				final ResponseEntity<ServiceQueryResultDTO> srResponse = queryServiceReqistryByCoreService(publicServices.get(0));
				
				if (srResponse.getStatusCode() != HttpStatus.OK || srResponse.getBody().getServiceQueryData().isEmpty()) {
					logger.debug("'{}' core system not known by Service Registry", coreSystem.name());
					return null;
				} else {
					address = srResponse.getBody().getServiceQueryData().get(0).getProvider().getAddress();
					port = srResponse.getBody().getServiceQueryData().get(0).getProvider().getPort();
					coreUri = publicServices.get(0).getServiceUri().split("/")[1];
				}				
			}			
		}
		
		return httpService.sendRequest(Utilities.createURI(getUriScheme(), address, port, coreUri + CommonConstants.ECHO_URI), HttpMethod.GET, String.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<ServiceRegistryResponseDTO> registerServiceToServiceRegistry(final ServiceRegistryRequestDTO request) {
		final String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
		final UriComponents registerUri = Utilities.createURI(getUriScheme(), serviceReqistryAddress, serviceRegistryPort, registerUriStr);
		
		return httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<ServiceRegistryResponseDTO> forceRegisterServiceToServiceRegistry(final ServiceRegistryRequestDTO request) {
		final String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
		final UriComponents registerUri = Utilities.createURI(getUriScheme(), serviceReqistryAddress, serviceRegistryPort, registerUriStr);
		
		try {			
			return httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
		} catch (final InvalidParameterException ex) {
			unregisterServiceFromServiceRegistry(request.getServiceDefinition());
			return httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
		}	
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<Void> unregisterServiceFromServiceRegistry(final String serviceDefinition) {
		final String unregisterUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
		final MultiValueMap<String,String> queryMap = new LinkedMultiValueMap<>(4);
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, List.of(clientSystemName));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS, List.of(clientSystemAddress));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT, List.of(String.valueOf(clientSystemPort)));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, List.of(serviceDefinition));
		final UriComponents unregisterUri = Utilities.createURI(getUriScheme(), serviceReqistryAddress, serviceRegistryPort, queryMap, unregisterUriStr);
		
		return httpService.sendRequest(unregisterUri, HttpMethod.DELETE, Void.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	public PublicKey queryAuthorizationPublicKey() {
		final CoreServiceUri uri = getCoreServiceUri(CoreSystemService.AUTH_PUBLIC_KEY_SERVICE);
		if (uri == null) {
			logger.debug("Authorization Public Key couldn't be retrieved due to the following reason: " +  CoreSystemService.AUTH_PUBLIC_KEY_SERVICE.name() + " not known by Arrowhead Context");
			return null;
		}
		
		final ResponseEntity<String> response = httpService.sendRequest(Utilities.createURI(getUriScheme(), uri.getAddress(), uri.getPort(), uri.getPath()), HttpMethod.GET, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			logger.debug("Authorization Public Key couldn't be retrieved due to the following reason: service registry response status {}", response.getStatusCode().name());
			return null;
		}
		
		
		return Utilities.getPublicKeyFromBase64EncodedString(response.getBody());
	}
	
	//-------------------------------------------------------------------------------------------------
	public Builder getOrchestrationFormBuilder() {
		final SystemRequestDTO thisSystem = new SystemRequestDTO();
		thisSystem.setSystemName(clientSystemName);
		thisSystem.setAddress(clientSystemAddress);
		thisSystem.setPort(clientSystemPort);
		if (sslProperties.isSslEnabled()) {
			final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
			thisSystem.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}
		
		return new OrchestrationFormRequestDTO.Builder(thisSystem);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<OrchestrationResponseDTO> proceedOrchestration(final OrchestrationFormRequestDTO request) {
		final CoreServiceUri uri = getCoreServiceUri(CoreSystemService.ORCHESTRATION_SERVICE);
		if (uri == null) {
			logger.debug("Orchestration couldn't be proceeded due to the following reason: " +  CoreSystemService.ORCHESTRATION_SERVICE.name() + " not known by Arrowhead Context");
			return null;
		}
		
		return httpService.sendRequest(Utilities.createURI(getUriScheme(), uri.getAddress(), uri.getPort(), uri.getPath()), HttpMethod.POST, OrchestrationResponseDTO.class, request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<ServiceQueryResultDTO> queryServiceReqistryByCoreService(final CoreSystemService coreService) {
		final ServiceQueryFormDTO request = new ServiceQueryFormDTO();
		request.setServiceDefinitionRequirement(coreService.getServiceDefinition());
		
		return httpService.sendRequest(Utilities.createURI(getUriScheme(), serviceReqistryAddress, serviceRegistryPort, CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI),
									   HttpMethod.POST, ServiceQueryResultDTO.class, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getUriScheme() {
		return sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<CoreSystemService> getPublicServicesOfCoreSystem(final CoreSystem coreSystem) {
		final List<CoreSystemService> publicServices = new ArrayList<>();
		for (final CoreSystemService coreSystemService : coreSystem.getServices()) {
			for (final CoreSystemService publicCoreService : CommonConstants.PUBLIC_CORE_SYSTEM_SERVICES) {
				if (coreSystemService == publicCoreService) {
					publicServices.add(coreSystemService);
				}
			}			
		}
		return publicServices;
	}
}
