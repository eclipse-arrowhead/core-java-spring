package eu.arrowhead.core.orchestrator.database.service;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.OrchestratorFormRequestDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;

public class OrchestratorDBService {
	
	//=================================================================================================
	// members
	
	private static final String NOT_IN_DB_ERROR_MESAGE = " is not available in database";
	
	private static final Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	@Autowired
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Autowired
	private OrchestratorStoreRepository orchestratorStoreRepository;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public List<OrchestratorStore> queryOrchestrationStore( OrchestratorFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("queryOrchestrationStore started ...");
		
		final List<OrchestratorStore> orchestratorStoreEntyList; 
		
		final SystemRequestDTO requestedSystem = orchestratorFormRequestDTO.getRequesterSystem();
		Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(requestedSystem.getSystemName(), requestedSystem.getAddress(), requestedSystem.getPort());
		if(systemOptional.isEmpty()) {
			throw new InvalidParameterException("RequesterSystem " + NOT_IN_DB_ERROR_MESAGE );
		}
		
		final String requestedServiceDefinition = orchestratorFormRequestDTO.getRequestedServiceDefinition();
		Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(requestedServiceDefinition);
		if(serviceDefinitionOptional.isEmpty()) {
			 Page<OrchestratorStore> entryListPage = orchestratorStoreRepository.findAllByPriority(CommonConstants.TOP_PRIORITY, PageRequest.of(
					 0, 
					 Integer.MAX_VALUE, 
					 Direction.ASC, 
					 CommonConstants.COMMON_FIELD_NAME_ID));
			 
			 if (entryListPage.getContent().isEmpty()) {
				 throw new InvalidParameterException("OrchestratorStore " + NOT_IN_DB_ERROR_MESAGE );
			 }
			 
			 orchestratorStoreEntyList = entryListPage.getContent();
		
		}else {
			
			Optional<List<OrchestratorStore>> entryListOptional  = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(systemOptional.get().getId(), serviceDefinitionOptional.get().getId());
			if (entryListOptional.isEmpty()) {
				throw new InvalidParameterException("OrchestratorStore " + NOT_IN_DB_ERROR_MESAGE );
			}
			
			orchestratorStoreEntyList = entryListOptional.get();
		}

		return removeNonValidEntriesFromOrchestratorStoreEntryList(orchestratorStoreEntyList);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	
	private List<OrchestratorStore> removeNonValidEntriesFromOrchestratorStoreEntryList(List<OrchestratorStore> retrievedList) {
		// TODO implement method logic here 
		return null;
	}

}