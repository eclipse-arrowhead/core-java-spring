package eu.arrowhead.core.orchestrator.database.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class OrchestratorStoreDBService {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	@Autowired
	private OrchestratorStoreRepository orchestratorStoreRepository;
	
	@Autowired
	private ServiceRegistryRepository serviceRegistryRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO getOrchestratorStoreById(final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started...");
		
		try {
			final Optional<OrchestratorStore> orchestratorStoreOption = orchestratorStoreRepository.findById(orchestratorStoreId);
			if (orchestratorStoreOption.isEmpty()){
				throw new InvalidParameterException("OrchestratorStore with id " + orchestratorStoreId + " not found.");		
			}		
			return DTOConverter.convertOrchestratorStoreToOrchestratorStoreResponseDTO(orchestratorStoreOption.get());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoreEntriesResponse(int page, int size,
			Direction direction, String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStoreEntriesResponse(int page, int size,
			Direction direction, String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!System.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAllByPriority(CommonConstants.TOP_PRIORITY, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}
