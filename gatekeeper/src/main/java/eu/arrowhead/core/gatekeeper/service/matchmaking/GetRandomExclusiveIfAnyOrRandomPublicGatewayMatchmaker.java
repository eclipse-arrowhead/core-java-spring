package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

public class GetRandomExclusiveIfAnyOrRandomPublicGatewayMatchmaker implements RelayMatchmakingAlgorithm {

	//=================================================================================================
	// members
	private static final Logger logger = LogManager.getLogger(GetRandomExclusiveIfAnyOrRandomPublicGatewayMatchmaker.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	private Random rng;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns a random exclusive Gateway Relay if a cloud has any,
	 * otherwise returns a random public Gateway Relay (non-exclusive GATEWAY_RELAY or GENERAL_RELAY type)
	 * or return null if matchmaking is not possible.
	 */
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		logger.debug("GetRandomExclusiveIfAnyOrRandomPublicGatewayMatchmaker.doMatchmaking started...");
		
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		
		if (rng == null) {
			rng = new Random(parameters.getRandomSeed());
		}
		
		final List<Relay> exclusiveGatewayRelays = new ArrayList<>();
		
		if (!parameters.getCloud().getGatewayRelays().isEmpty()) {
			for (final CloudGatewayRelay relayConn : parameters.getCloud().getGatewayRelays()) {
				exclusiveGatewayRelays.add(relayConn.getRelay());
			}
			return exclusiveGatewayRelays.get(rng.nextInt(exclusiveGatewayRelays.size()));
		}
		
		final List<Relay> nonExclusiveGatewayRelays = new ArrayList<>();
		final List<Relay> generalRelays = new ArrayList<>();
		
		final List<Relay> publicRelays = gatekeeperDBService.getPublicRelaysByType(RelayType.GATEWAY_RELAY);
		for (final Relay relay : publicRelays) {
			if (relay.getType() == RelayType.GATEWAY_RELAY) {
				nonExclusiveGatewayRelays.add(relay);
			} else {
				generalRelays.add(relay);
			}
		}
		
		if (!nonExclusiveGatewayRelays.isEmpty()) {
			return nonExclusiveGatewayRelays.get(rng.nextInt(nonExclusiveGatewayRelays.size()));
		}
		
		if (!generalRelays.isEmpty()) {
			return generalRelays.get(rng.nextInt(generalRelays.size()));
		}
		
		return null;
	}
}