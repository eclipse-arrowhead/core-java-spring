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

package eu.arrowhead.core.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.service.GatewayService;
import eu.arrowhead.core.gateway.thread.ConsumerSideServerSocketThread;
import eu.arrowhead.core.gateway.thread.ProviderSideSocketThreadHandler;

@Component
public class GatewayApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$GATEWAY_MIN_PORT_WD)
	private int minPort;
	
	@Value(CoreCommonConstants.$GATEWAY_MAX_PORT_WD)
	private int maxPort;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	public ConcurrentMap<String,ActiveSessionDTO> getActiveSessions() {
		return new ConcurrentHashMap<>();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CoreCommonConstants.GATEWAY_ACTIVE_CONSUMER_SIDE_SOCKET_THREAD_MAP)
	public ConcurrentMap<String,ConsumerSideServerSocketThread> getConsumerSideSocketThreads() {
		return new ConcurrentHashMap<>();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CoreCommonConstants.GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP)
	public ConcurrentMap<String,ProviderSideSocketThreadHandler> getProviderSideSocketThreadHandlers() {
		return new ConcurrentHashMap<>();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(name = CoreCommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE)
	public Queue<Integer> getAvailablePorts() {
		final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
		if (minPort <= maxPort) {
			for (int i = minPort; i <= maxPort; ++i) {
				queue.offer(i);
			}
		}
		
		return queue;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");

		if (!sslProperties.isSslEnabled()) {
			throw new ServiceConfigurationError("Gateway can only started in SECURE mode!");
		}
		
		if (minPort > maxPort) {
			throw new ServiceConfigurationError("Available port interval is invalid: [" + minPort + " - " + maxPort + "]");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {
		logger.debug("customDestroy started...");
		
		if (!standaloneMode) {
			@SuppressWarnings("unchecked")
			final ConcurrentMap<String,ActiveSessionDTO> activeSessions = applicationContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentMap.class);
			
			final List<ActiveSessionDTO> sessionsToClose = new ArrayList<>(activeSessions.values());
			
			if (!sessionsToClose.isEmpty()) {
				final GatewayService gatewayService = applicationContext.getBean(GatewayService.class);
				for (final ActiveSessionDTO session : sessionsToClose) {
					try {
						gatewayService.closeSession(session);
						logger.debug("Session closed: {}", session.getQueueId());
					} catch (final ArrowheadException ex) {
						logger.debug("Error while trying to close active session {}: {}", session.getQueueId(), ex.getMessage());
						logger.debug("Exception:", ex);
					}
				}
			}
		}
	}
}