package eu.arrowhead.core.orchestrator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(OrchestratorService.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/**
	 * This method represents the orchestration process where the requester System is NOT in the local Cloud. This means that the Gatekeeper made sure
	 * that this request from the remote Orchestrator can be satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization
	 * Systems.)
	 */
	public OrchestrationResponseDTO externalServiceRequest(final OrchestrationFormRequestDTO request) {
		logger.debug("externalServiceRequest started ...");
		
		//TODO implement method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO triggerInterCloud(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("triggerInterCloud started ...");
		
		//TODO implement method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO orchestrationFromStore(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("orchestrationFromStore started ...");
		

		//TODO implement additional method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO dynamicOrchestration(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("dynamicOrchestration started ...");
		
		//TODO implement method logic here
		return null;
	}
}