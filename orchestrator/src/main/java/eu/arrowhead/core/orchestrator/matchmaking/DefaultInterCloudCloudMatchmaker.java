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

public class DefaultInterCloudCloudMatchmaker implements InterCloudCloudMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DefaultInterCloudProviderMatchmaker.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	

	@Override
	public CloudResponseDTO doMatchmaking(InterCloudCloudMatchmakingParameters params) {
		logger.debug("DefaultInterCloudCloudMatchmaker.doMatchmaking started...");
		Assert.notNull(params, "params is null");
		
		final GSDQueryResultDTO gSDResult = params.getgSDResult();
		final List<CloudRequestDTO> preferredClouds = params.getPreferredClouds();
		final boolean onlyPreferred = params.isOnlyPreferred();
		
		final List<CloudResponseDTO> partnerClouds = new ArrayList<>(gSDResult.getResults().size());
		for (GSDPollResponseDTO gSDPollResponseDTO : gSDResult.getResults()) {
			if ( gSDPollResponseDTO != null &&  gSDPollResponseDTO.getProviderCloud() != null) {
				
				partnerClouds.add(gSDPollResponseDTO.getProviderCloud());
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
	    
	    
	    if ( onlyPreferred ) {
	    	//Return Empty response
	    	return new CloudResponseDTO();
	    }
       
	    // Return the first 
	    return partnerClouds.get(0);

	}

}
