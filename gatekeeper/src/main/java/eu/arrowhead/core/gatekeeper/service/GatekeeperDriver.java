package eu.arrowhead.core.gatekeeper.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayResponse;
import eu.arrowhead.core.gatekeeper.relay.GeneralAdvertisementResult;
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
	@PostConstruct
	public void init() {
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
	public ICNProposalResponseDTO sendICNProposal(final Cloud targetCloud, final ICNProposalRequestDTO request) {
		logger.debug("sendICNProposal started...");
		
		Assert.notNull(targetCloud, "Target cloud is null.");
		Assert.notNull(request, "Request is null.");
		
		final Relay relay = gatekeeperMatchmaker.doMatchmaking(new GatekeeperMatchmakingParameters(targetCloud));
		try {
			final Session session = relayClient.createConnection(relay.getAddress(), relay.getPort());
			final GeneralAdvertisementResult advResult = relayClient.publishGeneralAdvertisement(session, getRecipientCommonName(targetCloud), targetCloud.getAuthenticationInfo());
			if (advResult == null) {
				//TODO: timeout 
				return null;
			}
			
			final GatekeeperRelayResponse relayResponse = relayClient.sendRequestAndReturnResponse(session, advResult, request);
			if (relayResponse == null) {
				//TODO: timeout
				return null;
			}
			
			return relayResponse.getICNProposalResponse();
		} catch (final JMSException ex) {
			// TODO implement exception handling
			ex.printStackTrace();
			
			return null;
		}
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
	
	//-------------------------------------------------------------------------------------------------
	private String getRecipientCommonName(final Cloud cloud) {
		return "gatekeeper." + Utilities.getCloudCommonName(cloud.getOperator(), cloud.getName()); 
	}
}