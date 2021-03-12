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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;

public class RelayMatchmakingParameters {

	//=================================================================================================
	// members
	
	protected Cloud cloud;
	protected List<RelayRequestDTO> preferredGatewayRelays = new ArrayList<>();
	protected List<RelayRequestDTO> knownGatewayRelays = new ArrayList<>();
	protected long randomSeed = System.currentTimeMillis();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public RelayMatchmakingParameters(final Cloud cloud) {
		Assert.notNull(cloud, "cloud is null.");
		this.cloud = cloud;
	}

	//-------------------------------------------------------------------------------------------------
	public Cloud getCloud() { return cloud; }
	public long getRandomSeed() { return randomSeed; }
	public List<RelayRequestDTO> getPreferredGatewayRelays() { return preferredGatewayRelays; }
	public List<RelayRequestDTO> getKnownGatewayRelays() { return knownGatewayRelays; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final Cloud cloud) { this.cloud = cloud; }
	public void setRandomSeed(final long randomSeed) { this.randomSeed = randomSeed; }
	public void setPreferredGatewayRelays(final List<RelayRequestDTO> preferredGatewayRelays) { this.preferredGatewayRelays = preferredGatewayRelays; }
	public void setKnownGatewayRelays(final List<RelayRequestDTO> knownGatewayRelays) { this.knownGatewayRelays = knownGatewayRelays; }	
}