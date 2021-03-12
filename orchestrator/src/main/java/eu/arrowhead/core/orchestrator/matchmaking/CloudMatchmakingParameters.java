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

package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class CloudMatchmakingParameters {
	
	//=================================================================================================
	// members
	
	protected GSDQueryResultDTO gsdResult;
	protected List<CloudRequestDTO> preferredClouds;
	protected boolean onlyPreferred;
	
	// additional parameter can be add here to provide information to the various matchmaking algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudMatchmakingParameters(final GSDQueryResultDTO gsdResult, final List<CloudRequestDTO> preferredClouds, final boolean onlyPreferred) {
		this.gsdResult = gsdResult;
		this.preferredClouds = preferredClouds;
		this.onlyPreferred = onlyPreferred;
	}

	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO getGsdResult() { return gsdResult;	}
	public List<CloudRequestDTO> getPreferredClouds() {	return preferredClouds;	}
	public boolean isOnlyPreferred() { return onlyPreferred; }

	//-------------------------------------------------------------------------------------------------
	public void setGsdResult(final GSDQueryResultDTO gsdResult) { this.gsdResult = gsdResult; }
	public void setPreferredClouds(final List<CloudRequestDTO> preferredClouds) { this.preferredClouds = preferredClouds; }
	public void setOnlyPreferred(final boolean onlyPreferred) { this.onlyPreferred = onlyPreferred; }
}