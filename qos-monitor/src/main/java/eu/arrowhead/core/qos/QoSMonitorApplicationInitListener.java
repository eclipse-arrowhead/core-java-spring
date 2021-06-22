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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

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
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.measurement.properties.MonitorProviderType;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventBufferElement;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventCollectorTask;
import eu.arrowhead.core.qos.service.ping.monitor.PingMonitorManager;
import eu.arrowhead.core.qos.service.ping.monitor.impl.DefaultExternalPingMonitor;
import eu.arrowhead.core.qos.service.ping.monitor.impl.DummyPingMonitor;
import eu.arrowhead.core.qos.service.ping.monitor.impl.OrchestratedExternalPingMonitor;

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
	@Bean(QosMonitorConstants.EVENT_QUEUE)
	public LinkedBlockingQueue<EventDTO> getEventQueue() {
		logger.debug("getEventQueue started...");

		return new LinkedBlockingQueue<EventDTO>();
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(QosMonitorConstants.EVENT_BUFFER)
	public ConcurrentHashMap<UUID, PingEventBufferElement> getEventBuffer() {
		logger.debug("getEventBuffer started...");

		return new ConcurrentHashMap<UUID, PingEventBufferElement>();
	}

	//-------------------------------------------------------------------------------------------------
	@Bean(QosMonitorConstants.EVENT_COLLECTOR)
	public PingEventCollectorTask getEventCollector() {
		logger.debug("getEventCollector started...");

		return new PingEventCollectorTask();
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
			return new OrchestratedExternalPingMonitor();
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

		final List<CoreSystemService> result = new ArrayList<>(8);

		if (gatekeeperIsPresent) {
			result.add(CoreSystemService.GATEKEEPER_PULL_CLOUDS); 
			result.add(CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES);
			result.add(CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES);
			result.add(CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE);
			result.add(CoreSystemService.GATEKEEPER_GET_CLOUD_SERVICE);
		}

		if (monitorType.equals(MonitorProviderType.DEFAULTEXTERNAL)) {
			result.add(CoreSystemService.EVENT_SUBSCRIBE_SERVICE);
			result.add(CoreSystemService.EVENT_UNSUBSCRIBE_SERVICE);
		}else if (monitorType.equals(MonitorProviderType.ORCHESTRATEDEXTERNAL)) {
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
		standaloneMode = context.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);

		if (!standaloneMode) {
			final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;

			final UriComponents queryAll = createQueryAllUri(scheme);
			context.put(CoreCommonConstants.SR_QUERY_ALL, queryAll);

			final PingMonitorManager pingManager = appContext.getBean(CoreCommonConstants.PING_MONITOR, PingMonitorManager.class);
			pingManager.init();
		}

	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryAllUri(final String scheme) {
		logger.debug("createQueryAllUri started...");

		final String registyUriStr = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI;

		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registyUriStr);
	}

}