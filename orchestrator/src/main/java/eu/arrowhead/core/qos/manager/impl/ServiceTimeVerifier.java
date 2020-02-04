package eu.arrowhead.core.qos.manager.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
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
	public boolean verify(final OrchestrationResultDTO result, final Map<String,String> qosRequirements, final Map<String,String> commands) {
		logger.debug("verify started...");
		Assert.notNull(result, "'result' is null.");
		Assert.notNull(result.getMetadata(), "'result.getMetadata()' is null.");
		Assert.notNull(result.getWarnings(), "'result.getWarnings()' is null.");
		Assert.notNull(commands, "'commands' is null.");
		
		
		int recommendedTime = -1; // no restrictions
		if (result.getMetadata().containsKey(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME)) {
			try {
				recommendedTime = Integer.parseInt(result.getMetadata().get(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME));
			} catch (final NumberFormatException ex) {
				logger.debug("Invalid recommended orchestration time: {}, ignored.", result.getMetadata().get(ServiceRegistryRequestDTO.KEY_RECOMMENDED_ORCHESTRATION_TIME));
			}
		}
		
		int calculatedTime = recommendedTime;
		if (commands.containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY)) {
			final int exclusivityTime = Integer.parseInt(commands.get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY));
			
			if (recommendedTime > 0 && exclusivityTime > Math.round(recommendedTime * recommendedTimeFactor)) { // means this provider's time frame is not long enough
				return false;
			}
			
			calculatedTime = exclusivityTime;
		}
		
		if (calculatedTime > 0) {
			calculatedTime += extraSeconds; // give some extra seconds because of orchestration overhead
			result.getMetadata().put(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME, String.valueOf(calculatedTime));
			
			// adjust TTL warnings
			result.getWarnings().remove(OrchestratorWarnings.TTL_UNKNOWN);
			if (!result.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRED) && 
				!result.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRING) &&
				calculatedTime <= OrchestratorService.EXPIRING_TIME_IN_MINUTES * CommonConstants.CONVERSION_SECOND_TO_MINUTE) {
				result.getWarnings().add(OrchestratorWarnings.TTL_EXPIRING);
			}
		}
		
		return true;
	}
}