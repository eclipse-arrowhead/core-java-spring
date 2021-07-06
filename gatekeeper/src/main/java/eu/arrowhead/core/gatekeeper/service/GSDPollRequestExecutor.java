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

package eu.arrowhead.core.gatekeeper.service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

public class GSDPollRequestExecutor {
	
	//=================================================================================================
	// members
	
	private static final int MAX_THREAD_POOL_SIZE = 20;

	private final BlockingQueue<ErrorWrapperDTO> queue;
	private final ThreadPoolExecutor threadPool;
	private final GatekeeperRelayClient relayClient;
	private final GSDPollRequestDTO gsdPollRequestDTO;
	private final Map<Cloud,Relay> gatekeeperRelayPerCloud;
	
	private final Logger logger = LogManager.getLogger(GSDPollRequestExecutor.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDPollRequestExecutor(final BlockingQueue<ErrorWrapperDTO> queue, final GatekeeperRelayClient relayClient, final GSDPollRequestDTO gsdPollRequestDTO, 
								  final Map<Cloud,Relay> gatekeeperRelayPerCloud) {
		this.queue = queue;
		this.relayClient = relayClient;
		this.gsdPollRequestDTO = gsdPollRequestDTO;
		this.gatekeeperRelayPerCloud = gatekeeperRelayPerCloud;
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.gatekeeperRelayPerCloud.size() > MAX_THREAD_POOL_SIZE ? MAX_THREAD_POOL_SIZE : this.gatekeeperRelayPerCloud.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void execute() {
		logger.debug("GSDPollRequestExecutor.execute started...");
		validateMembers();
		
		final Map<String,Session> sessionsToClouds = createSessionsToClouds();
		for (final Entry<Cloud,Relay> cloudRelay : gatekeeperRelayPerCloud.entrySet()) {			
			try {
				final String cloudCN = getRecipientCommonName(cloudRelay.getKey());				
				threadPool.execute(new GSDPollTask(relayClient, sessionsToClouds.get(cloudCN), cloudCN, cloudRelay.getKey().getAuthenticationInfo(), gsdPollRequestDTO, queue));
			} catch (final RejectedExecutionException ex) {
				logger.error("GSDPollTask execution rejected at {}", ZonedDateTime.now());
				
				// adding empty responseDTO into the blocking queue in order to having exactly as many response as request was sent
				queue.add(new GSDPollResponseDTO());
			}
		}
		
		threadPool.shutdown();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void shutdownExecutionNow() {
		threadPool.shutdownNow();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Map<String,Session> createSessionsToClouds() {
		logger.debug("createSessionsToClouds started...");
		
		final Map<String,Session> sessionsForRelays = new HashMap<>();
		
		for (final Entry<Cloud,Relay> cloudRelay : gatekeeperRelayPerCloud.entrySet()) {
			final Relay relay = cloudRelay.getValue();
			try {
				final String cloudCN = getRecipientCommonName(cloudRelay.getKey());		
				final Session session = relayClient.createConnection(relay.getAddress(), relay.getPort(), relay.getSecure());
				sessionsForRelays.put(cloudCN, session);						
			} catch (final JMSException ex) {
				logger.debug("Exception occured while creating connection for address: {} and port {}:", relay.getAddress(), relay.getPort());
				logger.debug("Exception message: {}:", ex.getMessage());
			}
		}
		
		return sessionsForRelays;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getRecipientCommonName(final Cloud cloud) {
		return "gatekeeper." + Utilities.getCloudCommonName(cloud.getOperator(), cloud.getName()); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateMembers() {
		Assert.notNull(this.queue, "queue is null");
		Assert.notNull(this.threadPool, "threadPool is null");
		Assert.notNull(this.relayClient, "relayClient is null");
		Assert.notNull(this.gsdPollRequestDTO, "gsdPollRequestDTO is null");
		Assert.notNull(this.gatekeeperRelayPerCloud, "gatekeeperRelayPerCloud is null");
	}
}