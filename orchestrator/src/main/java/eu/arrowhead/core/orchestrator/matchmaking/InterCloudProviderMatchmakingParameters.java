package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.ICNResponseDTO;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;

public class InterCloudProviderMatchmakingParameters {
	//=================================================================================================
	// members
	
	protected ICNResponseDTO icnResponseDTO;
	protected List<PreferredProviderDataDTO> preferredGlobalProviders = new ArrayList<>();
	protected boolean storeOrchestration;

	
	// additional parameter can be add here to provide information to the various matchmaking algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudProviderMatchmakingParameters(
			final ICNResponseDTO icnResponseDTO, 
			final List<PreferredProviderDataDTO> preferredGlobalProviders,
			final boolean storeOrchestration) {
		Assert.notNull(icnResponseDTO, "icnResponseDTO is null.");
		Assert.notNull(preferredGlobalProviders, "preferredLocalProviders set is null.");
		
		this.icnResponseDTO = icnResponseDTO;
		this.preferredGlobalProviders = preferredGlobalProviders;
		this.storeOrchestration = storeOrchestration;
	}

	//-------------------------------------------------------------------------------------------------
	public ICNResponseDTO getIcnResponseDTO( ) { return icnResponseDTO; }
	public List<PreferredProviderDataDTO> getPreferredGlobalProviders() { return preferredGlobalProviders; }
	public boolean isStoreOrchestration() {return storeOrchestration; }

	//-------------------------------------------------------------------------------------------------
	public void setPreferredGlobalProviders(final List<PreferredProviderDataDTO> preferredGlobalProviders) {
		if (preferredGlobalProviders != null) {
			this.preferredGlobalProviders = preferredGlobalProviders;
		}
	}
	public void setIcnResponseDTO(ICNResponseDTO icnResponseDTO) { this.icnResponseDTO = icnResponseDTO; }
	public void setStoreOrchestration(boolean storeOrchestration) { this.storeOrchestration = storeOrchestration; }
}
