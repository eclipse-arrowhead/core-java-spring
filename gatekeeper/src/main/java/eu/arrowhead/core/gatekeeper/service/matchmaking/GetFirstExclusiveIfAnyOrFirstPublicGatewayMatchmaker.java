package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

public class GetFirstExclusiveIfAnyOrFirstPublicGatewayMatchmaker implements RelayMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first dedicated Gateway Relay if a cloud has any,
	 * otherwise return the first public Relay (non-exclusive GATEWAY_RELAY or GENERAL_RELAY type),
	 * or return null if matchmaking is not possible.
	 */
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		
		if (parameters.getCloud().getGatewayRelays() != null && !parameters.getCloud().getGatewayRelays().isEmpty()) {
			return parameters.getCloud().getGatewayRelays().iterator().next().getRelay();
		}
		
		final List<Relay> publicGatewayRelays = gatekeeperDBService.getPublicRelaysByType(RelayType.GATEWAY_RELAY);
		if (!publicGatewayRelays.isEmpty()) {
			return publicGatewayRelays.get(0);
		}
		
		return null;
	}

}
