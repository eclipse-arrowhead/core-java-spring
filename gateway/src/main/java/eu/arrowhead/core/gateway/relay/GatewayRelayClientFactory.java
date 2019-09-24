package eu.arrowhead.core.gateway.relay;

import java.security.PrivateKey;

import eu.arrowhead.core.gateway.relay.activemq.ActiveMQGatewayRelayClient;

public class GatewayRelayClientFactory {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static GatewayRelayClient createGatewayRelayClient(final String serverCommonName, final PrivateKey privateKey) {
		return new ActiveMQGatewayRelayClient(serverCommonName, privateKey);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private GatewayRelayClientFactory() {
		throw new UnsupportedOperationException();
	}
}