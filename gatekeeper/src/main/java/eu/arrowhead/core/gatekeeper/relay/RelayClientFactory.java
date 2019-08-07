package eu.arrowhead.core.gatekeeper.relay;

import java.security.PrivateKey;
import java.security.PublicKey;

public class RelayClientFactory {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static GatekeeperRelayClient createGatekeeperRelayClient(final String serverCommonName, final PublicKey publicKey, final PrivateKey privateKey, final long timeout) {
		return new GatekeeperRelayClientUsingCachedSessions(serverCommonName, publicKey, privateKey, timeout);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private RelayClientFactory() {
		throw new UnsupportedOperationException();
	}
}