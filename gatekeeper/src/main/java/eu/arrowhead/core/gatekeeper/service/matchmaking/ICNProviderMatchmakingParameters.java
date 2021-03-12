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

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ICNProviderMatchmakingParameters {
	
	//=================================================================================================
	// members
	
	protected List<SystemRequestDTO> preferredLocalProviders = new ArrayList<>();
	protected boolean onlyPreferred = false;
	protected long randomSeed = System.currentTimeMillis();
	
	// additional parameter can be add here to provide information to the various matchmaking algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNProviderMatchmakingParameters(final List<SystemRequestDTO> preferredLocalProviders, final boolean onlyPreferred) {
		Assert.notNull(preferredLocalProviders, "preferredLocalProviders list is null.");
		this.preferredLocalProviders = preferredLocalProviders;
		this.onlyPreferred = onlyPreferred;
	}

	//-------------------------------------------------------------------------------------------------
	public List<SystemRequestDTO> getPreferredLocalProviders() { return preferredLocalProviders; }
	public boolean isOnlyPreferred() { return onlyPreferred; }
	public long getRandomSeed() { return randomSeed; }

	//-------------------------------------------------------------------------------------------------
	public void setOnlyPreferred(final boolean onlyPreferred) { this.onlyPreferred = onlyPreferred; }
	public void setRandomSeed(final long randomSeed) { this.randomSeed = randomSeed; }	

	//-------------------------------------------------------------------------------------------------
	public void setPreferredLocalProviders(final List<SystemRequestDTO> preferredLocalProviders) {
		if (preferredLocalProviders != null) {
			this.preferredLocalProviders = preferredLocalProviders;
		}
	}
}