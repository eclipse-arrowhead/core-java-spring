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

package eu.arrowhead.core.qos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.measurement.properties.MonitorProviderType;
import eu.arrowhead.core.qos.service.event.queue.FinishedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.InteruptedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.ReceivedMonitoringRequestEventQueue;
import eu.arrowhead.core.qos.service.event.queue.StartedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.ping.monitor.PingMonitorManager;
import eu.arrowhead.core.qos.service.ping.monitor.impl.DefaultExternalPingMonitor;
import eu.arrowhead.core.qos.service.ping.monitor.impl.DummyPingMonitor;
import eu.arrowhead.core.qos.service.ping.monitor.impl.OrchetratedExternalPingMonitor;

@Component
public class QoSMonitorApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// members

	@Value(CoreCommonConstants.$QOS_IS_GATEKEEPER_PRESENT_WD)
	private boolean gatekeeperIsPresent;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_TYPE_WD)
	private MonitorProviderType monitorType;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean(QosMonitorConstants.RECEIVED_MONITORING_REQUEST_QUEUE)
	public ReceivedMonitoringRequestEventQueue getReceivedMonitoringRequestEventQueue() {
		return new ReceivedMonitoringRequestEventQueue();
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_QUEUE)
	public StartedMonitoringMeasurementEventQueue getStartedMonitoringMeasurementEventQueue() {
		return new StartedMonitoringMeasurementEventQueue();
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(QosMonitorConstants.INTERUPTED_MONITORING_MEASUREMENT_QUEUE)
	public InteruptedMonitoringMeasurementEventQueue getInteruptedMonitoringMeasurementEventQueue() {
		return new InteruptedMonitoringMeasurementEventQueue();
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_QUEUE)
	public FinishedMonitoringMeasurementEventQueue getFinishedMonitoringMeasurementEventQueue() {
		return new FinishedMonitoringMeasurementEventQueue();
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.PING_MONITOR)
	public PingMonitorManager getPingMonitor() {
		logger.debug("getPingMonitor started...");

		switch (monitorType) {
		case DUMMY:
			return new DummyPingMonitor();
		case DEFAULTEXTERNAL:
			return new DefaultExternalPingMonitor();
		case ORCHESTRATEDEXTERNAL:
			return new OrchetratedExternalPingMonitor();
		default:
			throw new InvalidParameterException("Not implemented monitor type: " + monitorType.name());
		}

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		logger.debug("getRequiredCoreSystemServiceUris started...");

		final List<CoreSystemService> result = new ArrayList<>(5);

		if (gatekeeperIsPresent) {
			result.add(CoreSystemService.GATEKEEPER_PULL_CLOUDS); 
			result.add(CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES);
			result.add(CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES);
			result.add(CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE);
			result.add(CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE);
		}if (monitorType.equals(MonitorProviderType.DEFAULTEXTERNAL)) {
			result.add(CoreSystemService.EVENT_SUBSCRIBE_SERVICE);
			result.add(CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE);
		}if (monitorType.equals(MonitorProviderType.ORCHESTRATEDEXTERNAL)) {
			result.add(CoreSystemService.ORCHESTRATION_SERVICE);
			result.add(CoreSystemService.EVENT_SUBSCRIBE_SERVICE);
			result.add(CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE);
		}

		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		final ApplicationContext appContext = event.getApplicationContext();
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);

		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;

		final UriComponents queryAll = createQueryAllUri(scheme);
		context.put(CoreCommonConstants.SR_QUERY_ALL, queryAll);

	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryAllUri(final String scheme) {
		logger.debug("createQueryAllUri started...");

		final String registyUriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI;

		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registyUriStr);
	}

}