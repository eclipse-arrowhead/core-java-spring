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

package eu.arrowhead.core.orchestrator.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.dto.internal.TokenDataDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class OrchestratorDriver {

	//=================================================================================================
	// members
	
	private static final String AUTH_TOKEN_GENERATION_URI_KEY = CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String AUTH_INTRA_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_INTRA_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_INIT_GSD_URI_KEY = CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_INIT_ICN_URI_KEY = CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_GET_CLOUD_URI_KEY = CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_INTRA_PING_MEASUREMENT_URI_KEY = CoreSystemService.QOSMONITOR_INTRA_PING_MEASUREMENT_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_URI_KEY = CoreSystemService.QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_URI_KEY = CoreSystemService.QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_URI_KEY = CoreSystemService.QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	public static final String KEY_CALCULATED_SERVICE_TIME_FRAME = "QoSCalculatedServiceTimeFrame";
	
	private static final Logger logger = LogManager.getLogger(OrchestratorDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CoreCommonConstants.$AUTH_TOKEN_TTL_IN_MINUTES_WD)
	private int tokenDuration;
	
	@Value(CoreCommonConstants.$QOS_ENABLED_WD)
	private boolean qosEnabled;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	public void init() {
		if (qosEnabled) {
			tokenDuration = CoreDefaults.DEFAULT_AUTH_TOKEN_TTL_IN_MINUTES_WITH_QOS_ENABLED;
		}
	}

	//-------------------------------------------------------------------------------------------------
	// The flags can override the corresponding settings in the form
	public ServiceQueryResultDTO queryServiceRegistry(final ServiceQueryFormDTO form, final OrchestrationFlags flags) { 
		logger.debug("queryServiceRegistry started...");
		Assert.notNull(form, "Form is null.");
		Assert.notNull(flags, "Flags is null.");
		
		// overriding settings
		form.setPingProviders(flags.get(Flag.PING_PROVIDERS));
		if (!flags.get(Flag.METADATA_SEARCH)) {
			form.setMetadataRequirements(null);
		}
		
		final List<AddressType> providerAddressTypeRequirements = calculateAddressTypeRequirements(flags);
		if (!Utilities.isEmpty(providerAddressTypeRequirements)) {
			form.setProviderAddressTypeRequirements(providerAddressTypeRequirements);
		}
		
		final UriComponents queryUri = getQueryUri();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
		
		return response.getBody();
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryResultListDTO multiQueryServiceRegistry(final List<ServiceQueryFormDTO> forms) { 
		logger.debug("multiQueryServiceRegistry started...");
		Assert.notNull(forms, "Form list is null.");
		Assert.isTrue(!forms.isEmpty(), "Form list is empty.");
		
		final UriComponents queryUri = getMultiQueryUri();
		final ResponseEntity<ServiceQueryResultListDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultListDTO.class, new ServiceQueryFormListDTO(forms));
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO queryServiceRegistryBySystemId(final long consumerSystemId) {
		logger.debug("queryByIdServiceRegistry started...");
		Assert.isTrue(consumerSystemId > 0, "ConsumerSystemId is less than 1.");
		
		final UriComponents queryBySystemIdUri = getQueryBySystemIdUri().expand(Map.of(CoreCommonConstants.COMMON_FIELD_NAME_ID, String.valueOf(consumerSystemId)));
		final ResponseEntity<SystemResponseDTO> response = httpService.sendRequest(queryBySystemIdUri, HttpMethod.GET, SystemResponseDTO.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO queryServiceRegistryBySystemRequestDTO(final SystemRequestDTO consumerSystemRequestDTO) {
		logger.debug("queryServiceRegistryBySystemRequestDTO started...");
		Assert.notNull(consumerSystemRequestDTO, "ConsumerSystemRequestDTO is null.");

		final UriComponents queryBySystemDTOUri = getQueryBySystemDTOUri();
		final ResponseEntity<SystemResponseDTO> response = httpService.sendRequest(queryBySystemDTOUri, HttpMethod.POST, SystemResponseDTO.class, consumerSystemRequestDTO);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<OrchestrationResultDTO> generateAuthTokens(final OrchestrationFormRequestDTO request, final List<OrchestrationResultDTO> orList) {
		logger.debug("generateAuthTokens started...");
		
		Assert.notNull(request, "Request is null.");
		Assert.notNull(orList, "Orchestration result list is null.");
		
		final List<TokenGenHelper> tokenGenHelperList = convertOrchestrationResultListToTokenGenHelperList(orList);
		if (tokenGenHelperList.isEmpty()) {
			return orList;
		}
		
		final UriComponents tokenGenerationUri = getAuthTokenGenerationUri();
		for (final TokenGenHelper helper : tokenGenHelperList) {
			final TokenGenerationRequestDTO payload = new TokenGenerationRequestDTO(request.getRequesterSystem(), request.getRequesterCloud(), helper.getProviders(), helper.getService());
			final ResponseEntity<TokenGenerationResponseDTO> response = httpService.sendRequest(tokenGenerationUri, HttpMethod.POST, TokenGenerationResponseDTO.class, payload);
			
			final TokenGenerationResponseDTO tokenGenerationResult = response.getBody();
			updateOrchestrationResultsWithTokenData(orList, helper.getService(), tokenGenerationResult.getTokenData());
		}
		
		return orList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistryResponseDTO> queryAuthorization(final SystemRequestDTO consumer, final List<ServiceRegistryResponseDTO> providers) {
		logger.debug("queryAuthorization started...");
		
		Assert.notNull(consumer, "consumer is null.");
		Assert.notNull(providers, "providers list is null.");
		
		if (!providers.isEmpty()) {
			final UriComponents checkUri = getAuthIntraCheckUri();
			final long serviceDefinitionId = providers.get(0).getServiceDefinition().getId();
			final List<IdIdListDTO> providerIdsWithInterfaceIds = convertSRResultsToProviderIdListWithInterfaceIds(providers);
			final AuthorizationIntraCloudCheckRequestDTO payload = new AuthorizationIntraCloudCheckRequestDTO(consumer, serviceDefinitionId, providerIdsWithInterfaceIds);
			final ResponseEntity<AuthorizationIntraCloudCheckResponseDTO> response = httpService.sendRequest(checkUri, HttpMethod.POST, AuthorizationIntraCloudCheckResponseDTO.class, payload);
			
			filterProviderListUsingAuthorizationResult(providers, response.getBody());
		}
		
		return providers;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO doInterCloudNegotiation(final ICNRequestFormDTO icnForm) {
		logger.debug("doInterCloudNegotiation started...");
		Assert.notNull(icnForm, "ICNResultDTO is null.");
		
		final UriComponents icnUri = getGatekeeperICNUri();
		final ResponseEntity<ICNResultDTO> response = httpService.sendRequest(icnUri, HttpMethod.POST, ICNResultDTO.class, icnForm);
		
		return response.getBody();
	}

	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO doGlobalServiceDiscovery(final GSDQueryFormDTO gsdForm) {
		logger.debug("doGlobalServiceDiscovery started...");
		Assert.notNull(gsdForm, "GSDPollRequestDTO is null.");
		
		final UriComponents gsdUri = getGatekeeperGSDUri();
		final ResponseEntity<GSDQueryResultDTO> response = httpService.sendRequest(gsdUri, HttpMethod.POST, GSDQueryResultDTO.class, gsdForm);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysResponseDTO getCloudsWithExclusiveGatewayAndPublicRelays(final String operator, final String name) {
		logger.debug("getCloudsWithGatewayAndPublicRelays started...");
		Assert.isTrue(!Utilities.isEmpty(operator), "operator is null or empty");
		Assert.isTrue(!Utilities.isEmpty(name), "name is null or empty");
		
		final UriComponents uri = getGatekeeperGetCloudUri(operator, name);
		final ResponseEntity<CloudWithRelaysResponseDTO> response = httpService.sendRequest(uri, HttpMethod.GET, CloudWithRelaysResponseDTO.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementResponseDTO getIntraPingMeasurement(final long systemId) {
		logger.debug("getPingMeasurement started...");
		
		final UriComponents pingUri = getQosMonitorIntraPingMeasurementUri(systemId);
		final ResponseEntity<QoSIntraPingMeasurementResponseDTO> response = httpService.sendRequest(pingUri, HttpMethod.GET, QoSIntraPingMeasurementResponseDTO.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementResponseDTO getIntraPingMedianMeasurement(final QoSMeasurementAttribute attribute) {
		logger.debug("getIntraPingMedianMeasurement started...");
		
		final UriComponents uri = getQosMonitorIntraMedianPingMeasurementUri(attribute.name());
		final ResponseEntity<QoSIntraPingMeasurementResponseDTO> response = httpService.sendRequest(uri, HttpMethod.GET, QoSIntraPingMeasurementResponseDTO.class);
		
		return response.getBody();		
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectPingMeasurementResponseDTO getInterDirectPingMeasurement(final CloudSystemFormDTO request) {
		logger.debug("getInterDirectPingMeasurement started...");
		Assert.notNull(request, "CloudSystemFormDTO is null.");
		
		final UriComponents uri = getQosMonitorInterDirectPingMeasurementUri();
		final ResponseEntity<QoSInterDirectPingMeasurementResponseDTO> response = httpService.sendRequest(uri, HttpMethod.POST, QoSInterDirectPingMeasurementResponseDTO.class, request);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementListResponseDTO getInterRelayEchoMeasurement(final CloudRequestDTO request) {
		logger.debug("getInterRelayEchoMeasurement started...");
		Assert.notNull(request, "CloudRequestDTO is null.");
		
		final UriComponents uri = getQosMonitorInterRelayEchoMeasurementUri();
		final ResponseEntity<QoSInterRelayEchoMeasurementListResponseDTO> response = httpService.sendRequest(uri, HttpMethod.POST, QoSInterRelayEchoMeasurementListResponseDTO.class, request);
		
		return response.getBody();
	}
 	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryUri() {
		logger.debug("getQueryUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getMultiQueryUri() {
		logger.debug("getMultiQueryUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Multi Query URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Multi Query URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryBySystemIdUri() {
		logger.debug("getQueryByIdUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_ID_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_ID_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query By Id URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query By Id URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryBySystemDTOUri() {
		logger.debug("getQueryBySystemDTOUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query By DTO URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query By DTO URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthTokenGenerationUri() {
		logger.debug("getAuthTokenGenerationUri started...");
		
		if (arrowheadContext.containsKey(AUTH_TOKEN_GENERATION_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_TOKEN_GENERATION_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find token generation URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find token generation URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthIntraCheckUri() {
		logger.debug("getAuthIntraCheckUri started...");
		
		if (arrowheadContext.containsKey(AUTH_INTRA_CHECK_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_INTRA_CHECK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find authorization check URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find authorization check URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperGSDUri() {
		logger.debug("getGatekeeperGSDUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_INIT_GSD_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_INIT_GSD_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find gatekeeper init_gsd URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find gatekeeper init_gsd URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperICNUri() {
		logger.debug("getGatekeeperICNUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_INIT_ICN_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_INIT_ICN_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find gatekeeper init_icn URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find gatekeeper init_icn URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperGetCloudUri(final String operator, final String name) {
		logger.debug("getGatekeeperGetCloudUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_GET_CLOUD_URI_KEY)) {
			try {
				final UriComponents uri = (UriComponents) arrowheadContext.get(GATEKEEPER_GET_CLOUD_URI_KEY);
				return uri.expand(operator, name);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find gatekeeper get_cloud URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find gatekeeper get_cloud URI.");		
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQosMonitorIntraPingMeasurementUri(final long id) {
		logger.debug("getQosMonitorIntraPingMeasurementUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_INTRA_PING_MEASUREMENT_URI_KEY)) {
			try {
				final UriComponents uri = (UriComponents) arrowheadContext.get(QOSMONITOR_INTRA_PING_MEASUREMENT_URI_KEY);
				return uri.expand(id);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find QoS Monitor ping/measurement URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find QoS Monitor ping/measurement URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQosMonitorIntraMedianPingMeasurementUri(final String attribute) {
		logger.debug("getQosMonitorIntraMedianPingMeasurementUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_URI_KEY)) {
			try {
				final UriComponents uri = (UriComponents) arrowheadContext.get(QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_URI_KEY);
				return uri.expand(attribute);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find QoS Monitor intra ping median URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find QoS Monitor intra ping median URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQosMonitorInterDirectPingMeasurementUri() {
		logger.debug("getQosMonitorInterDirectPingMeasurementUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find QoS Monitor inter direct ping URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find QoS Monitor inter direct ping URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQosMonitorInterRelayEchoMeasurementUri() {
		logger.debug("getQosMonitorInterRelayEchoMeasurementUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find QoS Monitor inter relay echo URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find QoS Monitor inter relay echo URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<TokenGenHelper> convertOrchestrationResultListToTokenGenHelperList(final List<OrchestrationResultDTO> orList) {
		logger.debug("convertOrchestrationResultListToTokenGenHelperList started...");
		
		final Map<String,TokenGenHelper> serviceMap = new HashMap<>();
		
		for (final OrchestrationResultDTO result : orList) {
			if (result.getSecure() == ServiceSecurityType.TOKEN) {
				final String serviceDefinition = result.getService().getServiceDefinition();
				
				TokenGenHelper helper;
				if (serviceMap.containsKey(serviceDefinition)) {
					helper = serviceMap.get(serviceDefinition);
				} else {
					helper = new TokenGenHelper(serviceDefinition);
					serviceMap.put(serviceDefinition, helper);
				}
				
				helper.getProviders().add(createTokenGenerationProvider(result));
			}
		}
		
		return new ArrayList<>(serviceMap.values());
	}
	
	//-------------------------------------------------------------------------------------------------
	private TokenGenerationProviderDTO createTokenGenerationProvider(final OrchestrationResultDTO result) {
		logger.debug("createTokenGenerationProvider started...");
		
		int tokenDuration = this.tokenDuration;
		if (result.getMetadata() != null && result.getMetadata().containsKey(KEY_CALCULATED_SERVICE_TIME_FRAME)) {
			try {
				tokenDuration = Integer.parseInt(result.getMetadata().get(KEY_CALCULATED_SERVICE_TIME_FRAME));
			} catch (final NumberFormatException ex) {
				logger.debug(ex.getMessage());
				logger.trace("Stacktrace:", ex);
			}
		}
		
		return new TokenGenerationProviderDTO(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(result.getProvider()), tokenDuration,
											  convertServiceInterfaceListToServiceInterfaceNameList(result.getInterfaces()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S1612")
	private List<String> convertServiceInterfaceListToServiceInterfaceNameList(final List<ServiceInterfaceResponseDTO> intfs) {
		logger.debug("convertServiceInterfaceListToServiceInterfaceNameList started...");
		
		if (intfs == null) {
			return List.of();
		}
		
		return intfs.stream().map(dto -> dto.getInterfaceName()).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void updateOrchestrationResultsWithTokenData(final List<OrchestrationResultDTO> orList, final String serviceDefinition, final List<TokenDataDTO> tokenDataList) {
		logger.debug("updateOrchestrationResultsWithTokenData started...");
		
		for (final OrchestrationResultDTO result : orList) {
			if (result.getService().getServiceDefinition().equals(serviceDefinition)) {
				for (final TokenDataDTO tokenData : tokenDataList) {
					if (systemEquals(result.getProvider(), tokenData.getProviderName(), tokenData.getProviderAddress(), tokenData.getProviderPort())) {
						result.setAuthorizationTokens(tokenData.getTokens());
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean systemEquals(final SystemResponseDTO system, final String systemName, final String systemAddress, final int systemPort) {
		return system.getSystemName().equals(systemName) && system.getAddress().equals(systemAddress) && system.getPort() == systemPort;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<IdIdListDTO> convertSRResultsToProviderIdListWithInterfaceIds(final List<ServiceRegistryResponseDTO> providers) {
		logger.debug("convertSRResultsToProviderIdListWithInterfaceIds started...");
		
		final List<IdIdListDTO> result = new ArrayList<>(providers.size());
		for (final ServiceRegistryResponseDTO srEntry : providers) {
			final IdIdListDTO dto = new IdIdListDTO(srEntry.getProvider().getId(), convertServiceInterfaceListToServiceInterfaceIdList(srEntry.getInterfaces()));
			result.add(dto);
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S1612")
	private List<Long> convertServiceInterfaceListToServiceInterfaceIdList(final List<ServiceInterfaceResponseDTO> intfs) {
		logger.debug("convertServiceInterfaceListToServiceInterfaceIdList started...");
		
		if (intfs == null) {
			return List.of();
		}
		
		return intfs.stream().map(dto -> dto.getId()).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	// method may change providers list
	private void filterProviderListUsingAuthorizationResult(final List<ServiceRegistryResponseDTO> providers, final AuthorizationIntraCloudCheckResponseDTO authResult) {
		logger.debug("filterProviderListUsingAuthorizationResult started...");

		if (authResult.getAuthorizedProviderIdsWithInterfaceIds().isEmpty()) {
			// consumer has no access to any of the specified providers
			providers.clear();
		} else {
			final Map<Long,List<Long>> authMap = convertAuthorizationResultsToMap(authResult.getAuthorizedProviderIdsWithInterfaceIds());
			for (final Iterator<ServiceRegistryResponseDTO> it = providers.iterator(); it.hasNext();) {
				final ServiceRegistryResponseDTO srEntry = it.next();
				if (authMap.containsKey(srEntry.getProvider().getId())) {
					final List<Long> authorizedInterfaceIds = authMap.get(srEntry.getProvider().getId());
					srEntry.getInterfaces().removeIf(e -> !authorizedInterfaceIds.contains(e.getId()));
				} else {
					it.remove();
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,List<Long>> convertAuthorizationResultsToMap(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		logger.debug("convertAuthorizationResultsToMap started...");

		return authorizedProviderIdsWithInterfaceIds.stream().collect(Collectors.toMap(e -> e.getId(), 
																					   e -> e.getIdList()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AddressType> calculateAddressTypeRequirements(final OrchestrationFlags flags) {
		logger.debug("calculateAddressTypeRequirements started...");
		
		final Set<AddressType> result = new HashSet<>();
		if (flags.get(Flag.ONLY_IPV4_ADDRESS_RESPONSE)) {
			result.add(AddressType.IPV4);
		}
		
		if (flags.get(Flag.ONLY_IPV6_ADDRESS_RESPONSE)) {
			result.add(AddressType.IPV6);
		}
		
		if (flags.get(Flag.ONLY_IP_ADDRESS_RESPONSE)) {
			result.add(AddressType.IPV4);
			result.add(AddressType.IPV6);
		}

		return result.isEmpty() ? null : new ArrayList<>(result);
	}
	
	//=================================================================================================
	// nested classes

	//-------------------------------------------------------------------------------------------------
	private static class TokenGenHelper {
		
		//=================================================================================================
		// members
		
		private final String service;
		private final List<TokenGenerationProviderDTO> providers = new ArrayList<>();
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public TokenGenHelper(final String service) {
			this.service = service;
		}

		//-------------------------------------------------------------------------------------------------
		public String getService() { return service; }
		public List<TokenGenerationProviderDTO> getProviders() { return providers; 	}
	}
}