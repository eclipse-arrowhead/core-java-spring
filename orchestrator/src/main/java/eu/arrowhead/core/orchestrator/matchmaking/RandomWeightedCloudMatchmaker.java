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
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class RandomWeightedCloudMatchmaker implements CloudMatchmakingAlgorithm {
    
    //=================================================================================================
    // members
    
    private static final Logger logger = LogManager.getLogger(RandomWeightedCloudMatchmaker.class);

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------    
    @Override
    public CloudResponseDTO doMatchmaking(final CloudMatchmakingParameters params) {
        logger.debug("RandomWeightedCloudMatchmaker.doMatchmaking started...");
        Assert.notNull(params, "params is null");
        
        final GSDQueryResultDTO gsdResult = params.getGsdResult();
        if (gsdResult == null || gsdResult.getResults().isEmpty()) {
            // Return empty response
            return new CloudResponseDTO();
        }
        
        final List<CloudRequestDTO> preferredClouds = params.getPreferredClouds();
        final boolean onlyPreferred = params.isOnlyPreferred();
        
        final RandomCollection randomPreferredCloudCollection = fillUpRandomPreferredCloudCollection(gsdResult, preferredClouds);
        if (!randomPreferredCloudCollection.isEmpty()) {
            return randomPreferredCloudCollection.next();
        }
        
        if (onlyPreferred) {
            // Return empty response
            return new CloudResponseDTO();
        }
        
        final RandomCollection randomCloudCollection = fillUpRandomCloudCollection(gsdResult);
        
        return randomCloudCollection.isEmpty() ? new CloudResponseDTO() : randomCloudCollection.next();
    }

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
    private RandomCollection fillUpRandomPreferredCloudCollection(final GSDQueryResultDTO gsdResult, final List<CloudRequestDTO> preferredClouds) {
        logger.debug("fillUpRandomPreferredCloudCollection started...");
        
        final RandomCollection randomPreferredCloudCollection = new RandomCollection();
        for (final GSDPollResponseDTO gsdPollResponseDTO : gsdResult.getResults()) {
            if (gsdPollResponseDTO != null && gsdPollResponseDTO.getProviderCloud() != null) {
                final CloudResponseDTO cloud = gsdPollResponseDTO.getProviderCloud();
                
                for (final CloudRequestDTO preferredCloud : preferredClouds) {
                    if (DTOUtilities.equalsCloudInResponseAndRequest(cloud, preferredCloud)) {
                        randomPreferredCloudCollection.add(gsdPollResponseDTO.getNumOfProviders(), cloud);
                    }
                }                
            }
        }
        
        return randomPreferredCloudCollection;
    }
    
    //-------------------------------------------------------------------------------------------------
    private RandomCollection fillUpRandomCloudCollection(final GSDQueryResultDTO gsdResult) {
        logger.debug("fillUpRandomCloudCollection started...");
        
        final RandomCollection randomCloudCollection = new RandomCollection();
        for (final GSDPollResponseDTO gsdPollResponseDTO : gsdResult.getResults()) {
            if (gsdPollResponseDTO != null &&  gsdPollResponseDTO.getProviderCloud() != null) {
                randomCloudCollection.add(gsdPollResponseDTO.getNumOfProviders(), gsdPollResponseDTO.getProviderCloud());                
            }
        }
        
        return randomCloudCollection;
    }

	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
    private static class RandomCollection {
        
        //=================================================================================================
        // members
        
        private final NavigableMap<Double,CloudResponseDTO> map = new TreeMap<>();
        private int total = 0;

        //=================================================================================================
        // methods

        //-------------------------------------------------------------------------------------------------  
        public void add(final int weight, final CloudResponseDTO result) {
            logger.debug("RandomCollection . add started...");
            
            if (weight <= 0 || map.containsValue(result)) {
            	return;
            }
            
            total += weight;
            map.put((double) total, result);
        }
        
        //------------------------------------------------------------------------------------------------- 
        public CloudResponseDTO next() {
            logger.debug("RandomCollection . next started...");
            
            final double value = ThreadLocalRandom.current().nextDouble() * total;
            return map.ceilingEntry(value).getValue();
        }
        
        //-------------------------------------------------------------------------------------------------
        public boolean isEmpty() {
            logger.debug("RandomCollection . isEmpty started...");
            
            return map.isEmpty();
        }
    }
}