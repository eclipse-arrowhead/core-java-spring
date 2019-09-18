package eu.arrowhead.client.skeleton.common;


import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
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
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
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

	//------------------------------------------------------------------------------------------------
	/**
	 * @param coreSystemService CoreSystemService enum which represents an Arrowhead Core System Service
	 * @return the URI details of the Arrowhead Core System or null when the specified coreSystemService is not a public one or ArrowhedContext component not contains the the given core service.
	 */
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
	/**
	 * Queries and stores the public service URIs of the given Arrowhead Core System in the ArrowheadContext component. 
	 * If the specified Core System has no public service or the server is not available, then ArrowheadContext won't contain the core service and a log info message will be triggered.
	 * 
	 * @param coreSystem CoreSystem enum which represents an Arrowhead Core System
	 */
	public void updateCoreServiceURIs(final CoreSystem coreSystem) {
		final List<CoreSystemService> publicServices = getPublicServicesOfCoreSystem(coreSystem);
		if (publicServices.isEmpty()) {
			logger.info("'{}' core system has no public service.", coreSystem.name());
			return;
		}
		
		for (final CoreSystemService coreService : publicServices) {			
			try {	
				final ResponseEntity<ServiceQueryResultDTO> response = queryServiceReqistryByCoreService(coreService);
				
				if (response.getBody().getServiceQueryData().isEmpty()) {
					logger.info("'{}' core service couldn't be retrieved due to the following reason: not registered by Serivce Registry", coreService.getServiceDefinition());
					arrowheadContext.remove(coreService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX);
					
				} else {
					final ServiceRegistryResponseDTO serviceRegistryResponseDTO = response.getBody().getServiceQueryData().get(0);
					arrowheadContext.put(coreService.getServiceDefinition() + ClientCommonConstants.CORE_SERVICE_DEFINITION_SUFFIX, new CoreServiceUri(serviceRegistryResponseDTO.getProvider().getAddress(),
							serviceRegistryResponseDTO.getProvider().getPort(), serviceRegistryResponseDTO.getServiceUri()));					
				}
				
			} catch (final  ArrowheadException ex) {
				logger.info("'{}' core service couldn't be retrieved due to the following reason: {}", coreService.getServiceDefinition(), ex.getMessage());
			}			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	/**
	 * Sends a http(s) request to the 'echo' end point of the given Arrowhead Core System.
	 * 
	 * @param coreSystem CoreSystem enum which represents an Arrowhead Core System
	 * @return the response received from core system server or null when the specified core system has no public service or it is not known by Service Registry Core System
	 * @throws ArrowheadException when internal server error happened at Service Registry or at the given core system
	 * @throws UnavailableServerException when Service Registry Core System or the given core system is not available
	 */
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
				
				if (srResponse.getBody().getServiceQueryData().isEmpty()) {
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
	/**
	 * Sends a http(s) 'register' request to Service Registry Core System.
	 * 
	 * @param request ServiceRegistryRequestDTO which represents the required payload of the http(s) request
	 * @return the response received from Service Registry Core System
	 * @throws AuthException when you are not authorized by Service Registry Core System
	 * @throws BadPayloadException when the payload couldn't be validated by Service Registry Core System 
	 * @throws InvalidParameterException when the payload content couldn't be validated by Service Registry Core System
	 * @throws ArrowheadException when internal server error happened at Service Registry Core System
	 * @throws UnavailableServerException when Service Registry Core System is not available
	 */
	public ResponseEntity<ServiceRegistryResponseDTO> registerServiceToServiceRegistry(final ServiceRegistryRequestDTO request) {
		final String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
		final UriComponents registerUri = Utilities.createURI(getUriScheme(), serviceReqistryAddress, serviceRegistryPort, registerUriStr);
		
		return httpService.sendRequest(registerUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	/**
	 * Sends a http(s) 'register' request to Service Registry Core System. In the case of service already registered, then the old service registry entry will be overwritten.  
	 * 
	 * @param request ServiceRegistryRequestDTO which represents the required payload of the http(s) request
	 * @return the response received from Service Registry Core System
	 * @throws AuthException when you are not authorized by Service Registry Core System
	 * @throws BadPayloadException when the payload couldn't be validated by Service Registry Core System 
	 * @throws InvalidParameterException when the payload content couldn't be validated by Service Registry Core System
	 * @throws ArrowheadException when internal server error happened at Service Registry Core System
	 * @throws UnavailableServerException when Service Registry Core System is not available
	 */
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
	/**
	 * Sends a http(s) 'unregister' request to Service Registry Core System.
	 * 
	 * @param serviceDefinition String value which represents the service being deleted from service registry
	 * @return the response received from Service Registry Core System
	 * @throws AuthException when you are not authorized by Service Registry Core System
	 * @throws BadPayloadException when the payload couldn't be validated by Service Registry Core System 
	 * @throws InvalidParameterException when the payload content couldn't be validated by Service Registry Core System
	 * @throws ArrowheadException when internal server error happened at Service Registry Core System
	 * @throws UnavailableServerException when Service Registry Core System is not available
	 */
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
	/**
	 * Queries its public key from Authorization Core System. 
	 * 
	 * @return the public key of Authorization Core System or null when the public key core service URI is not known by ArrowheadContext component.
	 * @throws AuthException when you are not authorized by Authorization Core System
	 * @throws ArrowheadException when internal server error happened at Authorization Core System
	 * @throws UnavailableServerException when Authorization Core System is not available
	 */
	public PublicKey queryAuthorizationPublicKey() {
		final CoreServiceUri uri = getCoreServiceUri(CoreSystemService.AUTH_PUBLIC_KEY_SERVICE);
		if (uri == null) {
			logger.debug("Authorization Public Key couldn't be retrieved due to the following reason: " +  CoreSystemService.AUTH_PUBLIC_KEY_SERVICE.name() + " not known by Arrowhead Context");
			return null;
		}
		
		final ResponseEntity<String> response = httpService.sendRequest(Utilities.createURI(getUriScheme(), uri.getAddress(), uri.getPort(), uri.getPath()), HttpMethod.GET, String.class);
				
		return Utilities.getPublicKeyFromBase64EncodedString(response.getBody());
	}
	
	//-------------------------------------------------------------------------------------------------
	/** 
	 * @return your public key or null when https mode is not enabled
	 */
	public PublicKey getMyPublicKey() {
		if (sslProperties.isSslEnabled()) {
			return (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		} else {
			return null;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	/**
	 * @return an Orchestration form builder prefilled with your system properties
	 */
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
	/**
	 * Sends a http(s) 'orchestration' request to Orchestrator Core System.
	 * 
	 * @param request OrchestrationFormRequestDTO which represents the required payload of the http(s) request
	 * @return the response received from Orchestrator Core System
	 * @throws AuthException when you are not authorized by Orchestrator Core System
	 * @throws BadPayloadException when the payload couldn't be validated by Orchestrator Core System 
	 * @throws InvalidParameterException when the payload content couldn't be validated by Orchestrator Core System
	 * @throws ArrowheadException when internal server error happened at one of the core system involved in orchestration process 
	 * @throws UnavailableServerException when one of the core system involved in orchestration process is not available 
	 */
	public ResponseEntity<OrchestrationResponseDTO> proceedOrchestration(final OrchestrationFormRequestDTO request) {
		final CoreServiceUri uri = getCoreServiceUri(CoreSystemService.ORCHESTRATION_SERVICE);
		if (uri == null) {
			logger.debug("Orchestration couldn't be proceeded due to the following reason: " +  CoreSystemService.ORCHESTRATION_SERVICE.name() + " not known by Arrowhead Context");
			return null;
		}
		
		return httpService.sendRequest(Utilities.createURI(getUriScheme(), uri.getAddress(), uri.getPort(), uri.getPath()), HttpMethod.POST, OrchestrationResponseDTO.class, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	/**
	 * Sends a http(s) request with the specified service reachability details.
	 * 
	 * @param httpMethod HttpMethod enum which represents the method how the service is available.
	 * @param address String value which represents the host where the service is available.
	 * @param port int value which represents the port where the service is available
	 * @param serviceUri String value which represents the URI where the service is available.
	 * @param interfaceName String value which represents the name of the interface used for the communication. Usable interfaces could be received in orchestration response.
	 * @param token (nullable) String value which represents the token for being authorized at the provider side if necessary. Token could be received in orchestration response per interface type.  
	 * @param payload (nullable) Object type which represents the required payload of the http(s) request if any necessary.
	 * @param queryParams (nullable) String... variable arguments which represent the additional key-value http(s) query parameters if any necessary. E.g.: "k1", "v1", "k2", "v2".  
	 * @return the response received from the provider 
	 * 
	 * @throws InvalidParameterException when service URL can't be assembled.
	 * @throws AuthException when ssl context or access control related issue happened.
	 * @throws ArrowheadException when the communication is managed via Gateway Core System and internal server error happened.
	 * @throws UnavailableServerException when the specified server is not available.
	 */
	public ResponseEntity<Object> consumeServiceHTTP(final HttpMethod httpMethod, final String address, final int port, final String serviceUri, final String interfaceName, final String token,
													 final Object payload, final String... queryParams) {
		if (httpMethod == null) {
			throw new InvalidParameterException("httpMethod cannot be null.");
		}
		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("address cannot be null or blank.");
		}
		if (Utilities.isEmpty(serviceUri)) {
			throw new InvalidParameterException("serviceUri cannot be null or blank.");
		}
		if (Utilities.isEmpty(interfaceName)) {
			throw new InvalidParameterException("interfaceName cannot be null or blank.");
		}
		
		final String protocolStr = interfaceName.split("-")[0];
		if (!protocolStr.equalsIgnoreCase(CommonConstants.HTTP) && !protocolStr.equalsIgnoreCase(CommonConstants.HTTPS)) {
			throw new InvalidParameterException("Invalid interfaceName: protocol should be 'http' or 'https'.");
		}
		
		UriComponents uri;
		if(!Utilities.isEmpty(token)) {
			final List<String> query = new ArrayList<>();
			query.addAll(Arrays.asList(queryParams));
			query.add(CommonConstants.REQUEST_PARAM_TOKEN);
			query.add(token);
			uri = Utilities.createURI(protocolStr, address, port, serviceUri, query.toArray(new String[query.size()]));
		} else {
			uri = Utilities.createURI(protocolStr, address, port, serviceUri, queryParams);
		}
		
		return httpService.sendRequest(uri, httpMethod, Object.class, payload);
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
