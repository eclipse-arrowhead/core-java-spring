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
import java.util.concurrent.TimeUnit;

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
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingRequest;
import eu.arrowhead.core.qos.dto.IcmpPingRequestACK;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

@Component
public class QoSMonitorDriver {

	//=================================================================================================
	// members

	private static final long SLEEP_PERIOD = TimeUnit.SECONDS.toMillis(15);
	private static final int MAX_RETRIES = 10;

	private static final String GATEKEEPER_PULL_CLOUDS_URI_KEY = CoreSystemService.GATEKEEPER_PULL_CLOUDS.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_URI_KEY = CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_COLLECT_ACCESS_TYPES_URI_KEY = CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_GET_CLOUD_URI_KEY = CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_INIT_RELAY_TEST_URI_KEY = CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String EVENT_SUBSCRIBE_URI_KEY = CoreSystemService.EVENT_SUBSCRIBE_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String EVENT_UNSUBSCRIBE_URI_KEY = CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;

	public static final String KEY_CALCULATED_SERVICE_TIME_FRAME = "QoSCalculatedServiceTimeFrame";

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_ADDRESS_WD)
	private String fixedExternalPingMonitorAddress;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_PORT_WD)
	private int fixedExternalPingMonitorPort;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_PATH_WD)
	private String fixedExternalPingMonitorPath;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_SCHEME_WD)
	private String fixedExternalPingMonitorScheme;

	private static final Logger logger = LogManager.getLogger(QoSMonitorDriver.class);

	@Autowired
	private HttpService httpService;

	@Autowired
	protected SSLProperties sslProperties;

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String coreSystemName;

	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String coreSystemAddress;
	
	@Value(CoreCommonConstants.$SERVER_PORT)
	private int coreSystemPort;

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
	public IcmpPingRequestACK requestExternalPingMonitorService(final UriComponents externalPingMonitorUri, final IcmpPingRequest request) {
		logger.debug("requestExternalPingMonitorService started...");

		Assert.notNull(request, "IcmpPingRequest is null.");
		Assert.notNull(request, "externalPingMonitorUri is null.");

		try {
			final ResponseEntity<IcmpPingRequestACK> response = httpService.sendRequest(externalPingMonitorUri, HttpMethod.POST, IcmpPingRequestACK.class, request);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestACK requestExternalPingMonitorService(final IcmpPingRequest request) {
		logger.debug("requestExternalPingMonitorService started...");

		Assert.notNull(request, "IcmpPingRequest is null.");

		final UriComponents externalPingMonitorUri = createFixedPingMonitorProviderUri();

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
		final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(orchestrationProcessUri, HttpMethod.POST, OrchestrationResponseDTO.class, form);

		return response.getBody();
	}

	//-------------------------------------------------------------------------------------------------
	public void checkFixedPingMonitorProviderEchoUri() {
		logger.debug("checkFixedPingMonitorProviderEchoUri started...");

		final UriComponents echoUri = createFixedPingMonitorProviderEchoUri();

		int count = 0;
		boolean echo = false;

		while ( !echo && count < MAX_RETRIES) {

			rest();

			try {

				httpService.sendRequest(echoUri, HttpMethod.GET, String.class);

				echo = true;

			} catch (final Exception ex) {
				logger.warn("QoS Monitor can't access Fixed External Qos Monitor provider at : " + echoUri );

				count++;

			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	public void subscribeToExternalPingMonitorEvents() {
		logger.debug("subscribeToExternalPingMonitorEvents started...");

		final SubscriptionRequestDTO subscriptionTemplate = getSubscriptionRequestDTOTemplateForFixedExternalPingMonitor();
		final UriComponents subscriptionUri = getEventHandlerSubscribeUri();

		int count = 0;
		boolean subscribedToAll = false; 

		while ( !subscribedToAll && count < MAX_RETRIES) {
			try {
				for (final QosMonitorEventType externalPingMonitorEventType : QosMonitorEventType.values()) {
					subscriptionTemplate.setEventType(externalPingMonitorEventType.name());

					rest();

					final ResponseEntity<SubscriptionResponseDTO> response = httpService.sendRequest(subscriptionUri, HttpMethod.POST, SubscriptionResponseDTO.class, subscriptionTemplate);

					final SubscriptionResponseDTO subscriptionResponse = response.getBody();
					Assert.isTrue(subscriptionResponse.getEventType().getEventTypeName().equalsIgnoreCase(externalPingMonitorEventType.name()), "Invalid subscriptionResponse event type.");

				}

				subscribedToAll = true;

			} catch (final Exception ex) {
				logger.warn("QoS Monitor can't access EventHandler.");

				count++;

				if (count < MAX_RETRIES) {
					logger.warn("Retrying to access EventHandler.");
				}
			}
		}

		if (!subscribedToAll) {

			throw new ArrowheadException("QoS Monitor can't subscribe to required events.");
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
	private UriComponents createFixedPingMonitorProviderUri() {
		logger.debug("createFixedPingMonitorProviderUri started...");

		return Utilities.createURI(fixedExternalPingMonitorScheme, fixedExternalPingMonitorAddress, fixedExternalPingMonitorPort, fixedExternalPingMonitorPath);
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createFixedPingMonitorProviderEchoUri() {
		logger.debug("createFixedPingMonitorProviderEchoUri started...");

		return Utilities.createURI(fixedExternalPingMonitorScheme, fixedExternalPingMonitorAddress, fixedExternalPingMonitorPort, CommonConstants.ECHO_URI);
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getEventHandlerSubscribeUri() {
		logger.debug("getEventHandlerSubscribeUri started...");

		if (arrowheadContext.containsKey(EVENT_SUBSCRIBE_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(EVENT_SUBSCRIBE_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Monitor can't find Event Handler subscribe URI.");
			}
		}

		throw new ArrowheadException("QoS Monitor can't find Event Handler subscribe URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getEventHandlerUnsubscribeUri() {
		logger.debug("getEventHandlerUnsubscribeUri started...");

		if (arrowheadContext.containsKey(EVENT_UNSUBSCRIBE_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(EVENT_UNSUBSCRIBE_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Monitor can't find Event Handler unsubscribe URI.");
			}
		}

		throw new ArrowheadException("QoS Monitor can't find Event Handler unsubscribe URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private SubscriptionRequestDTO getSubscriptionRequestDTOTemplateForFixedExternalPingMonitor() {
		logger.debug("getSubscriptionRequestDTOForFixedExternalPingMonitor started...");

		final SubscriptionRequestDTO requestTemplate = new SubscriptionRequestDTO();
		requestTemplate.setMatchMetaData(false);
		requestTemplate.setNotifyUri(QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI);
		requestTemplate.setSubscriberSystem(getQosMonitorSystemRequestDTO());

		return requestTemplate;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getQosMonitorSystemRequestDTO() {
		logger.debug("getQosMonitorSystemRequestDTO started...");

		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);

		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName(coreSystemName);
		system.setAddress(coreSystemAddress);
		system.setPort(coreSystemPort);
		system.setMetadata(null);
		if (sslProperties.isSslEnabled()) {
			system.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private void rest() {
		try {
			Thread.sleep(SLEEP_PERIOD);
		} catch (final InterruptedException e) {
			logger.warn(e.getMessage());
		}
	}
}