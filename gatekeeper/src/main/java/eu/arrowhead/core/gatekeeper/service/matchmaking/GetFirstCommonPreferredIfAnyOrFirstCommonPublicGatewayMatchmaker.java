package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

public class GetFirstCommonPreferredIfAnyOrFirstCommonPublicGatewayMatchmaker implements RelayMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	private static final Logger logger = LogManager.getLogger(GetFirstCommonPreferredIfAnyOrFirstCommonPublicGatewayMatchmaker.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		logger.debug("GetFirstCommonPreferredIfAnyOrFirstCommonPublicGatewayMatchmaker.doMatchmaking started...");
		
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		Assert.notNull(parameters.getPreferredGatewayRelays(), "Preferred relay list is null");
		Assert.notNull(parameters.getKnownGatewayRelays(), "Known relay list is null");
		
		
		//TODO: reimplement
		if (parameters.getCloud().getGatewayRelays() != null && !parameters.getCloud().getGatewayRelays().isEmpty()) {
			return parameters.getCloud().getGatewayRelays().iterator().next().getRelay();
		}
		
		final List<Relay> publicGatewayRelays = gatekeeperDBService.getPublicGatewayRelays();
		if (!publicGatewayRelays.isEmpty()) {
			return publicGatewayRelays.get(0);
		}
		
		return null;
	}

}
