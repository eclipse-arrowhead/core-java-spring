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

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.quartz.subscriber.RelaySubscriberDataContainer.RelayResource;
import eu.arrowhead.core.gatekeeper.service.GeneralAdvertisementMessageListener;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

@Component
@DisallowConcurrentExecution
public class RelaySubscriberTask implements Job {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(RelaySubscriberTask.class);

	@Autowired
	private ApplicationContext applicationContext;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CoreCommonConstants.$NO_GATEKEEPER_RELAY_REQUEST_HANDLER_WORKERS_WD)
	private int noWorkers;
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	@Autowired
	private RelaySubscriberDataContainer dataContainer;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Relay Subscriber task");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {
			logger.debug("FINISHED: Relay Subscriber task can not run if server is in standalone mode");
			dataContainer.shutdown();
			return;
		}
		
		if (!dataContainer.isInitialized()) {
			return;
		}
		
		createOrCheckSubscribers();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void createOrCheckSubscribers() {
		logger.debug("createOrCheckSubsribers started...");
		
		final Set<Relay> gatekeeperRelays = gatekeeperDBService.getAllLiveGatekeeperRelays();
		final Map<String,RelayResource> relayResources = dataContainer.getRelayResources();
		synchronized (relayResources) {
			final GatekeeperRelayClient gatekeeperRelayClient = dataContainer.getGatekeeperRelayClient(false);
			
			for (final Relay relay : gatekeeperRelays) {
				final String key = relay.getAddress() + ":" + relay.getPort();
				if (relayResources.containsKey(key)) {
					final RelayResource resource = relayResources.get(key);
					if (gatekeeperRelayClient.isConnectionClosed(resource.getSession())) {
						resource.close(gatekeeperRelayClient);
						relayResources.remove(key);
						createSubscribers(relay);
					}
				} else {
					createSubscribers(relay);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void createSubscribers(final Relay relay) {
		logger.debug("createSubscribers started...");

		try {
			final GatekeeperRelayClient gatekeeperRelayClient = dataContainer.getGatekeeperRelayClient(false);
			final Session session = gatekeeperRelayClient.createConnection(relay.getAddress(), relay.getPort(), relay.getSecure());
			final MessageConsumer consumer = gatekeeperRelayClient.subscribeGeneralAdvertisementTopic(session);
			final GatekeeperRelayClient gatekeeperRelayClientWithCache = dataContainer.getGatekeeperRelayClient(true);
			final GeneralAdvertisementMessageListener listener = new GeneralAdvertisementMessageListener(applicationContext, relay.getAddress(), relay.getPort(), relay.getSecure(),
																										 gatekeeperRelayClientWithCache, noWorkers);
			consumer.setMessageListener(listener);
			final String key = relay.getAddress() + ":" + relay.getPort();
			dataContainer.getRelayResources().put(key, new RelayResource(session, listener));
		} catch (final JMSException | ArrowheadException ex) {
			logger.debug("Error while trying to subscribe relay {}:{}", relay.getAddress(), relay.getPort()); // we skip the wrong ones
		}
	}
}