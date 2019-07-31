package eu.arrowhead.core.gatekeeper.relay;

import java.security.PrivateKey;
import java.security.PublicKey;

import eu.arrowhead.core.gatekeeper.relay.activemq.ActiveMQGatekeeperRelayClient;

public class RelayClientFactory {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static GatekeeperRelayClient createUnsecuredGatekeeperRelayClient() {
		return new ActiveMQGatekeeperRelayClient(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static GatekeeperRelayClient createSecuredGatekeeperRelayClient(final PublicKey publicKey, final PrivateKey privateKey) {
		return new ActiveMQGatekeeperRelayClient(publicKey, privateKey);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private RelayClientFactory() {
		throw new UnsupportedOperationException();
	}
}