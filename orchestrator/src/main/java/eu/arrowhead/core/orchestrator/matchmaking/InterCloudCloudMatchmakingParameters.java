package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;

public class InterCloudCloudMatchmakingParameters {

	
	//=================================================================================================
	// members
	
	protected GSDQueryResultDTO gSDResult;
	protected List<CloudRequestDTO> preferredClouds;
	protected boolean onlyPreferred;

	
	// additional parameter can be add here to provide information to the various matchmaking algorithms
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudCloudMatchmakingParameters(final GSDQueryResultDTO gSDResult, final List<CloudRequestDTO> preferredClouds,
			final boolean onlyPreferred) {
		
		this.gSDResult = gSDResult;
		this.preferredClouds = preferredClouds;
		this.onlyPreferred = onlyPreferred;
	}

	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO getgSDResult() { return gSDResult;	}
	public List<CloudRequestDTO> getPreferredClouds() {	return preferredClouds;	}
	public boolean isOnlyPreferred() { return onlyPreferred; }

	public void setgSDResult( final GSDQueryResultDTO gSDResult) {	this.gSDResult = gSDResult;	}
	public void setPreferredClouds( final List<CloudRequestDTO> preferredClouds) {	this.preferredClouds = preferredClouds;	}
	public void setOnlyPreferred( final boolean onlyPreferred) { this.onlyPreferred = onlyPreferred; }
}
