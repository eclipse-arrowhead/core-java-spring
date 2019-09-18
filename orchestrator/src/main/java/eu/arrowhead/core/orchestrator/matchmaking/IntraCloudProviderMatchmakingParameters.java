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