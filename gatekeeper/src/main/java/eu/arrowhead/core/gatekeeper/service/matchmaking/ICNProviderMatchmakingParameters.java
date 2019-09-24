package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ICNProviderMatchmakingParameters {
	
	//=================================================================================================
	// members
	
	protected List<SystemRequestDTO> preferredLocalProviders = new ArrayList<>();
	protected long randomSeed = System.currentTimeMillis();
	
	// additional parameter can be add here to provide information to the various matchmaking algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNProviderMatchmakingParameters(final List<SystemRequestDTO> preferredLocalProviders) {
		Assert.notNull(preferredLocalProviders, "preferredLocalProviders list is null.");
		this.preferredLocalProviders = preferredLocalProviders;
	}

	//-------------------------------------------------------------------------------------------------
	public List<SystemRequestDTO> getPreferredLocalProviders() { return preferredLocalProviders; }
	public long getRandomSeed() { return randomSeed; }

	//-------------------------------------------------------------------------------------------------
	public void setRandomSeed(final long randomSeed) { this.randomSeed = randomSeed; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredLocalProviders(final List<SystemRequestDTO> preferredLocalProviders) {
		if (preferredLocalProviders != null) {
			this.preferredLocalProviders = preferredLocalProviders;
		}
	}
}