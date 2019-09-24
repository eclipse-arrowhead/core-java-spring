package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.RelayType;

public class GetRandomAndDedicatedIfAnyGatekeeperMatchmaker implements RelayMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(GetRandomAndDedicatedIfAnyGatekeeperMatchmaker.class);

	private Random rng;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns a random Gatekeeper Relay with the type of GATEKEEPER_RELAY if there is any available. If not than return a random GENERAL_RELAY type.
	 */
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		logger.debug("GetRandomAndDedicatedIfAnyGatekeeperMatchmaker.doMatchmaking started...");
		
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		Assert.isTrue(parameters.getCloud().getGatekeeperRelays() != null && !parameters.getCloud().getGatekeeperRelays().isEmpty(), "GatekeeperRelaysList is null or empty.");
		
		if (rng == null) {
			rng = new Random(parameters.getRandomSeed());
		}
		
		final List<Relay> gatekeeperTypeRelays = new ArrayList<>();
		final List<Relay> generalTypeRelays = new ArrayList<>();
		for (final CloudGatekeeperRelay relayConn : parameters.getCloud().getGatekeeperRelays()) {
			if (relayConn.getRelay().getType() == RelayType.GATEKEEPER_RELAY) {
				gatekeeperTypeRelays.add(relayConn.getRelay());
			} else {
				generalTypeRelays.add(relayConn.getRelay());
			}
		}
		
		if (!gatekeeperTypeRelays.isEmpty()) {
			return gatekeeperTypeRelays.get(rng.nextInt(gatekeeperTypeRelays.size()));
		} else {
			return generalTypeRelays.get(rng.nextInt(generalTypeRelays.size()));
		}
	}
}