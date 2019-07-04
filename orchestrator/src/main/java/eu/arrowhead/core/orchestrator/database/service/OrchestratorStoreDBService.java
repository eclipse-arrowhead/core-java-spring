package eu.arrowhead.core.orchestrator.database.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
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
	
	private final static Logger logger = LogManager.getLogger(OrchestratorStoreDBService.class);
	
	@Autowired
	private OrchestratorStoreRepository orchestratorStoreRepository;
	
	@Autowired
	private ServiceRegistryRepository serviceRegistryRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;

	private static final String LESS_THEN_ONE_ERROR_MESAGE= " must be greater then zero.";
	private static final String NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE = "The following shortable field  is not available : ";
	private static final String NOT_IN_DB_ERROR_MESAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESAGE = " is empty or null";
	private static final String NULL_ERROR_MESAGE = " is null";
	private static final String ORCHESTRATORSTORE_REQUESTBYIDDTO_VALIDATION_EXCEPTION_MESSAGE = "Exception in OrchestratorStoreRequestByIdDTO validation, entry not going to be added to save list." ;
	private static final String VIOLATES_UNIQUECONSTRAINT = " violates uniqueConstraint rules";
	private static final String MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE = "OrchestratorStore entry List size != PriorityMap size ";

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
	public OrchestratorStoreListResponseDTO getOrchestratorStoreEntriesResponse(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		try {
			return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
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
			throw new InvalidParameterException(NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
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
		
		try {
			
			if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
				throw new InvalidParameterException(NOT_AVAILABLE_SHORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
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
					
				return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(consumerSystemId, serviceDefinitionId, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField)));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

//	//-------------------------------------------------------------------------------------------------	
//	public OrchestratorStoreListResponseDTO createOrchestratorStoresResponse(
//			final List<OrchestratorStoreRequestDTO> request) {
//		logger.debug("createOrchestratorStoresResponse started...");
//		
//		try {
//			final Set<OrchestratorStore> validatedOrchestratorStoreEtrysToSave = validateOrchestratorStoreRequestList(request);
//			
//			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.saveAll(validatedOrchestratorStoreEtrysToSave));
//			
//		} catch (final Exception ex) {
//			logger.debug(ex.getMessage(), ex);
//			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//			
//		}
//		
//	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStoreListResponseDTO createOrchestratorStoresByIdResponse(
			final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("createOrchestratorStoresResponse started...");
		
		try {
			final Set<OrchestratorStore> validatedOrchestratorStoreEtrysToSave = validateOrchestratorStoreRequestByIdList(request);
			
			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(orchestratorStoreRepository.saveAll(validatedOrchestratorStoreEtrysToSave));
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			
		}
		
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
			final long consumerSystemId = orchestratorStore.getConsumerSystem().getId();
			final long serviceDefinitionId = orchestratorStore.getServiceDefinition().getId();
			final int priority = orchestratorStore.getPriority();
			
			orchestratorStoreRepository.deleteById(id);
			orchestratorStoreRepository.flush();
			
			final Optional<List<OrchestratorStore>> orchestratorStoreListOption = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(
					consumerSystemId, serviceDefinitionId );
			if (orchestratorStoreListOption.isEmpty()) {
				return;
			}
			final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreListOption.get();
			final Map<Long, Integer> originalPriorityMap = getPriorityMap(orchestratorStoreList);
			final Map<Long, Integer> modifyedPriorityMap = modifyFromPriorityMap(id, priority, originalPriorityMap);
			final List<OrchestratorStore> updatedOrchestratorStoreList = updateOrchestratorStoreListByModifyedPriorityMap(orchestratorStoreList, modifyedPriorityMap);
			
			saveAllInAscPriorityOrder(updatedOrchestratorStoreList);
			
			
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
			
			final Map<Long, Integer> modifyedPriorityMap = request.getPriorityMap();
			
			final List<OrchestratorStore> orchestratorStoreList = getOrchestratorStoreListByPriorityMap(modifyedPriorityMap);	
			
			final long consumerIdForPriorityMapValidation = orchestratorStoreList.get(orchestratorStoreList.size() - 1).getConsumerSystem().getId();
			final long serviceDefinitionIdForPriorityMapValidation = orchestratorStoreList.get(0).getServiceDefinition().getId();			
			validateModifyedPriorityMapSize(consumerIdForPriorityMapValidation, serviceDefinitionIdForPriorityMapValidation, modifyedPriorityMap.size());	
			
			refreshOrchestratorStoreListByModifyedPriorityMap(orchestratorStoreList, modifyedPriorityMap);
			
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
	private Set<OrchestratorStore> validateOrchestratorStoreRequestByIdList(
		final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("validateOrchestratorStoreRequestByIdList started...");
		
		if (request == null || request.isEmpty()) {
			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
	
		Collections.sort(request, getOrchestratorStoreRequestByIdDTOPriorityComparator());
		
		final List<OrchestratorStore> temporalOrchestratorList = new ArrayList(request.size());
		int priorityPositionInRequest = 0;
		for (final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO : request) {
			
			final OrchestratorStore validatedOrchestratorStore = validateOrchestratorStoreRequestById(orchestratorStoreRequestByIdDTO, priorityPositionInRequest);
			if (validatedOrchestratorStore != null) {
				++priorityPositionInRequest;
				temporalOrchestratorList.add(validatedOrchestratorStore);
			}
		}
		
		return Set.copyOf(temporalOrchestratorList);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore validateOrchestratorStoreRequestById(
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO, final int priorityPositionInRequest) {
		logger.debug("validateOrchestratorStoreRequestById started...");
		
		try {
			if (orchestratorStoreRequestByIdDTO.getConsumerSystemId() == null) {
				throw new InvalidParameterException("ConsumerSystem " + NULL_ERROR_MESAGE);
			}
			final System validConsumerSystem = validateSystemId(orchestratorStoreRequestByIdDTO.getConsumerSystemId()); 
			
			if (orchestratorStoreRequestByIdDTO.getProviderSystemId() == null) {
				throw new InvalidParameterException("ProviderSystem " + NULL_ERROR_MESAGE);
			}
			final System validProviderSystem = validateSystemId(orchestratorStoreRequestByIdDTO.getProviderSystemId());
			
			if (orchestratorStoreRequestByIdDTO.getServiceDefinitionId() == null) {
				throw new InvalidParameterException("ServiceDefinition " + NULL_ERROR_MESAGE);
			}
			final ServiceDefinition validServiceDefinition = validateServiceDefinitionId(orchestratorStoreRequestByIdDTO.getServiceDefinitionId());
			
			if (orchestratorStoreRequestByIdDTO.getPriority() == null) {
				throw new InvalidParameterException("Priority " + NULL_ERROR_MESAGE);
			} 
			final int validPriority = validatePriority(orchestratorStoreRequestByIdDTO.getPriority(), validConsumerSystem.getId(), validServiceDefinition.getId(), priorityPositionInRequest);
			
			checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority(validConsumerSystem.getId(), validServiceDefinition.getId(), validPriority);
			checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId(validConsumerSystem.getId(), validServiceDefinition.getId(), validProviderSystem.getId());

			final Cloud validProviderCloud = validateProviderCloudId(orchestratorStoreRequestByIdDTO.getCloudId());
			
			return new OrchestratorStore(
					validServiceDefinition,
					validConsumerSystem,
					validProviderSystem,
					validProviderCloud,
					validPriority,
					orchestratorStoreRequestByIdDTO.getAttribute(),
					null,
					null);
		
		} catch (final Exception e) {
			logger.debug( ORCHESTRATORSTORE_REQUESTBYIDDTO_VALIDATION_EXCEPTION_MESSAGE + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private System validateSystemId(final Long systemId) {
		logger.debug("validateSystemId started...");
		
		if (systemId == null) {
			throw new InvalidParameterException("System " + NULL_ERROR_MESAGE);
		}
		
		if (systemId < 1) {
			throw new InvalidParameterException("System " + LESS_THEN_ONE_ERROR_MESAGE);
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
			throw new InvalidParameterException("ServiceDefinition " + NULL_ERROR_MESAGE);
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
	private void checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority(final long consumerSystemId, final long serviceDefinitionId, final int priority) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority started...");
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerIdAndServiceDefinitionIdAndPriority( consumerSystemId, serviceDefinitionId, priority);
		if (orchestratorStoreOptional.isPresent()) {
			throw new InvalidParameterException("OrchestratorStore checkUniqueConstraintByConsumerSystemIdAndServiceIdAndPriority " + VIOLATES_UNIQUECONSTRAINT );
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private void checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId(final long consumerSystemId, final long serviceDefinitionId, final long providerSystemId) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId started...");
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerIdAndServiceDefinitionIdAndProviderId( consumerSystemId, serviceDefinitionId, providerSystemId);
		if (orchestratorStoreOptional.isPresent()) {
			throw new InvalidParameterException("OrchestratorStore checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId " + VIOLATES_UNIQUECONSTRAINT );
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private Cloud validateProviderCloudId(final Long cloudId) {
		logger.debug("validateProviderCloudId started...");
		
		if (cloudId == null) {
			return null;
		}

		final Optional<Cloud> cloudOptional = cloudRepository.findById(cloudId);
		if (cloudOptional.isEmpty()) {
			throw new InvalidParameterException("Cloud by id :" + cloudId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return cloudOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private int validatePriority(final Integer priority, final long consumerSystemId, final long serviceDefinitionId, final int priorityPositionInRequest) {
		logger.debug("validatePriority started...");
		
		if(priority != null && priority < 1) {
			throw new InvalidParameterException("Priority " + LESS_THEN_ONE_ERROR_MESAGE );
		}
		
		final Optional<List<OrchestratorStore>> orchestratorStoreOptionalList = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(consumerSystemId, serviceDefinitionId);
		if (orchestratorStoreOptionalList.isEmpty()) {
			return CommonConstants.TOP_PRIORITY + priorityPositionInRequest;
		}else {
			return orchestratorStoreOptionalList.get().size() + 1 + priorityPositionInRequest;	
		}		
	}

	//-------------------------------------------------------------------------------------------------
	private Map<Long, Integer> getPriorityMap(final List<OrchestratorStore> orchestratorStoreList) {
		logger.debug("getPriorityMap started...");
		
		final Map<Long, Integer> priorityMap = new HashMap(orchestratorStoreList.size());
		
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			priorityMap.put(orchestratorStore.getId(), orchestratorStore.getPriority());
		}
		
		return priorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long, Integer> modifyFromPriorityMap(final Long id, final int priority, final Map<Long, Integer> priorityMap) {
		logger.debug("modifyFromPriorityMap started...");
		
		final Map<Long, Integer> modifyedPriorityMap = new HashMap(priorityMap.size() - 1);
		
		for (final Long orchestratorStoreId : priorityMap.keySet()) {
			if (orchestratorStoreId != id) {
				int orchestratorStorePriority = priorityMap.get(orchestratorStoreId);
				if (orchestratorStorePriority > priority) {
					orchestratorStorePriority = orchestratorStorePriority - 1;
				}
				modifyedPriorityMap.put(orchestratorStoreId, orchestratorStorePriority);
			}
		}
		
		return modifyedPriorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> updateOrchestratorStoreListByModifyedPriorityMap(final List<OrchestratorStore> orchestratorStoreList, final Map<Long, Integer> priorityMap ) {
		logger.debug("updateOrchestratorStoreListByModifyedPriorityMap started...");
		
		if (orchestratorStoreList.size() != priorityMap.size()) {
			throw new InvalidParameterException(MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE);
		}
		
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			orchestratorStore.setPriority(priorityMap.get(orchestratorStore.getId()));
		}
		
		return orchestratorStoreList;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	private void refreshOrchestratorStoreListByModifyedPriorityMap(final List<OrchestratorStore> orchestratorStoreList, final Map<Long, Integer> priorityMap ) {
		logger.debug("updateOrchestratorStoreListByModifyedPriorityMap started...");
		
		if (orchestratorStoreList.size() != priorityMap.size()) {
			throw new InvalidParameterException(MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE);
		}
		
		final List<OrchestratorStore> updatedOrchestratorStore = new ArrayList(orchestratorStoreList.size());
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
		
		final Comparator<OrchestratorStore> compareByPriority = new Comparator<OrchestratorStore>() {
		    @Override
		    public int compare(final OrchestratorStore o1, final OrchestratorStore o2) {
		        return o1.getPriority().compareTo(o2.getPriority());
		    }
		};
		
		return compareByPriority;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Comparator<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOPriorityComparator() {
		logger.debug("getOrchestratorStoreRequestByIdDTOPriorityComparator started...");
		
		final Comparator<OrchestratorStoreRequestByIdDTO> compareByPriority = new Comparator<OrchestratorStoreRequestByIdDTO>() {
		    @Override
		    public int compare(final OrchestratorStoreRequestByIdDTO o1, final OrchestratorStoreRequestByIdDTO o2) {
		    	
		        return o1.getPriority().compareTo(o2.getPriority());
		    }
		};
		
		return compareByPriority;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getOrchestratorStoreListByPriorityMap(final Map<Long, Integer> modifyedPriorityMap) {
		logger.debug("getOrchestratorStoreList started...");

		final List<OrchestratorStore> orchestratorStoreList = new ArrayList<OrchestratorStore>(modifyedPriorityMap.size()); 
		
		for (final Long orchestratorStoreId : modifyedPriorityMap.keySet()) {		
			
			final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findById(orchestratorStoreId);
			if(orchestratorStoreOptional.isEmpty()) {
				throw new InvalidParameterException("OrchestratorStore by id: " + orchestratorStoreId + NOT_IN_DB_ERROR_MESAGE );
			}
			orchestratorStoreList.add(orchestratorStoreOptional.get());
		}

		return orchestratorStoreList;
	}

	//-------------------------------------------------------------------------------------------------
	private void validateModifyedPriorityMapSize(final long anyConsumerIdForValidation, final long anyServiceDefinitionId, final int modifyedPriorityMapSize) {
		logger.debug("validateModifyedPriorityMapSize started...");
		
		final Optional<List<OrchestratorStore>> orchestratorStoreListOption = orchestratorStoreRepository.findAllByConsumerIdAndServiceDefinitionId(anyConsumerIdForValidation, anyServiceDefinitionId);
		if (orchestratorStoreListOption.isEmpty()) {
			throw new InvalidParameterException("Priorities for consumerSystemId : " + anyConsumerIdForValidation + ", and serviceDefinitionId : " + anyServiceDefinitionId + ", " + NOT_IN_DB_ERROR_MESAGE );
		}
		final List<OrchestratorStore> validOrchestratorStoreList = orchestratorStoreListOption.get();
		
		if (validOrchestratorStoreList.size() != modifyedPriorityMapSize) {
			throw new InvalidParameterException(MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE);
		}
		
	}
	
//	//-------------------------------------------------------------------------------------------------	
//	private Set<OrchestratorStore> validateOrchestratorStoreRequestList(
//			final List<OrchestratorStoreRequestDTO> request) {
//		logger.debug("validateOrchestratorStoreRequestList started...");
//		
//		if (request == null || request.isEmpty()) {
//			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
//		}
//		
//		final List<OrchestratorStore> temporalOrchestratorList = new ArrayList(request.size());
//		for (final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO : request) {
//			
//			final OrchestratorStore validatedOrchestratorStore = validateOrchestratorStoreRequest(orchestratorStoreRequestDTO);
//			temporalOrchestratorList.add(validatedOrchestratorStore);
//		}
//		
//		return Set.copyOf(temporalOrchestratorList);
//	}

//	//-------------------------------------------------------------------------------------------------
//	
//	private OrchestratorStore validateOrchestratorStoreRequest(
//			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO) {
//		logger.debug("validateOrchestratorStoreRequest started ...");
//		
//		if (orchestratorStoreRequestDTO.getConsumerSystemDTO == null) {
//			throw new InvalidParameterException("ConsumerSystem " + NULL_ERROR_MESAGE);
//		}
//		System validConsumerSystem = validateSystem(orchestratorStoreRequestDTO.getConsumerSystemDTO()); 
//		
//		if (orchestratorStoreRequestDTO.getProviderSystemDTO() == null) {
//			throw new InvalidParameterException("ProviderSystem " + NULL_ERROR_MESAGE);
//		}
//		System validProviderSystem = validateSystem(orchestratorStoreRequestDTO.getProviderSystemDTO());
//		
//		if (orchestratorStoreRequestDTO.getServiceDefinition() == null) {
//			throw new InvalidParameterException("ServiceDefinition " + NULL_ERROR_MESAGE);
//		}
//		ServiceDefinition validatedServiceDefinition = validateServiceDefinition(orchestratorStoreRequestDTO.getServiceDefinition());
//		
//		if (orchestratorStoreRequestDTO.getPriority() == null) {
//			throw new InvalidParameterException("Priority " + NULL_ERROR_MESAGE);
//		} 
//		int validatedPriority = orchestratorStoreRequestDTO.getPriority();
//		
//		
//		
//		return null;
//	}
//
//	//-------------------------------------------------------------------------------------------------
//	private System validateSystem(final SystemRequestDTO systemDTO) {
//		logger.debug("validateSystem started ...");
//		
//		if (Utilities.isEmpty(systemDTO.getSystemName())) {
//			throw new InvalidParameterException("System name is null or empty");
//		}
//		
//		if (Utilities.isEmpty(systemDTO.getAddress())) {
//			throw new InvalidParameterException("System address is null or empty");
//		}
//		
//		if () {
//			
//		}
//		if (port. < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
//			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
//		}
//		
//		final String validatedSystemName = systemName.trim().toLowerCase();
//		final String validatedAddress = address.trim().toLowerCase();
//		final String validatedAuthenticationInfo = authenticationInfo;
//		
//		checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);
//		
//		return new System(validatedSystemName, validatedAddress, port, validatedAuthenticationInfo);
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	private void checkConstraintsOfSystemTable(final String validatedSystemName, final String validatedAddress, final int validatedPort) {
//		logger.debug("checkConstraintsOfSystemTable started...");
//		
//		try {
//			final Optional<System> find = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedAddress, validatedPort);
//			if (find.isPresent()) {
//				throw new InvalidParameterException("System with name: " + validatedSystemName + ", address: " + validatedAddress +	", port: " + validatedPort + " already exists.");
//			}
//		} catch (final InvalidParameterException ex) {
//			throw ex;
//		} catch (final Exception ex) {
//			logger.debug(ex.getMessage(), ex);
//			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
//		}
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	private System validateNonNullSystemParameters(final String systemName, final String address, final int port, final String authenticationInfo) {
//		logger.debug("validateNonNullSystemParameters started...");
//		
//		if (Utilities.isEmpty(systemName)) {
//			throw new InvalidParameterException("System name is null or empty");
//		}
//		
//		if (Utilities.isEmpty(address)) {
//			throw new InvalidParameterException("System address is null or empty");
//		}
//		
//		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
//			throw new InvalidParameterException(PORT_RANGE_ERROR_MESSAGE);
//		}
//		
//		final String validatedSystemName = systemName.trim().toLowerCase();
//		final String validatedAddress = address.trim().toLowerCase();
//		final String validatedAuthenticationInfo = authenticationInfo;
//		
//		checkConstraintsOfSystemTable(validatedSystemName, validatedAddress, port);
//		
//		return new System(validatedSystemName, validatedAddress, port, validatedAuthenticationInfo);
//	}
	
}
