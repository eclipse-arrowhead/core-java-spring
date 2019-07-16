package eu.arrowhead.core.orchestrator.database.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestByIdDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class OrchestratorStoreDBService {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	@Autowired
	private OrchestratorStoreRepository orchestratorStoreRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;

	private static final String LESS_THEN_ONE_ERROR_MESAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESAGE = "The following shortable field  is not available : ";
	private static final String NOT_IN_DB_ERROR_MESAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESAGE = " is empty or null";
	private static final String NULL_ERROR_MESAGE = " is null";
	private static final String ORCHESTRATOR_STORE_REQUEST_BY_ID_DTO_VALIDATION_EXCEPTION_MESSAGE = "Exception in OrchestratorStoreRequestByIdDTO validation, entry not going to be added to save list." ;
	private static final String VIOLATES_UNIQUE_CONSTRAINT = " violates uniqueConstraint rules";
	private static final String MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE = "The given PriorityMap has different size than the size of consumer-serviceDeffinition pars in DB";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO getOrchestratorStoreById(final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started...");
		
		try {
			
			if (orchestratorStoreId < 1) {
				throw new InvalidParameterException("OrchestratorStoreId " + LESS_THEN_ONE_ERROR_MESAGE );
			}
		
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
	public OrchestratorStoreListResponseDTO getOrchestratorStoreEntriesResponse(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(getOrchestratorStoreEntries(page, size, direction, sortField));
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStore> getOrchestratorStoreEntries(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		try {
			return orchestratorStoreRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStoreEntriesResponse(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		try {
			return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAllByPriority(CommonConstants.TOP_PRIORITY, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoresByConsumerResponse(final int page,
			final int size, final Direction direction, final String sortField, final long consumerSystemId,
			final long serviceDefinitionId) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		if ( consumerSystemId < 1) {
			throw new InvalidParameterException("ConsumerSystemId " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		final Optional<System> consumerOption = systemRepository.findById(consumerSystemId);
			if ( consumerOption.isEmpty() ) {
				throw new InvalidParameterException("ConsumerSystemId " + NOT_IN_DB_ERROR_MESAGE);
			}
				
		if ( serviceDefinitionId < 1) {
			throw new InvalidParameterException("ServiceDefinitionId " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		
		final Optional<ServiceDefinition> serviceDefinitionOption = serviceDefinitionRepository.findById(serviceDefinitionId);
		if ( serviceDefinitionOption.isEmpty() ) {
			throw new InvalidParameterException("ServiceDefinitionId " + NOT_IN_DB_ERROR_MESAGE);
		}
		
		try {		
			return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(consumerSystemId, serviceDefinitionId, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStoreListResponseDTO createOrchestratorStoresByIdResponse(
			final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("createOrchestratorStoresResponse started...");
		
		try {			
			final List<OrchestratorStore> savedOrchestratorStoreEntries = createOrchestratorStoresById(request);
			
			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(savedOrchestratorStoreEntries);
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<OrchestratorStore> createOrchestratorStoresById(final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("createOrchestratorStoresById started...");
		
		if (request == null || request.isEmpty()) {
			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		
		try {
			
			final List<OrchestratorStore> savedOrchestratorStoreEntries = new ArrayList<>();

			for (final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO : request) {
				
				try {					
					savedOrchestratorStoreEntries.add(createOrchestratorStoreEntityById(orchestratorStoreRequestByIdDTO));
				
				} catch (final Exception e) {
					logger.debug( ORCHESTRATOR_STORE_REQUEST_BY_ID_DTO_VALIDATION_EXCEPTION_MESSAGE + e.getMessage() + e);
				}
			}
			
			return savedOrchestratorStoreEntries;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			
		}
	}

	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStore createOrchestratorStoreEntityById(final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO) {
		logger.debug("createOrchestratorStoreEntityById started...");
		
			final OrchestratorStore orchestratorStore = validateOrchestratorStoreRequestById(orchestratorStoreRequestByIdDTO);						
			
			return saveWithPriorityCheck(orchestratorStore);
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeOrchestratorStoreById(final long id) {
		logger.debug("removeOrchestratorStoreById started...");
		
		try {
			
			if (id < 1) {
				throw new InvalidParameterException("OrchestratorStore" + LESS_THEN_ONE_ERROR_MESAGE );
			}
			
			final Optional<OrchestratorStore> orchestratorStoreOption = orchestratorStoreRepository.findById(id);
			if (orchestratorStoreOption.isEmpty()) {
				throw new InvalidParameterException("OrchestratorStore" + NOT_IN_DB_ERROR_MESAGE );
			}
			final OrchestratorStore orchestratorStore = orchestratorStoreOption.get();
			
			final System consumerSystem = orchestratorStore.getConsumerSystem();
			final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
			
			final int priority = orchestratorStore.getPriority();
			
			orchestratorStoreRepository.deleteById(id);
			orchestratorStoreRepository.flush();
			

			final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(
					consumerSystem, serviceDefinition, Sort.by(Direction.ASC, "priority"));
			
			if (orchestratorStoreList.isEmpty()) {
				return;
			}

			final List<OrchestratorStore> updatedOrchestratorStoreEntryList = new ArrayList<>();
			for (OrchestratorStore orchestratorStoreInList : orchestratorStoreList) {
				
				int actualPriority = orchestratorStoreInList.getPriority();
				if(actualPriority > priority) {
					orchestratorStoreInList.setPriority( actualPriority - 1 );
					updatedOrchestratorStoreEntryList.add(orchestratorStoreInList);
				}
			}
			
			saveAllInAscPriorityOrder(updatedOrchestratorStoreEntryList);
	
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void modifyOrchestratorStorePriorityResponse(final OrchestratorStoreModifyPriorityRequestDTO request) {
		logger.debug("modifyOrchestratorStorePriorityResponse started...");
		
		try {
			if (request == null || request.getPriorityMap().isEmpty()) {
				throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
			}
			
			final Map<Long, Integer> modifiedPriorityMap = request.getPriorityMap();
			
			final List<OrchestratorStore> orchestratorStoreList = getInvolvedOrchestratorStoreListByPriorityMap(modifiedPriorityMap);	
			
			final long consumerSystemIdForPriorityMapValidation = orchestratorStoreList.get(orchestratorStoreList.size() - 1).getConsumerSystem().getId();
			final long serviceDefinitionIdForPriorityMapValidation = orchestratorStoreList.get(0).getServiceDefinition().getId();			
			validatemodifiedPriorityMapSize(consumerSystemIdForPriorityMapValidation, serviceDefinitionIdForPriorityMapValidation, modifiedPriorityMap.size());	
			
			refreshOrchestratorStoreListBymodifiedPriorityMap(orchestratorStoreList, modifiedPriorityMap);
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore validateOrchestratorStoreRequestById(
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO) {
		logger.debug("validateOrchestratorStoreRequestById started...");
		
		final System validConsumerSystem = validateSystemId(orchestratorStoreRequestByIdDTO.getConsumerSystemId()); 
		final System validProviderSystem = validateSystemId(orchestratorStoreRequestByIdDTO.getProviderSystemId());		
		final ServiceDefinition validServiceDefinition = validateServiceDefinitionId(orchestratorStoreRequestByIdDTO.getServiceDefinitionId());	
		final int validPriority = validatePriority(orchestratorStoreRequestByIdDTO.getPriority());
		final Cloud validProviderCloud = validateProviderCloudId(orchestratorStoreRequestByIdDTO.getCloudId());		
		
		checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId(validConsumerSystem.getId(), validServiceDefinition.getId(), validProviderSystem.getId());
	
		return new OrchestratorStore(
				validServiceDefinition,
				validConsumerSystem,
				validProviderSystem,
				validProviderCloud,
				validPriority,
				Utilities.map2Text(orchestratorStoreRequestByIdDTO.getAttribute()),
				null,
				null);	
	}

	//-------------------------------------------------------------------------------------------------
	private System validateSystemId(final Long systemId) {
		logger.debug("validateSystemId started...");
		
		if (systemId == null) {
			throw new InvalidParameterException("SystemId " + NULL_ERROR_MESAGE);
		}
		
		if (systemId < 1) {
			throw new InvalidParameterException("SystemId " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		
		final Optional<System> systemOptional = systemRepository.findById(systemId);
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by id" + systemId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return systemOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition validateServiceDefinitionId(final Long serviceDefinitionId) {
		logger.debug("validateServiceDefinitionId started...");
		
		if (serviceDefinitionId == null) {
			throw new InvalidParameterException("ServiceDefinitionId " + NULL_ERROR_MESAGE);
		}
		
		if (serviceDefinitionId < 1) {
			throw new InvalidParameterException("ServiceDefinition " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		
		final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findById(serviceDefinitionId);
		if (serviceDefinitionOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinition by id: " + serviceDefinitionId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return serviceDefinitionOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private void checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId(final long consumerSystemId, final long serviceDefinitionId, final long providerSystemId) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId started...");
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerIdAndServiceDefinitionIdAndProviderId( consumerSystemId, serviceDefinitionId, providerSystemId);
		if (orchestratorStoreOptional.isPresent()) {
			throw new InvalidParameterException("OrchestratorStore checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId " + VIOLATES_UNIQUE_CONSTRAINT );
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private Cloud validateProviderCloudId(final Long cloudId) {
		logger.debug("validateProviderCloudId started...");
		
		if (cloudId == null) {
			return null;
		}
		
		if( cloudId < 1) {
			throw new InvalidParameterException("CloudId " + LESS_THEN_ONE_ERROR_MESAGE );
		}	

		final Optional<Cloud> cloudOptional = cloudRepository.findById(cloudId);
		if (cloudOptional.isEmpty()) {
			throw new InvalidParameterException("Cloud by id :" + cloudId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return cloudOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private int validatePriority(final Integer priority) {
		logger.debug("validatePriority started...");
		
		if (priority == null) {
			throw new InvalidParameterException("Priority " + NULL_ERROR_MESAGE);
		} 
		
		if( priority < 1) {
			throw new InvalidParameterException("Priority " + LESS_THEN_ONE_ERROR_MESAGE );
		}		

		return priority;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<Long, Integer> getPriorityMap(final List<OrchestratorStore> orchestratorStoreList) {
		logger.debug("getPriorityMap started...");
		
		final Map<Long, Integer> priorityMap = new HashMap<>(orchestratorStoreList.size());
		
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			priorityMap.put(orchestratorStore.getId(), orchestratorStore.getPriority());
		}
		
		return priorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void refreshOrchestratorStoreListBymodifiedPriorityMap(final List<OrchestratorStore> orchestratorStoreList, final Map<Long, Integer> priorityMap ) {
		logger.debug("updateOrchestratorStoreListBymodifiedPriorityMap started...");
		
		if (orchestratorStoreList.size() != priorityMap.size()) {
			throw new InvalidParameterException(MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE);
		}
		
		final List<OrchestratorStore> updatedOrchestratorStore = new ArrayList<>(orchestratorStoreList.size());
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			orchestratorStore.setPriority(priorityMap.get(orchestratorStore.getId()) * -1);
			updatedOrchestratorStore.add(orchestratorStore);
		}
		orchestratorStoreRepository.saveAll(updatedOrchestratorStore);
		orchestratorStoreRepository.flush();

		updatedOrchestratorStore.clear();
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			orchestratorStore.setPriority(priorityMap.get(orchestratorStore.getId()));
			updatedOrchestratorStore.add(orchestratorStore);
		}
		orchestratorStoreRepository.saveAll(updatedOrchestratorStore);
		orchestratorStoreRepository.flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void saveAllInAscPriorityOrder(final List<OrchestratorStore> updatedOrchestratorStoreList) {
		logger.debug("saveAllInAscPriorityOrder started...");
		
		Collections.sort(updatedOrchestratorStoreList, getOrchestratorStorePriorityComparator());
		
		for (final OrchestratorStore orchestratorStore : updatedOrchestratorStoreList) {
			
			orchestratorStoreRepository.saveAndFlush(orchestratorStore);
			orchestratorStoreRepository.refresh(orchestratorStore);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Comparator<OrchestratorStore> getOrchestratorStorePriorityComparator() {
		logger.debug("getOrchestratorStorePriorityComparato started...");
		
		return new Comparator<OrchestratorStore>() {
		    @Override
		    public int compare(final OrchestratorStore o1, final OrchestratorStore o2) {
		        if(o1.getPriority() < o2.getPriority()) {
		        	return -1;
		        }
		        if(o1.getPriority() > o2.getPriority()) {
		        	return 1;
		        }
		        
		        return 0;

		    }
		};
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getInvolvedOrchestratorStoreListByPriorityMap(final Map<Long, Integer> modifiedPriorityMap) {
		logger.debug("getOrchestratorStoreList started...");

		final List<OrchestratorStore> orchestratorStoreList = new ArrayList<>(modifiedPriorityMap.size()); 
		
		for (final Long orchestratorStoreId : modifiedPriorityMap.keySet()) {		
			
			final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findById(orchestratorStoreId);
			if(orchestratorStoreOptional.isEmpty()) {
				throw new InvalidParameterException("OrchestratorStore by id: " + orchestratorStoreId + NOT_IN_DB_ERROR_MESAGE );
			}
			orchestratorStoreList.add(orchestratorStoreOptional.get());
		}

		return orchestratorStoreList;
	}

	//-------------------------------------------------------------------------------------------------
	private void validatemodifiedPriorityMapSize(final long anyConsumerIdForValidation, final long anyServiceDefinitionId, final int modifiedPriorityMapSize) {
		logger.debug("validatemodifiedPriorityMapSize started...");
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(anyConsumerIdForValidation, anyServiceDefinitionId);
		if (orchestratorStoreList.isEmpty()) {
			throw new InvalidParameterException("Priorities for consumerSystemId : " + anyConsumerIdForValidation + ", and serviceDefinitionId : " + anyServiceDefinitionId + ", " + NOT_IN_DB_ERROR_MESAGE );
		}
		
		if (orchestratorStoreList.size() != modifiedPriorityMapSize) {
			throw new InvalidParameterException(MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore saveWithPriorityCheck(final OrchestratorStore orchestratorStore) {
		logger.debug("saveWithPriorityCheck started...");

		final long consumerSystemId = orchestratorStore.getConsumerSystem().getId();
		final long serviceDefinitionId = orchestratorStore.getServiceDefinition().getId();
		final int priority = orchestratorStore.getPriority();
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(consumerSystemId, serviceDefinitionId);
		if (orchestratorStoreList.isEmpty()) {
			orchestratorStore.setPriority(CommonConstants.TOP_PRIORITY);
			
			return orchestratorStoreRepository.saveAndFlush(orchestratorStore);
			
		}else {
			final Map<Long, Integer> priorityMap = getPriorityMap(orchestratorStoreList);
			
			if (priorityMap.containsValue(priority)){
				orchestratorStore.setPriority(priority);
				
				return insertOrchestratorStoreWithPriority(orchestratorStoreList, orchestratorStore, priority);
				
			}else {
				orchestratorStore.setPriority(orchestratorStoreList.size() + 1);
				
				return orchestratorStoreRepository.saveAndFlush(orchestratorStore);
			
			}
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore insertOrchestratorStoreWithPriority(final List<OrchestratorStore> orchestratorStoreList,
			final OrchestratorStore orchestratorStoreToInsert, final int priority) {
		logger.debug("insertOrchestratorStoreWithPriority started...");

		Collections.sort(orchestratorStoreList, getOrchestratorStorePriorityComparator());
		Collections.reverse(orchestratorStoreList);
		
		for (int i = 0; i < orchestratorStoreList.size(); i++) {
			
			final OrchestratorStore ochestratorStoreFromList = orchestratorStoreList.get(i);
			final int priorityFromList = ochestratorStoreFromList.getPriority();
			
			if (priority <= priorityFromList) {
				 ochestratorStoreFromList.setPriority(priorityFromList + 1);
				 orchestratorStoreRepository.saveAndFlush( ochestratorStoreFromList);
			} 
			
		}
		
		return orchestratorStoreRepository.saveAndFlush(orchestratorStoreToInsert);
	}
	
}
