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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

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