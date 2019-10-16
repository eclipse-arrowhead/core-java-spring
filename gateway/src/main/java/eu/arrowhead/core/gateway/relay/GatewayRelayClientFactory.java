package eu.arrowhead.core.gateway.relay;

import java.security.PrivateKey;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.core.gateway.relay.activemq.ActiveMQGatewayRelayClient;

public class GatewayRelayClientFactory {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static GatewayRelayClient createGatewayRelayClient(final String serverCommonName, final PrivateKey privateKey, final SSLProperties sslProps) {
		return new ActiveMQGatewayRelayClient(serverCommonName, privateKey, sslProps);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private GatewayRelayClientFactory() {
		throw new UnsupportedOperationException();
	}
}