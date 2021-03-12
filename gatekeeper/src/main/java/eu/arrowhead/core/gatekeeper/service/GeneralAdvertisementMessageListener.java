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

import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

public class GeneralAdvertisementMessageListener implements Closeable, MessageListener {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GeneralAdvertisementMessageListener.class);
	private static int idCounter = 1;
	
	private final int id;
	private final ApplicationContext appContext;
	private final String relayHost;
	private final int relayPort;
	private final boolean securedRelay;
	private final GatekeeperRelayClient relayClient;
	private final ThreadPoolExecutor threadPool;
	
	private boolean closed = false;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageListener(final ApplicationContext appContext, final String relayHost, final int relayPort, final boolean securedRelay, final GatekeeperRelayClient relayClient,
											   final int threadPoolSize) {
		logger.debug("Constructor started...");
		
		Assert.notNull(appContext, "appContext is null.");
		Assert.isTrue(!Utilities.isEmpty(relayHost), "relayHost is null or blank.");
		Assert.isTrue(relayPort > CommonConstants.SYSTEM_PORT_RANGE_MIN && relayPort < CommonConstants.SYSTEM_PORT_RANGE_MAX, "relayPort is invalid.");
		Assert.notNull(relayClient, "Gatekeeper relay client is null.");
		Assert.isTrue(threadPoolSize > 0, "threadPoolSize must be a positive number.");
		
		this.id = idCounter++;
		this.appContext = appContext;
		this.relayHost = relayHost;
		this.relayPort = relayPort;
		this.securedRelay = securedRelay;
		this.relayClient = relayClient;
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
		logger.debug("GeneralAdvertisementMessageListener-{} started...", id);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message msg) {
		logger.debug("onMessage started...");
		
		if (!closed) {
			try {
				if (msg != null) {
					threadPool.execute(new GatekeeperTask(appContext, relayHost, relayPort, securedRelay, relayClient, msg)); 
				}
			} catch (final RejectedExecutionException ex) {
				logger.error("Message rejected at {}", ZonedDateTime.now());
			}
		} else {
			logger.trace("Message rejected at {} because listener is closed", ZonedDateTime.now());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void close() throws IOException {
		logger.debug("close started...");
		
		closed = true;
		threadPool.shutdownNow();
		
		logger.debug("GeneralAdvertisementMessageListener-{} stopped...", id);
	}
}