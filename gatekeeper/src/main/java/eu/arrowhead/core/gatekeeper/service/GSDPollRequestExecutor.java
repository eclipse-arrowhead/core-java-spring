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

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;

public class GSDPollRequestExecutor {
	
	//=================================================================================================
	// members
	
	private static final int MAX_THREAD_POOL_SIZE = 20;

	private final BlockingQueue<GSDPollResponseDTO> queue;
	private final ThreadPoolExecutor threadPool;
	private final GatekeeperRelayClient relayClient;
	private final GSDPollRequestDTO gsdPollRequestDTO;
	private final Map<Cloud, Relay> gatekeeperRelayPerCloud;
	
	private final Logger logger = LogManager.getLogger(GSDPollRequestExecutor.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDPollRequestExecutor(final BlockingQueue<GSDPollResponseDTO> queue, final GatekeeperRelayClient relayClient, final GSDPollRequestDTO gsdPollRequestDTO, final Map<Cloud, Relay> gatekeeperRelayPerCloud) {
		
		this.queue = queue;
		this.relayClient = relayClient;
		this.gsdPollRequestDTO = gsdPollRequestDTO;
		this.gatekeeperRelayPerCloud = gatekeeperRelayPerCloud;
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.gatekeeperRelayPerCloud.size() > MAX_THREAD_POOL_SIZE ? MAX_THREAD_POOL_SIZE : this.gatekeeperRelayPerCloud.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void execute() {
		logger.debug("GSDPollRequestExecutor.execute started...");
		
		for (final Entry<Cloud, Relay> cloudRelay : gatekeeperRelayPerCloud.entrySet()) {			
			try {
			
				final String cloudCN = getRecipientCommonName(cloudRelay.getKey());
				final Map<String, Session> sessionsToClouds = createSessionsToClouds();
				
				threadPool.execute(new GSDPollTask(relayClient,
												   sessionsToClouds.get(cloudCN),
												   cloudCN,
												   cloudRelay.getKey().getAuthenticationInfo(), 
												   gsdPollRequestDTO, 
												   queue));
			
			} catch (final RejectedExecutionException ex) {
				logger.error("GSDPollTask execution rejected at {}", ZonedDateTime.now());
				
				//adding empty responseDTO into the blocking queue in order to having exactly as many response as request was sent
				queue.add(new GSDPollResponseDTO());
			}
		}
		threadPool.shutdown();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void shutdownExecutionNow() {
		threadPool.isShutdown();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Map<String, Session> createSessionsToClouds() {
		logger.debug("createSessionsToRelays started...");
		
		final Map<String, Session> sessionsForRelays = new HashMap<>();
		
		for (final Entry<Cloud, Relay> cloudRelay : gatekeeperRelayPerCloud.entrySet()) {
			
			try {
				
				final String cloudCN = getRecipientCommonName(cloudRelay.getKey());		
				final Session session = relayClient.createConnection(cloudRelay.getValue().getAddress(), cloudRelay.getValue().getPort());
				sessionsForRelays.put(cloudCN, session);						
				
			} catch (final JMSException ex) {
				logger.debug("Exception occured while creating connection for address: {} and port {}:", cloudRelay.getValue().getAddress(), cloudRelay.getValue().getPort());
				logger.debug("Exception message: {}:", ex.getMessage());
			}
		}
		return sessionsForRelays;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getRecipientCommonName(final Cloud cloud) {
		return "gatekeeper." + Utilities.getCloudCommonName(cloud.getOperator(), cloud.getName()); 
	}
}
