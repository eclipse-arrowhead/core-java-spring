package eu.arrowhead.relay.gatekeeper;

import java.security.PrivateKey;
import java.security.PublicKey;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.relay.gatekeeper.activemq.ActiveMQGatekeeperRelayClient;

public class GatekeeperRelayClientFactory {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static GatekeeperRelayClient createGatekeeperRelayClient(final String serverCommonName, final PublicKey publicKey, final PrivateKey privateKey, final SSLProperties sslProps,
																    final long timeout) {
		return createGatekeeperRelayClient(serverCommonName, publicKey, privateKey, sslProps, timeout, true);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static GatekeeperRelayClient createGatekeeperRelayClient(final String serverCommonName, final PublicKey publicKey, final PrivateKey privateKey, final SSLProperties sslProps, 
																	final long timeout, final boolean useCache) {
		return useCache ? new GatekeeperRelayClientUsingCachedSessions(serverCommonName, publicKey, privateKey, sslProps, timeout) : 
						  new ActiveMQGatekeeperRelayClient(serverCommonName, publicKey, privateKey, sslProps, timeout);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GatekeeperRelayClientFactory() {
		throw new UnsupportedOperationException();
	}
}