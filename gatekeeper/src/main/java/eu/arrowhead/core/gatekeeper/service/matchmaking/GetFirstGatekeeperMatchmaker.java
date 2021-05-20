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

package eu.arrowhead.core.gatekeeper.service.matchmaking;

import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Relay;

public class GetFirstGatekeeperMatchmaker implements RelayMatchmakingAlgorithm {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first Gatekeeper Relay, no matter if it is GATEKEEPER_RELAY or GENERAL_RELAY type
	 */
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		Assert.isTrue(parameters.getCloud().getGatekeeperRelays() != null && !parameters.getCloud().getGatekeeperRelays().isEmpty(), "GatekeeperRelaysList is null or empty.");
		
		return parameters.getCloud().getGatekeeperRelays().iterator().next().getRelay();
	}
}