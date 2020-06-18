package eu.arrowhead.relay.gateway;

import java.security.PrivateKey;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.relay.gateway.activemq.ActiveMQGatewayRelayClient;

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