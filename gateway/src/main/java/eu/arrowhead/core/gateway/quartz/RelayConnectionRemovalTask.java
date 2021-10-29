/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.gateway.quartz;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.core.gateway.thread.ConsumerSideServerSocketThread;
import eu.arrowhead.core.gateway.thread.ProviderSideSocketThreadHandler;

@Component
@DisallowConcurrentExecution
public class RelayConnectionRemovalTask implements Job {
	
	//=================================================================================================
	// member
	
	public static final long PERIOD = RelayConnectionRemovalTaskConfig.SCHEDULER_INTERVAL;
	
	@Value(CoreCommonConstants.$GATEWAY_INACTIVE_BRIDGE_TIMEOUT_WD)
	private long consumerSideThreshold;
	
	private long providerSideThreshold;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_CONSUMER_SIDE_SOCKET_THREAD_MAP)
	private ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP)
	private ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers;

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		this.providerSideThreshold = consumerSideThreshold + PERIOD;
		
		while (true) {
			final ZonedDateTime now = ZonedDateTime.now();
			handleConsumerSideConnections(now);
			handleProviderSideConnections(now);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void handleConsumerSideConnections(final ZonedDateTime now) {
		for (ConsumerSideServerSocketThread thread : activeConsumerSideSocketThreads.values()) {
			if (now.isAfter(thread.getLastInteractionTime().plusSeconds(consumerSideThreshold))) {
				thread.setInterrupted(true);
			}
		}
	}
	
	
	//-------------------------------------------------------------------------------------------------
	private void handleProviderSideConnections(final ZonedDateTime now) {
		for (final ProviderSideSocketThreadHandler threadHandler : activeProviderSideSocketThreadHandlers.values()) {
			if (now.isAfter(threadHandler.getLastInteractionTime().plusSeconds(providerSideThreshold))) {
				threadHandler.close();
			}
		}
	}
}
