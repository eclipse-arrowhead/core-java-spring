package eu.arrowhead.core.gatekeeper.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import antlr.debug.Event;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.RelayClientFactory;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GatekeeperMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GatekeeperMatchmakingParameters;

@Component
public class GatekeeperDriver {
	
	//=================================================================================================
	// members
	
	@Resource(name = CommonConstants.GATEKEEPER_MATCHMAKER)
	private GatekeeperMatchmakingAlgorithm gatekeeperMatchmaker;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private long timeout;
	
	private final Logger logger = LogManager.getLogger(GatekeeperDriver.class);
	
	private GatekeeperRelayClient relayClient;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	@EventListener
	@Order(15)
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)) {
				throw new ArrowheadException("Server's certificate not found.");
		}
		final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Server's public key is not found.");
		}
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)) {
			throw new ArrowheadException("Server's private key is not found.");
		}
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);
	
		relayClient = RelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, timeout);	
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<GSDPollResponseDTO> sendGSDPollRequest(final List<Cloud> cloudsToContact, final GSDPollRequestDTO gsdPollRequestDTO) {
		logger.debug("sendGSDPollRequest started...");		
		Assert.isTrue(cloudsToContact != null && !cloudsToContact.isEmpty(), "cloudsToContact list is null or empty");
		Assert.notNull(gsdPollRequestDTO, "gsdPollRequestDTO is null");
		Assert.notNull(gsdPollRequestDTO.getRequestedService(), "requestedService is null");
		Assert.isTrue(!Utilities.isEmpty(gsdPollRequestDTO.getRequestedService().getServiceDefinitionRequirement()), "serviceDefinitionRequirement is empty");
		Assert.notNull(gsdPollRequestDTO.getRequesterCloud(), "requesterCloud is null");
		
		final int numOfCloudsToContact = cloudsToContact.size();

		final BlockingQueue<GSDPollResponseDTO> queue = new LinkedBlockingQueue<>(numOfCloudsToContact);		
		final GSDPollRequestExecutor gsdPollRequestExecutor = new GSDPollRequestExecutor(queue, relayClient, gsdPollRequestDTO, getOneGatekeeperRelayPerCloud(cloudsToContact));
		
		gsdPollRequestExecutor.execute();
		
		final List<GSDPollResponseDTO> gsdPollAnswers = new ArrayList<>(numOfCloudsToContact);
		for (int i = 0; i < numOfCloudsToContact; ++i) {
			
			try {
				
				gsdPollAnswers.add(queue.take());
				
			} catch (final InterruptedException ex) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
			}
		} 
		
		return gsdPollAnswers;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO sendICNProposal(final Cloud targetCloud, final ICNProposalRequestDTO request) {
		logger.debug("sendICNProposal started...");
				
		//TODO:
		
		return null;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------		
	private Map<Cloud, Relay> getOneGatekeeperRelayPerCloud(final List<Cloud> clouds) {
		logger.debug("getOneGatekeeperRelayPerCloud started...");
		
		final Map<Cloud, Relay> realyPerCloud = new HashMap<>();
		for (final Cloud cloud : clouds) {
			final Relay relay = gatekeeperMatchmaker.doMatchmaking(new GatekeeperMatchmakingParameters(cloud));
			realyPerCloud.put(cloud, relay);
		}
		
		return realyPerCloud;
	}
}