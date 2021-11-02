package eu.arrowhead.core.choreographer.service;

import java.util.ArrayList;
import java.util.HashMap;
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
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryResultDTO;
import eu.arrowhead.common.dto.internal.KeyValuesDTO;
import eu.arrowhead.common.dto.shared.ActiveSessionCloseErrorDTO;
import eu.arrowhead.common.dto.shared.ChoreographerAbortStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecuteStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class ChoreographerDriver {

    //=================================================================================================
    // members
	
	private static final String SEPARATOR = "/";

    private static final String ORCHESTRATION_PROCESS_BY_PROXY_URI_KEY = CoreSystemService.ORCHESTRATION_BY_PROXY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    private static final String GATEKEEPER_MULTI_GSD_URI_KEY = CoreSystemService.GATEKEEPER_MULTI_GLOBAL_SERVICE_DISCOVERY.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    private static final String GATEWAY_CLOSE_SESSIONS_URI_KEY = CoreSystemService.GATEWAY_CLOSE_SESSIONS_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
    
    public static final String OWN_CLOUD_MARKER = "#OWN_CLOUD#";
    
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
    public KeyValuesDTO pullServiceRegistryConfig() {
    	logger.debug("pullServiceRegistryConfig started...");
    	final UriComponents uri = getPullServiceRegistryConfigUri();
    	return httpService.sendRequest(uri, HttpMethod.GET, KeyValuesDTO.class).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
	public Map<Integer,List<String>> searchForServices(final ServiceQueryFormListDTO forms, final boolean allowInterCloud) { 
		logger.debug("searchForServices started...");
		Assert.notNull(forms, "Forms is null.");
		Assert.isTrue(!Utilities.isEmpty(forms.getForms()), "Form list is empty.");
		
		final List<ServiceQueryFormDTO> interCloudCandidates = new ArrayList<>();
		final List<Integer> interCloudCandidatesOriginalIdx = new ArrayList<>();
		final Map<Integer,List<String>> result = new HashMap<>(forms.getForms().size());
		
		final ServiceQueryResultListDTO localResponse = multiQueryServiceRegistry(forms);
		
		for (int i = 0; i < localResponse.getResults().size(); ++i) {
			final ServiceQueryFormDTO form = forms.getForms().get(i);
			final ServiceQueryResultDTO resultDTO = localResponse.getResults().get(i);
			
			if (resultDTO.getServiceQueryData().isEmpty()) {
				interCloudCandidates.add(form);
				interCloudCandidatesOriginalIdx.add(i);
				result.put(i, new ArrayList<>());
			} else {
				result.put(i, List.of(OWN_CLOUD_MARKER));
			}
		}
		
		if (!interCloudCandidates.isEmpty() && allowInterCloud) { // we may find the missing providers in other clouds
			if (hasLocalOnlyCandidate(interCloudCandidates)) {
				// no reason to continue, because there is at least one service without any provider
				return result;
			}
			
			final GSDMultiQueryResultDTO gsdResult = multiGlobalServiceDiscovery(new GSDMultiQueryFormDTO(interCloudCandidates, null));
			for (final GSDMultiPollResponseDTO pollResponse : gsdResult.getResults()) {
				final String cloudIdentifier = getCloudIdentifier(pollResponse.getProviderCloud());
				if (!pollResponse.getProvidedServiceDefinitions().isEmpty()) {
					for (final String serviceDef : pollResponse.getProvidedServiceDefinitions()) {
						final int idx = findServiceDefinitionIn(interCloudCandidates, serviceDef);
						if (idx < 0) {
							// should never happens
							logger.warn("Global Service Discovery returns an unwanted service: {}", serviceDef);
							continue;
						}
						
						final int originalIdx = interCloudCandidatesOriginalIdx.get(idx);
						result.get(originalIdx).add(cloudIdentifier);
					}
				}
			}
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
    public SystemResponseDTO queryServiceRegistryBySystem(final String systemName, final String address, final int port) {
    	logger.debug("queryServiceRegistryBySystem started...");
    	Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is empty");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	
    	final UriComponents uri = getQueryServiceRegistryBySystemUri();
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
    	Assert.notNull(baseUri, "baseUri is null");
    	Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "serviceDefinition is empty");
    	
    	final ChoreographerExecutorServiceInfoRequestDTO dto = new ChoreographerExecutorServiceInfoRequestDTO(serviceDefinition, minVersion, maxVersion);
    	final UriComponents uri = getQueryExecutorServiceInfoUri(address, port, baseUri);
    	return httpService.sendRequest(uri, HttpMethod.POST, ChoreographerExecutorServiceInfoResponseDTO.class, dto).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
    public void startExecutor(final String address, final int port, final String baseUri, final ChoreographerExecuteStepRequestDTO payload) {
    	logger.debug("startExecutor started...");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	Assert.notNull(baseUri, "baseUri is null");
    	Assert.notNull(payload, "payload is null");
    	
    	final UriComponents uri = getStartExecutorUri(address, port, baseUri);
    	httpService.sendRequest(uri, HttpMethod.POST, Void.class, payload);
    }
    
    //-------------------------------------------------------------------------------------------------
    public void abortExecutor(final String address, final int port, final String baseUri, final ChoreographerAbortStepRequestDTO payload) {
    	logger.debug("getExecutorServiceInfo started...");
    	Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
    	Assert.notNull(baseUri, "baseUri is null");
    	Assert.notNull(payload, "payload is null");
    	
    	final UriComponents uri = getAbortExecutorUri(address, port, baseUri);
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
	public void sendSessionNotification(final String notifyUri, final ChoreographerNotificationDTO payload) {
		logger.debug("sendSessionNotification started...");
		Assert.isTrue(!Utilities.isEmpty(notifyUri), "Notification URI is not specified.");
		Assert.notNull(payload, "Payload is not specified.");
		
		final UriComponents uri = UriComponentsBuilder.fromUriString(notifyUri).build();
		httpService.sendRequest(uri, HttpMethod.POST, Void.class, payload);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void closeGatewayTunnels(final List<Integer> ports) { 
		logger.debug("closeGatewayTunnels started...");
		Assert.isTrue(!Utilities.isEmpty(ports), "Port list is null or empty.");
		
		final UriComponents uri = getGatewayCloseSessionsUri();
		try {
			@SuppressWarnings("rawtypes")
			final ResponseEntity<List> response = httpService.sendRequest(uri, HttpMethod.POST, List.class, ports);
			@SuppressWarnings("unchecked")
			final List<ActiveSessionCloseErrorDTO> body = response.getBody();
			for (final ActiveSessionCloseErrorDTO errorDTO : body) {
				logger.warn("Problem occurs while trying to close gateway tunnel for port {}: {}", errorDTO.getPort(), errorDTO.getError());
			}
		} catch (final Exception ex) {
			logger.warn(ex.getMessage());
			logger.debug("Stacktrace: ", ex);
		}
	}
	
    //=================================================================================================
    // assistant methods
	
	//-------------------------------------------------------------------------------------------------
    private UriComponents getPullServiceRegistryConfigUri() {
        logger.debug("getPullServiceRegistryConfigUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_PULL_CONFIG_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_PULL_CONFIG_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Service Registry pull-config URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Service Registry pull-config URI.");
    }

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
    private UriComponents getMultiGlobalServiceDiscoveryUri() {
        logger.debug("getMultiGlobalServiceDiscoveryUri started...");

        if (arrowheadContext.containsKey(GATEKEEPER_MULTI_GSD_URI_KEY)) {
            try {
                return (UriComponents) arrowheadContext.get(GATEKEEPER_MULTI_GSD_URI_KEY);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find Gatekeeper Multi Global Service Discovery URI.");
            }
        }

        throw new ArrowheadException("Choreographer can't find Gatekeeper Multi Global Service Discovery URI.");
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
    private UriComponents getAbortExecutorUri(final String address, final int port, final String baseUri) {
    	logger.debug("getAbortExecutorUri started...");
    	
    	final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
    	return Utilities.createURI(scheme, address, port, baseUri + CommonConstants.CHOREOGRAPHER_EXECUTOR_CLIENT_SERVICE_ABORT_URI);
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getOrchestrationProcessUri() {
        logger.debug("getOrchestrationProcessUri started...");

        if (arrowheadContext.containsKey(ORCHESTRATION_PROCESS_BY_PROXY_URI_KEY)) {
            try {
                return (UriComponents) arrowheadContext.get(ORCHESTRATION_PROCESS_BY_PROXY_URI_KEY);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find orchestration process URI.");
            }
        }
        throw new ArrowheadException("Choreographer can't find orchestration process URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private UriComponents getGatewayCloseSessionsUri() {
        logger.debug("getGatewayCloseSessionsUri started...");

        if (arrowheadContext.containsKey(GATEWAY_CLOSE_SESSIONS_URI_KEY)) {
            try {
                return (UriComponents) arrowheadContext.get(GATEWAY_CLOSE_SESSIONS_URI_KEY);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Choreographer can't find gateway close sessions URI.");
            }
        }
        throw new ArrowheadException("Choreographer can't find gateway close sessions URI.");
    }
    
    //-------------------------------------------------------------------------------------------------
    private ServiceQueryResultListDTO multiQueryServiceRegistry(final ServiceQueryFormListDTO forms) {
        logger.debug("multiQueryServiceRegistry started...");

        final UriComponents uri = getMultiQueryServiceRegistryUri();
        return httpService.sendRequest(uri, HttpMethod.POST, ServiceQueryResultListDTO.class, forms).getBody();
    }
    
    //-------------------------------------------------------------------------------------------------
	private GSDMultiQueryResultDTO multiGlobalServiceDiscovery(final GSDMultiQueryFormDTO form) { 
		logger.debug("multiGlobalServiceDiscovery started...");
		
		final UriComponents uri = getMultiGlobalServiceDiscoveryUri();
		return httpService.sendRequest(uri, HttpMethod.POST, GSDMultiQueryResultDTO.class, form).getBody();
	}
	
    //-------------------------------------------------------------------------------------------------
	private boolean hasLocalOnlyCandidate(final List<ServiceQueryFormDTO> candidates) {
		logger.debug("hasLocalOnlyCandidate started...");
		
		for (final ServiceQueryFormDTO form : candidates) {
			if (form instanceof ChoreographerServiceQueryFormDTO) {
				final ChoreographerServiceQueryFormDTO _form = (ChoreographerServiceQueryFormDTO) form;
				if (_form.isLocalCloudOnly()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getCloudIdentifier(final CloudResponseDTO cloud) {
		logger.debug("getCloudIdentifier started...");
		
		return cloud.getOperator() + SEPARATOR + cloud.getName();
	}
	
	//-------------------------------------------------------------------------------------------------
	private int findServiceDefinitionIn(final List<ServiceQueryFormDTO> forms, final String serviceDef) {
		logger.debug("findServiceDefinitionIn started...");

		for (int i = 0; i < forms.size(); ++i) {
			final ServiceQueryFormDTO form = forms.get(i);
			if (serviceDef.equals(form.getServiceDefinitionRequirement())) {
				return i;
			}
		}
		
		return -1;
	}

}