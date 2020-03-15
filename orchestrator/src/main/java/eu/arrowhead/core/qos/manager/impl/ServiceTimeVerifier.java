package eu.arrowhead.core.qos.manager.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;
import eu.arrowhead.core.qos.manager.QoSVerifier;

public class ServiceTimeVerifier implements QoSVerifier {
	
	//=================================================================================================
	// members
	
	private static final double recommendedTimeFactor = 1.1;
	private static final int extraSeconds = 5;
	
	private static final Logger logger = LogManager.getLogger(ServiceTimeVerifier.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean verify(final QoSVerificationParameters parameters, final boolean isPreVerification) {
		logger.debug("verify started...");
		Assert.notNull(parameters, "'parameters' is null.");
		Assert.notNull(parameters.getMetadata(), "'parameters.getMetadata()' is null.");
		Assert.notNull(parameters.getWarnings(), "'parameters.getWarnings()' is null.");
		Assert.notNull(parameters.getCommands(), "'parameters.getCommands()' is null.");
		
		
		int recommendedTime = -1; // no restrictions
		if (parameters.getMetadata().containsKey(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME)) {
			try {
				recommendedTime = Integer.parseInt(parameters.getMetadata().get(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME));
			} catch (final NumberFormatException ex) {
				logger.debug("Invalid recommended orchestration time: {}, ignored.", parameters.getMetadata().get(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME));
			}
		}
		
		int calculatedTime = recommendedTime;
		if (parameters.getCommands().containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY)) {
			final int exclusivityTime = Integer.parseInt(parameters.getCommands().get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY));
			
			if (recommendedTime > 0 && exclusivityTime > Math.round(recommendedTime * recommendedTimeFactor)) { // means this provider's time frame is not long enough
				return false;
			}
			
			calculatedTime = exclusivityTime;
		}
		
		if (calculatedTime > 0 && !isPreVerification) {
			calculatedTime += extraSeconds; // give some extra seconds because of orchestration overhead
			parameters.getMetadata().put(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME, String.valueOf(calculatedTime));
			
			// adjust TTL warnings
			parameters.getWarnings().remove(OrchestratorWarnings.TTL_UNKNOWN);
			if (!parameters.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRED) && 
				!parameters.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRING) &&
				calculatedTime <= OrchestratorService.EXPIRING_TIME_IN_MINUTES * CommonConstants.CONVERSION_SECOND_TO_MINUTE) {
				parameters.getWarnings().add(OrchestratorWarnings.TTL_EXPIRING);
			}
		}
		
		return true;
	}
}