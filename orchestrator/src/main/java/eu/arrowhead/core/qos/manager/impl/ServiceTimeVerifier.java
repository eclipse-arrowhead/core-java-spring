/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.manager.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;
import eu.arrowhead.core.orchestrator.service.OrchestratorUtils;
import eu.arrowhead.core.qos.manager.QoSVerifier;

public class ServiceTimeVerifier implements QoSVerifier {
	
	//=================================================================================================
	// members
	
	private static final int extraSeconds = 5;
	
	private static final Logger logger = LogManager.getLogger(ServiceTimeVerifier.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean verify(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verify started...");
		Assert.notNull(parameters, "'parameters' is null.");
		parameters.validateParameters();
		
		int calculatedTime = OrchestratorUtils.calculateServiceTime(parameters.getMetadata(), parameters.getCommands());
		if (calculatedTime == 0 || parameters.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRED)) {
			return false;
		}
		
		if (calculatedTime > 0 && !isPreVerification) {
			calculatedTime += extraSeconds; // give some extra seconds because of orchestration overhead
			parameters.getMetadata().put(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME, String.valueOf(calculatedTime));
			
			// adjust TTL warnings
			parameters.getWarnings().remove(OrchestratorWarnings.TTL_UNKNOWN);
			if (!parameters.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRING) &&
				calculatedTime <= OrchestratorService.EXPIRING_TIME_IN_MINUTES * CommonConstants.CONVERSION_SECOND_TO_MINUTE) {
				parameters.getWarnings().add(OrchestratorWarnings.TTL_EXPIRING);
			}
		}
		
		return true;
	}
}