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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;

public class IntraCloudProviderMatchmakingParameters {
	
	//=================================================================================================
	// members
	
	protected List<PreferredProviderDataDTO> preferredLocalProviders = new ArrayList<>();
	protected long randomSeed = System.currentTimeMillis();
	
	// additional parameter can be add here to provide information to the various matchmaking algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudProviderMatchmakingParameters(final List<PreferredProviderDataDTO> preferredLocalProviders) {
		Assert.notNull(preferredLocalProviders, "preferredLocalProviders list is null.");
		this.preferredLocalProviders = preferredLocalProviders;
	}

	//-------------------------------------------------------------------------------------------------
	public List<PreferredProviderDataDTO> getPreferredLocalProviders() { return preferredLocalProviders; }
	public long getRandomSeed() { return randomSeed; }

	//-------------------------------------------------------------------------------------------------
	public void setRandomSeed(final long randomSeed) { this.randomSeed = randomSeed; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredLocalProviders(final List<PreferredProviderDataDTO> preferredLocalProviders) {
		if (preferredLocalProviders != null) {
			this.preferredLocalProviders = preferredLocalProviders;
		}
	}
}