/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.gatekeeper.quartz;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

public class RelaySupervisor {

	//=================================================================================================
	// members
	
	private final static Set<GatekeeperRelayClient> RELAY_CLIENTS = Collections.synchronizedSet(new HashSet<>());
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static Set<GatekeeperRelayClient> getRegistry() {
		return RELAY_CLIENTS;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private RelaySupervisor() {
		throw new UnsupportedOperationException();
	}
}