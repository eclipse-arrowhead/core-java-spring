package eu.arrowhead.core.orchestrator;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.dto.OrchestratorFormRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorDBService;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final String NOT_IN_DB_ERROR_MESAGE = " is not available in database";
	
	private static final Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	@Autowired
	private OrchestratorDBService orchestratorDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO externalServiceRequest(
			final OrchestratorFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("externalServiceRequest started ...");
		
		//TODO implement method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public ServiceRegistryListResponseDTO triggerInterCloud(
			final OrchestratorFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("triggerInterCloud started ...");
		
		//TODO implement method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public ServiceRegistryListResponseDTO orchestrationFromStore(
			final OrchestratorFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("orchestrationFromStore started ...");
		
		final List<OrchestratorStore> entryList = orchestratorDBService.queryOrchestrationStore(orchestratorFormRequestDTO);

		//TODO implement additional method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public ServiceRegistryListResponseDTO dynamicOrchestration(
			final OrchestratorFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("dynamicOrchestration started ...");
		
		//TODO implement method logic here
		return null;
	}
}