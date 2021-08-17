package eu.arrowhead.core.choreographer.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.TokenGenerationMultiServiceResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecuteStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class ChoreographerDriver {

    //=================================================================================================
    // members
	
    private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    private static final String AUTH_TOKEN_GENERATION_MULTI_SERVICE_URI_KEY = CoreSystemService.AUTH_TOKEN_GENERATION_MULTI_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;

    @Autowired
    private HttpService httpService;
    
    @Autowired
	protected SSLProperties sslProperties;

    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String,Object> arrowheadContext;

    private final Logger logger = LogManager.getLogger(ChoreographerDriver.class);

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ServiceQueryResultListDTO multiQueryServiceRegistry(final ServiceQueryFormListDTO forms) {
        logger.debug("multiQueryServiceRegistry started...");
        Assert.notNull(forms, "ServiceQueryFormListDTOt is null.");

        final UriComponents uri = getMultiQueryServiceRegistryUri();
        return httpService.sendRequest(uri, HttpMethod.POST, ServiceQueryResultListDTO.class, forms).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public SystemResponseDTO queryServiceRegistryBySystem(final String systemName, final String address, final int port) {
    	Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is empty");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	
    	UriComponents uri = getQueryServiceRegistryBySystemUri();
    	final SystemRequestDTO request = new SystemRequestDTO();
    	request.setSystemName(systemName);
    	request.setAddress(address);
    	request.setPort(port);
    	return httpService.sendRequest(uri, HttpMethod.POST, SystemResponseDTO.class, request).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public SystemResponseDTO registerSystem(final SystemRequestDTO request) {
    	logger.debug("registerSystem started...");
        Assert.notNull(request, "SystemRequestDTO is null.");
        
        final UriComponents uri = getRegisterSystemUri();
        return httpService.sendRequest(uri, HttpMethod.POST, SystemResponseDTO.class, request).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public void unregisterSystem(final String systemName, final String address, final int port) {
    	logger.debug("unregisterSystem started...");
    	Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is empty");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	
    	final UriComponents uri = getUnregisterSystemUri(systemName, address, port);
    	httpService.sendRequest(uri, HttpMethod.DELETE, Void.class);
    }
    
    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceInfoResponseDTO queryExecutorServiceInfo(final String address, final int port, final String baseUri, final String serviceDefinition, final int minVersion, final int maxVersion) {
    	logger.debug("getExecutorServiceInfo started...");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	Assert.isTrue(!Utilities.isEmpty(baseUri), "baseUri is empty");
    	Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "serviceDefinition is empty");
    	
    	final ChoreographerExecutorServiceInfoRequestDTO dto = new ChoreographerExecutorServiceInfoRequestDTO(serviceDefinition, minVersion, maxVersion);
    	final UriComponents uri = getQueryExecutorServiceInfoUri(address, port, baseUri);
    	return httpService.sendRequest(uri, HttpMethod.POST, ChoreographerExecutorServiceInfoResponseDTO.class, dto).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public void startExecutor(final String address, final int port, final String baseUri, final ChoreographerExecuteStepRequestDTO payload) {
    	logger.debug("getExecutorServiceInfo started...");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	Assert.isTrue(!Utilities.isEmpty(baseUri), "baseUri is empty");
    	Assert.notNull(payload, "payload is null");
    	
    	final UriComponents uri = getStartExecutorUri(address, port, baseUri);
    	httpService.sendRequest(uri, HttpMethod.POST, Void.class, payload);
    }
    
    //-------------------------------------------------------------------------------------------------
    public OrchestrationResponseDTO queryOrchestrator(final OrchestrationFormRequestDTO form) {
        logger.debug("queryOrchestrator started...");

        Assert.notNull(form, "form is null.");

        final UriComponents orchestrationProcessUri = getOrchestrationProcessUri();
        final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(orchestrationProcessUri, HttpMethod.POST, OrchestrationResponseDTO.class, form);

        return response.getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public TokenGenerationMultiServiceResponseDTO generateMultiServiceAuthorizationTokens(final List<TokenGenerationRequestDTO> tokenGenerationRequests) {
    	logger.debug("generateMultiServiceAuthorizationTokens started...");

        Assert.notNull(tokenGenerationRequests, "tokenGenerationRequests list is null.");
    	UriComponents uri = getAuthorizationGernerateTokenMultiServiceUri();
    	return httpService.sendRequest(uri, HttpMethod.POST, TokenGenerationMultiServiceResponseDTO.class, tokenGenerationRequests).getBody();
    }
    
	//-------------------------------------------------------------------------------------------------
	public void sendSessionNotification(final String notifyUri, final ChoreographerNotificationDTO payload) {
		logger.debug("sendSessionNotification started...");
		Assert.isTrue(!Utilities.isEmpty(notifyUri), "Notification URI is not specified.");
		Assert.notNull(payload, "Payload is not specified");
		
		final UriComponents uri = UriComponentsBuilder.fromUriString(notifyUri).build();
		httpService.sendRequest(uri, HttpMethod.POST, Void.class, payload);
	}
	
    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private UriComponents getMultiQueryServiceRegistryUri() {
        logger.debug("getMultiQueryServiceRegistryUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry multi-query URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry multi-query URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getQueryServiceRegistryBySystemUri() {
        logger.debug("getQueryServiceRegistryBySystemUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry query by system URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry query by system URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getRegisterSystemUri() {
    	logger.debug("getRegisterSystemUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_REGISTER_SYSTEM_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_REGISTER_SYSTEM_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry Register System URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry Register System URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getUnregisterSystemUri(final String systemName, final String address, final int port) {
    	logger.debug("getUnregisterSystemUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI)) {
            try {
            	final UriComponents uri = (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_UNREGISTER_SYSTEM_URI);
            	return UriComponentsBuilder.fromUri(uri.toUri()).queryParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, systemName)
		            											.queryParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, address)
		            											.queryParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, port).build();
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry Unregister System URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry Unregister System URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getQueryExecutorServiceInfoUri(final String address, final int port, final String baseUri) {
    	logger.debug("getExecutorServiceInfoUri started...");
    	
    	final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
    	return Utilities.createURI(scheme, address, port, baseUri + CommonConstants.CHOREOGRAPHER_EXECUTOR_CLIENT_SERVICE_INFO_URI);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getStartExecutorUri(final String address, final int port, final String baseUri) {
    	logger.debug("getStartExecutorUri started...");
    	
    	final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
    	return Utilities.createURI(scheme, address, port, baseUri + CommonConstants.CHOREOGRAPHER_EXECUTOR_CLIENT_SERVICE_START_URI);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getOrchestrationProcessUri() {
        logger.debug("getOrchestrationProcessUri started...");

        if (arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)) {
            try {
                return (UriComponents) arrowheadContext.get(ORCHESTRATION_PROCESS_URI_KEY);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find orchestration process URI.");
            }
        }
        throw new ArrowheadException("Choreographer can't find orchestration process URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getAuthorizationGernerateTokenMultiServiceUri() {
        logger.debug("getAuthorizationGernerateTokenMultiServiceUri started...");

        if (arrowheadContext.containsKey(AUTH_TOKEN_GENERATION_MULTI_SERVICE_URI_KEY)) {
            try {
                return (UriComponents) arrowheadContext.get(AUTH_TOKEN_GENERATION_MULTI_SERVICE_URI_KEY);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't authorization generate multi service token URI.");
            }
        }
        throw new ArrowheadException("Choreographer can't authorization generate multi service token URI.");
    }
}
