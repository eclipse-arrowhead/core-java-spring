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