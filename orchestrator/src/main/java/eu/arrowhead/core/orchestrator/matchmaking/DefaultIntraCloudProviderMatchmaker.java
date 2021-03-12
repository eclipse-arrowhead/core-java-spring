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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;

public class DefaultIntraCloudProviderMatchmaker implements IntraCloudProviderMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DefaultIntraCloudProviderMatchmaker.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first (preferred) provider.
	 */
	@Override
	public OrchestrationResultDTO doMatchmaking(final List<OrchestrationResultDTO> orList, final IntraCloudProviderMatchmakingParameters params) {
		logger.debug("DefaultIntraCloudProviderMatchmaker.doMatchmaking started...");
		
		Assert.isTrue(orList != null && !orList.isEmpty(), "orList is null or empty.");
		Assert.notNull(params, "params is null");
		
		if (params.getPreferredLocalProviders().isEmpty()) {
			logger.debug("No preferred provider is specified, the first one in the list is selected.");
			return orList.get(0);
		}
		
		for (final OrchestrationResultDTO orResult : orList) {
			for (final PreferredProviderDataDTO provider : params.getPreferredLocalProviders()) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(orResult.getProvider(), provider.getProviderSystem())) {
					logger.debug("The first preferred provider found in the list is selected.");
					return orResult;
				}
			}
		}
		
		logger.debug("no match was found between preferred providers, the first one is selected.");
		
		return orList.get(0);
	}
}