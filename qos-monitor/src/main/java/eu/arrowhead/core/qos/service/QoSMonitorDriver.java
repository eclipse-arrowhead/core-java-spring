/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.service;

import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.QosMonitorConstants;

@Service
public class QoSMonitorDriver {

	//=================================================================================================
	// members

	private static final String GATEKEEPER_PULL_CLOUDS_URI_KEY = CoreSystemService.GATEKEEPER_PULL_CLOUDS.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI_KEY = CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_COLLECT_ACCESS_TYPES_URI_KEY = CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_GET_CLOUD_URI_KEY = CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_INIT_RELAY_TEST_URI_KEY = CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String EVENT_SUBSCRIBE_URI_KEY = CoreSystemService.EVENT_SUBSCRIBE_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String EVENT_UNSUBSCRIBE_URI_KEY = CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;

	private static final Logger logger = LogManager.getLogger(QoSMonitorDriver.class);

	@Autowired
	private HttpService httpService;

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String coreSystemName;

	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String coreSystemAddress;

	@Value(CoreCommonConstants.$SERVER_PORT)
	private int coreSystemPort;

	@Value(QosMonitorConstants.$QOS_MONITOR_REQUEST_MAX_RETRY_WD)
	private int MAX_RETRIES;

	@Value(QosMonitorConstants.$QOS_MONITOR_REQUEST_SLEEP_PERIOD_WD)
	private int SLEEP_PERIOD;

	private SystemRequestDTO requesterSystem;

	private Map<QosMonitorEventType, MultiValueMap<String, String>> unsubscribeParams;

	private UriComponents eventHandlerUnsubscribeUri;

	private UriComponents eventHandlerSubscribeUri;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO queryServiceRegistryAll() {
		logger.debug("queryServiceRegistryAll started...");

		try {
			final UriComponents queryBySystemDTOUri = getQueryAllUri();
			final ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(queryBySystemDTOUri, HttpMethod.GET, ServiceRegistryListResponseDTO.class);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysListResponseDTO queryGatekeeperAllCloud() {
		logger.debug("queryGatekeeperAllCloud started...");

		try {
			final UriComponents queryByAllCloudsUri = getGatekeeperAllCloudsUri();
			final ResponseEntity<CloudWithRelaysAndPublicRelaysListResponseDTO> response = httpService.sendRequest(queryByAllCloudsUri, HttpMethod.GET, CloudWithRelaysAndPublicRelaysListResponseDTO.class);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public CloudAccessListResponseDTO queryGatekeeperCloudAccessTypes(final List<CloudRequestDTO> cloudList) {
		logger.debug("queryGatekeeperCloudAccessTypes started...");

		Assert.notNull(cloudList, "CloudRequestDTO list is null.");
		
		try {
			final UriComponents cloudAccessTypesUri = getGatekeeperCloudAccessTypesURI();
			final ResponseEntity<CloudAccessListResponseDTO> response = httpService.sendRequest(cloudAccessTypesUri, HttpMethod.POST, CloudAccessListResponseDTO.class, cloudList);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO queryGatekeeperAllSystemAddresses(final CloudRequestDTO cloud) {
		logger.debug("queryGatekeeperAllSystemAddresses started...");

		Assert.notNull(cloud, "CloudRequestDTO is null.");
		
		try {
			final UriComponents queryByAllSystemsUri = getGatekeeperAllSystemsUri();
			final ResponseEntity<SystemAddressSetRelayResponseDTO> response = httpService.sendRequest(queryByAllSystemsUri, HttpMethod.POST, SystemAddressSetRelayResponseDTO.class, cloud);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysResponseDTO queryGatekeeperCloudInfo(final String operator, final String name) {
		logger.debug("queryGatekeeperCloudInfo started...");
		
		Assert.isTrue(!Utilities.isEmpty(operator), "Operator is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(name), "Name is null or blank.");
		
		try {
			final UriComponents uri = getGatekeeperGetCloudUri(operator, name);
			final ResponseEntity<CloudWithRelaysResponseDTO> response = httpService.sendRequest(uri, HttpMethod.GET, CloudWithRelaysResponseDTO.class);
			
			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void requestGatekeeperInitRelayTest(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("requestGatekeeperInitRelayTest started...");
		Assert.notNull(request, "QoSRelayTestProposalRequestDTO is null or blank.");
		
		try {
			final UriComponents uri = getGatekeeperInitRelayTestUri();
			httpService.sendRequest(uri, HttpMethod.POST, Void.class, request);
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestACK requestExternalPingMonitorService(final UriComponents externalPingMonitorUri, final IcmpPingRequestDTO request) {
		logger.debug("requestExternalPingMonitorService started...");

		Assert.notNull(request, "IcmpPingRequest is null.");
		Assert.notNull(externalPingMonitorUri, "externalPingMonitorUri is null.");

		try {
			final ResponseEntity<IcmpPingRequestACK> response = httpService.sendRequest(externalPingMonitorUri, HttpMethod.POST, IcmpPingRequestACK.class, request);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO queryOrchestrator(final OrchestrationFormRequestDTO form) {
		logger.debug("queryOrchestrator started...");

		Assert.notNull(form, "form is null.");

		final UriComponents orchestrationProcessUri = getOrchestrationProcessUri();
		try {

			final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(orchestrationProcessUri, HttpMethod.POST, OrchestrationResponseDTO.class, form);

			return response.getBody();

		} catch (final Exception ex) {
			logger.debug("QoS Monitor can't access Orchestrator : " + ex.getMessage());

			throw new ArrowheadException("QoS Monitor can't access Orchestrator.");
		}

	}

	//-------------------------------------------------------------------------------------------------
	public void checkPingMonitorProviderEchoUri(final UriComponents echoUri) {
		logger.debug("checkPingMonitorProviderEchoUri started...");

		Assert.notNull(echoUri, "echoUri is null.");

		int count = 0;

		while ( count < MAX_RETRIES) {

			try {

				httpService.sendRequest(echoUri, HttpMethod.GET, String.class);

				return;

			} catch (final Exception ex) {
				logger.debug("QoS Monitor can't access External Qos Monitor provider at : " + echoUri + ", " + ex.getMessage());
				logger.debug(ex);

				rest();
				count++;

			}
		}

		throw new ArrowheadException("QoS Monitor can't access External Qos Monitor provider at : " + echoUri);
	}

	//-------------------------------------------------------------------------------------------------
	public void subscribeToExternalPingMonitorEvents(final SystemRequestDTO pingMonitorProvider) {
		logger.debug("subscribeToExternalPingMonitorEvents started...");

		int count = 0;
		boolean subscribedToAll = false; 

		while ( !subscribedToAll && count < MAX_RETRIES) {

			try {

				final SubscriptionRequestDTO subscriptionRequest = getSubscriptionRequestDTOForExternalPingMonitor(pingMonitorProvider);
				final UriComponents subscriptionUri = getEventHandlerSubscribeUri();

				for (final QosMonitorEventType externalPingMonitorEventType : QosMonitorEventType.values()) {
					subscriptionRequest.setEventType(externalPingMonitorEventType.name());

					httpService.sendRequest(subscriptionUri, HttpMethod.POST, SubscriptionResponseDTO.class, subscriptionRequest);

				}

				subscribedToAll = true;

			} catch (final Exception ex) {
				logger.debug("QoS Monitor can't access EventHandler : " + ex.getMessage());

				eventHandlerSubscribeUri = null;

				count++;
				if (count < MAX_RETRIES) {
					logger.debug("Retrying to access EventHandler.");

					rest();
				}
			}
		}

		if (!subscribedToAll) {

			eventHandlerSubscribeUri = null;
			throw new ArrowheadException("QoS Monitor can't subscribe to required events.");
		}

	}

	//-------------------------------------------------------------------------------------------------
	public void unsubscribeFromPingMonitorEvents() {
		logger.debug("unSubscribeFromPingMonitorEvents started...");

		int count = 0;
		boolean unsubscribedFromAll = false; 

		while ( !unsubscribedFromAll && count < MAX_RETRIES) {
			try {
				for (final QosMonitorEventType externalPingMonitorEventType : QosMonitorEventType.values()) {

					final UriComponents unsubscriptionUri = getEventHandlerUnsubscribeUri(getUnsubscribeParams().get(externalPingMonitorEventType));

					httpService.sendRequest(unsubscriptionUri, HttpMethod.DELETE, Void.class, null);

				}

				unsubscribedFromAll = true;

			} catch (final Exception ex) {
				logger.debug("QoS Monitor can't access EventHandler: " + ex.getMessage());

				eventHandlerUnsubscribeUri = null;

				count++;
				if (count < MAX_RETRIES) {
					logger.debug("Retrying to access EventHandler.");

					rest();
				}
			}
		}

		if (!unsubscribedFromAll) {

			logger.debug("QoS Monitor can't unsubscribe from some required events.");
		}

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryAllUri() {
		logger.debug("getQueryUri started...");

		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
			}
		} else {
			throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperAllCloudsUri() {
		logger.debug("getGatekeeperAllCloudsUri started...");

		if (arrowheadContext.containsKey(GATEKEEPER_PULL_CLOUDS_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_PULL_CLOUDS_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find gatekeeper all_clouds URI.");
			}
		}

		throw new ArrowheadException("QoS Mointor can't find gatekeeper all_clouds URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperCloudAccessTypesURI() {
		logger.debug("getGatekeeperGatewayIsMandatoryUri started...");

		if (arrowheadContext.containsKey(GATEKEEPER_COLLECT_ACCESS_TYPES_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_COLLECT_ACCESS_TYPES_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find gatekeeper cloud access types URI.");
			}
		}
		
		throw new ArrowheadException("QoS Mointor can't find gatekeeper cloud access types URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperAllSystemsUri() {
		logger.debug("getGatekeeperAllSystemsUri started...");

		if (arrowheadContext.containsKey(GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find gatekeeper all_systems URI.");
			}
		}

		throw new ArrowheadException("QoS Mointor can't find gatekeeper all_systems URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperGetCloudUri(final String operator, final String name) {
		logger.debug("getGatekeeperGetCloudUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_GET_CLOUD_URI_KEY)) {
			try {
				final UriComponents uriTemplate = (UriComponents) arrowheadContext.get(GATEKEEPER_GET_CLOUD_URI_KEY);
				return uriTemplate.expand(operator, name);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Monitor can't find gatekeeper get cloud URI.");
			}
		}
		
		throw new ArrowheadException("QoS Monitor can't find gatekeeper get cloud URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperInitRelayTestUri() {
		logger.debug("getGatekeeperIniitRelayTestUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_INIT_RELAY_TEST_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_INIT_RELAY_TEST_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Monitor can't find gatekeeper init_relay_test URI.");
			}
		}
		
		throw new ArrowheadException("QoS Monitor can't find gatekeeper init_relay_test URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getOrchestrationProcessUri() {
		logger.debug("getOrchestrationProcessUri started...");

		if (arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(ORCHESTRATION_PROCESS_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Monitor can't find orchestration process URI.");
			}
		}

		throw new ArrowheadException("QoS Monitor can't find orchestration process URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getEventHandlerSubscribeUri() {
		logger.debug("getEventHandlerSubscribeUri started...");

		if (eventHandlerSubscribeUri != null) {

			return eventHandlerSubscribeUri;

		}else {

			if (arrowheadContext.containsKey(EVENT_SUBSCRIBE_URI_KEY)) {
				try {

					eventHandlerSubscribeUri = (UriComponents) arrowheadContext.get(EVENT_SUBSCRIBE_URI_KEY);

					return eventHandlerSubscribeUri;
	
				} catch (final ClassCastException ex) {

					eventHandlerSubscribeUri = null;
					throw new ArrowheadException("QoS Monitor can't find Event Handler subscribe URI.");
				}
			}

			eventHandlerSubscribeUri = null;
			throw new ArrowheadException("QoS Monitor can't find Event Handler subscribe URI.");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getEventHandlerUnsubscribeUri(final MultiValueMap<String, String> multiValueMap) {
		logger.debug("getEventHandlerUnsubscribeUri started...");

		if(eventHandlerUnsubscribeUri != null) {

			return eventHandlerUnsubscribeUri;

		}else {
			if (arrowheadContext.containsKey(EVENT_UNSUBSCRIBE_URI_KEY)) {
				try {

					final UriComponents uri = (UriComponents) arrowheadContext.get(EVENT_UNSUBSCRIBE_URI_KEY);
					eventHandlerUnsubscribeUri = Utilities.createURI(uri.getScheme(), uri.getHost(), uri.getPort(), multiValueMap, uri.getPath());

					return eventHandlerUnsubscribeUri;

				} catch (final ClassCastException ex) {

					eventHandlerUnsubscribeUri = null;
					throw new ArrowheadException("QoS Monitor can't find Event Handler unsubscribe URI.");
				}
			}

			eventHandlerUnsubscribeUri = null;
			throw new ArrowheadException("QoS Monitor can't find Event Handler unsubscribe URI.");
		}

	}

	//-------------------------------------------------------------------------------------------------
	private SubscriptionRequestDTO getSubscriptionRequestDTOForExternalPingMonitor(final SystemRequestDTO provider) {
		logger.debug("getSubscriptionRequestDTOForExternalPingMonitor started...");

		final SubscriptionRequestDTO requestTemplate = new SubscriptionRequestDTO();
		requestTemplate.setMatchMetaData(false);
		requestTemplate.setNotifyUri(CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI);
		requestTemplate.setSubscriberSystem(getQosMonitorSystemRequestDTO());

		if(provider != null) {
			requestTemplate.setSources(Set.of(provider));
		}

		return requestTemplate;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getQosMonitorSystemRequestDTO() {
		logger.debug("getQosMonitorSystemRequestDTO started...");

		if(requesterSystem == null) {
			requesterSystem = new SystemRequestDTO();
			requesterSystem.setSystemName(coreSystemName);
			requesterSystem.setAddress(coreSystemAddress);
			requesterSystem.setPort(coreSystemPort);
			requesterSystem.setMetadata(null);
			if (sslEnabled) {

				Assert.isTrue(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY), "Server Public key is not in Context");

				final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
				requesterSystem.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
			}
		}

		return requesterSystem;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<QosMonitorEventType, MultiValueMap<String, String>> getUnsubscribeParams() {
		logger.debug("getUnsubscribeParams started...");

		if (unsubscribeParams == null ) {

			final MultiValueMap<String, String> unsubscribeReceivedMonitoringequestParams = new LinkedMultiValueMap<>();
			unsubscribeReceivedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE, QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
			unsubscribeReceivedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME, coreSystemName);
			unsubscribeReceivedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS, coreSystemAddress);
			unsubscribeReceivedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT, String.valueOf(coreSystemPort));

			final MultiValueMap<String, String> unsubscribeStartMonitoringequestParams = new LinkedMultiValueMap<>();
			unsubscribeStartMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE, QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name());
			unsubscribeStartMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME, coreSystemName);
			unsubscribeStartMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS, coreSystemAddress);
			unsubscribeStartMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT, String.valueOf(coreSystemPort));

			final MultiValueMap<String, String> unsubscribeFinishedMonitoringequestParams = new LinkedMultiValueMap<>();
			unsubscribeFinishedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE, QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name());
			unsubscribeFinishedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME, coreSystemName);
			unsubscribeFinishedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS, coreSystemAddress);
			unsubscribeFinishedMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT, String.valueOf(coreSystemPort));

			final MultiValueMap<String, String> unsubscribeInterruptMonitoringequestParams = new LinkedMultiValueMap<>();
			unsubscribeInterruptMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE, QosMonitorEventType.INTERRUPTED_MONITORING_MEASUREMENT.name());
			unsubscribeInterruptMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME, coreSystemName);
			unsubscribeInterruptMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS, coreSystemAddress);
			unsubscribeInterruptMonitoringequestParams.add(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT, String.valueOf(coreSystemPort));

			unsubscribeParams = Map.of(
					QosMonitorEventType.RECEIVED_MONITORING_REQUEST, unsubscribeReceivedMonitoringequestParams,
					QosMonitorEventType.STARTED_MONITORING_MEASUREMENT, unsubscribeStartMonitoringequestParams,
					QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT, unsubscribeFinishedMonitoringequestParams,
					QosMonitorEventType.INTERRUPTED_MONITORING_MEASUREMENT, unsubscribeInterruptMonitoringequestParams);

		}

		return unsubscribeParams;
	}

	//-------------------------------------------------------------------------------------------------
	private void rest() {
		try {
			Thread.sleep(SLEEP_PERIOD);
		} catch (final InterruptedException ex) {
			logger.warn(ex.getMessage());
		}
	}
}