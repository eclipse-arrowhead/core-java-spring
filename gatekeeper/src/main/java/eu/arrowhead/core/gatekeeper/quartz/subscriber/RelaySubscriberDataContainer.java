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

package eu.arrowhead.core.gatekeeper.quartz.subscriber;

import java.io.Closeable;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.core.gatekeeper.quartz.RelaySupervisor;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClientFactory;

@Component
public class RelaySubscriberDataContainer {
	
	//=================================================================================================
	// members

	private static final String RELAY_SUBSCRIBER_TASK_SCHEDULER = "relaySubscriberTaskScheduler";

	private static final Logger logger = LogManager.getLogger(RelaySubscriberDataContainer.class);
	
	@Resource(name = RELAY_SUBSCRIBER_TASK_SCHEDULER)
	private Scheduler relaySubsriberTaskScheduler;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private SSLProperties sslProps;
	
	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private long timeout;
	
	private final Map<String,RelayResource> relayResources = new HashMap<>();
	private GatekeeperRelayClient gatekeeperRelayClient;
	private GatekeeperRelayClient gatekeeperRelayClientWithCache;

	private volatile boolean initialized = false;
	private volatile boolean canceled = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public void init() {
		logger.debug("init started...");
		
		final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);

		this.gatekeeperRelayClient = GatekeeperRelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, sslProps, timeout, false, RelaySupervisor.getRegistry());
		this.gatekeeperRelayClientWithCache = GatekeeperRelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, sslProps, timeout, true, RelaySupervisor.getRegistry());

		initialized = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void close() {
		logger.debug("close started...");
		
		if (!canceled) {
			cancelJob();
		}
		
		synchronized (relayResources) {
			for (final RelayResource resource : relayResources.values()) {
				resource.close(gatekeeperRelayClient);
			}

			relayResources.clear();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void cancelJob() {
		logger.debug("cancelJob started...");
		
		try {
			relaySubsriberTaskScheduler.unscheduleJob(new TriggerKey(RelaySubscriberTaskConfig.NAME_OF_TRIGGER));
			canceled = true;
			logger.debug("STOPPED: Relay Subscriber task.");
		} catch (final SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void shutdown() {
		logger.debug("shutdown started...");
		
		try {
			relaySubsriberTaskScheduler.shutdown();
			canceled = true;
			logger.debug("SHUTDOWN: Relay Subscriber task.");
		} catch (final SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperRelayClient getGatekeeperRelayClient(final boolean cached) {
		return cached ? gatekeeperRelayClientWithCache : gatekeeperRelayClient;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() { return initialized; }
	public Map<String,RelayResource> getRelayResources() { return relayResources; }

	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	static class RelayResource {
		
		//=================================================================================================
		// members
		
		private final Session session;
		private final Closeable listener;
		
		//=================================================================================================
		// methods 
		
		//-------------------------------------------------------------------------------------------------
		public RelayResource(final Session session, final Closeable listener) {
			Assert.notNull(session, "session is null.");
			Assert.notNull(listener, "listener is null.");
			
			this.session = session;
			this.listener = listener;
		}

		//-------------------------------------------------------------------------------------------------
		public Session getSession() { return session; }
		
		//-------------------------------------------------------------------------------------------------
		public void close(final GatekeeperRelayClient gatekeeperRelayClient) {
			try {
				listener.close();
			} catch (final IOException ex) {
				logger.error("Error while trying to close message listener: {}", ex.getMessage());
				logger.debug("Exception:", ex);
			}
			
			gatekeeperRelayClient.closeConnection(session);
		}
	}
}