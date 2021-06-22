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

package eu.arrowhead.core.orchestrator.service;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;

public class OrchestratorUtils {
	
	//=================================================================================================
	// members
	
	private static final double recommendedServiceTimeFactor = 1.1;
	
	private static final Logger logger = LogManager.getLogger(OrchestratorUtils.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static int calculateServiceTime(final Map<String, String> serviceMetadata, final Map<String, String> orchestrationCommands) {
		logger.debug("calculateServiceTime started...");
		
		int recommendedTime = -1; // no restrictions
		if (serviceMetadata.containsKey(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME)) {
			try {
				recommendedTime = Integer.parseInt(serviceMetadata.get(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME));
			} catch (final NumberFormatException ex) {
				logger.debug("Invalid recommended orchestration time: {}, ignored.", serviceMetadata.get(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME));
			}
		}
		
		int calculatedTime = recommendedTime;
		if (orchestrationCommands.containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY)) {
			final int exclusivityTime = Integer.parseInt(orchestrationCommands.get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY));
			
			if (recommendedTime > 0 && exclusivityTime > Math.round(recommendedTime * recommendedServiceTimeFactor)) {
				return 0; // means this provider's time frame is not long enough
			}
			
			calculatedTime = exclusivityTime;
		}
		
		return calculatedTime;
	}
}
