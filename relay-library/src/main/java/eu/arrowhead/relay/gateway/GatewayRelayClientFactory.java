/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

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