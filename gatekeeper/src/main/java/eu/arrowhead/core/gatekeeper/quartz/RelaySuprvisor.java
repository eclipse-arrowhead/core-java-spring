package eu.arrowhead.core.gatekeeper.quartz;

import java.util.HashSet;
import java.util.Set;

import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

public class RelaySuprvisor {

	//=================================================================================================
	// members
	
	private final static Set<GatekeeperRelayClient> RELAY_CLIENTS = new HashSet<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static Set<GatekeeperRelayClient> getRegistry() {
		return RELAY_CLIENTS;
	}
}
