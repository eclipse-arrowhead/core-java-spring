package eu.arrowhead.core.orchestrator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	
	private static final Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO externalServiceRequest(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
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