package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.DTOUtilities;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;

public class DefaultCloudMatchmaker implements CloudMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DefaultCloudMatchmaker.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Override
	public CloudResponseDTO doMatchmaking(final CloudMatchmakingParameters params) {
		logger.debug("DefaultCloudMatchmaker.doMatchmaking started...");
		Assert.notNull(params, "params is null");
		
		final GSDQueryResultDTO gsdResult = params.getGsdResult();
	    if (gsdResult == null || gsdResult.getResults().isEmpty()) {
	    	// Return empty response
	    	return new CloudResponseDTO();
	    }
		
		final List<CloudRequestDTO> preferredClouds = params.getPreferredClouds();
		final boolean onlyPreferred = params.isOnlyPreferred();
		
		final List<CloudResponseDTO> partnerClouds = new ArrayList<>(gsdResult.getResults().size());
		for (final GSDPollResponseDTO gsdPollResponseDTO : gsdResult.getResults()) {
			if (gsdPollResponseDTO != null &&  gsdPollResponseDTO.getProviderCloud() != null) {
				partnerClouds.add(gsdPollResponseDTO.getProviderCloud());
			}
		}		

	    if (!preferredClouds.isEmpty()) {
		    for (final CloudRequestDTO preferredCloud : preferredClouds) {
		        for (final CloudResponseDTO partnerCloud : partnerClouds) {
					if (DTOUtilities.equalsCloudInResponseAndRequest(partnerCloud, preferredCloud)) {
						return partnerCloud;
					}
		        }
		    }
	    }	    
	    
	    if (onlyPreferred) {
	    	// Return empty response
	    	return new CloudResponseDTO();
	    }
       
	    // Return the first 
	    return partnerClouds.get(0);
	}
}