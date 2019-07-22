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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.ForeignSystem;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.ForeignSystemRepository;
import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
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
	private ForeignSystemRepository foreignSystemRepository;
	
	@Autowired
	private CloudRepository cloudRepository;
	
	@Autowired
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
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
		
		return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(getAllTopPriorityOrchestratorStoreEntries(page, size, direction, sortField));		
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStore> getAllTopPriorityOrchestratorStoreEntries(final int page, final int size,
			final Direction direction, final String sortField) {
		logger.debug("getAllTopPriorityOrchestratorStoreEntries started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESAGE + validatedSortField);
		}
		
		try {
			return orchestratorStoreRepository.findAllByPriority(CommonConstants.TOP_PRIORITY, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoresByConsumerResponse(final int page,
			final int size, final Direction direction, final String sortField, final long consumerSystemId,
			final String serviceDefinitionName) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
				
		return DTOConverter.convertOrchestratorStorePageEntryListToOrchestratorStoreListResponseDTO(getOrchestratorStoresByConsumer(page, size, direction, sortField, consumerSystemId, serviceDefinitionName));
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStore> getOrchestratorStoresByConsumer(final int page,
			final int size, final Direction direction, final String sortField, final long consumerSystemId,
			final String serviceDefinitionName) {
		logger.debug("getOrchestratorStoresByConsumer started...");
		
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
				
		if ( Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionId " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		
		final Optional<ServiceDefinition> serviceDefinitionOption = serviceDefinitionRepository.findByServiceDefinition(serviceDefinitionName);
		if ( serviceDefinitionOption.isEmpty() ) {
			throw new InvalidParameterException("ServiceDefinitionName " + NOT_IN_DB_ERROR_MESAGE);
		}
		
		try {		
			final Page<OrchestratorStore> orchestratorStorePage = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(consumerOption.get(), serviceDefinitionOption.get(), PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
			
			return orchestratorStorePage;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStoreListResponseDTO createOrchestratorStoresResponse(
			final List<OrchestratorStoreRequestDTO> request) {
		logger.debug("createOrchestratorStoresResponse started...");
		
		try {			
			final List<OrchestratorStore> savedOrchestratorStoreEntries = createOrchestratorStores(request);
			
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
	public List<OrchestratorStore> createOrchestratorStores(final List<OrchestratorStoreRequestDTO> request) {
		logger.debug("createOrchestratorStoresById started...");
		
		if (request == null || request.isEmpty()) {
			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		
		try {
			
			final List<OrchestratorStore> savedOrchestratorStoreEntries = new ArrayList<>();

			for (final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO : request) {
				
				try {					
					savedOrchestratorStoreEntries.add(createOrchestratorStoreEntity(orchestratorStoreRequestDTO));
				
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
	public OrchestratorStore createOrchestratorStoreEntity(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO) {
		logger.debug("createOrchestratorStoreEntity started...");
		
		final System validConsumerSystem = validateSystemId(orchestratorStoreRequestDTO.getConsumerSystemId()); 
		
		final Cloud validCloud = validateProviderCloud(orchestratorStoreRequestDTO.getCloudDTO());	
		final boolean isLocalCloud = localCloudConditionCheck(validCloud);
		
		if (isLocalCloud) {
			
			return createLocalOrchestratorStoreEntry(orchestratorStoreRequestDTO, validConsumerSystem, validCloud);
		
		}else {
			
			return createForeignOrchestratorStoreEntry(orchestratorStoreRequestDTO, validConsumerSystem, validCloud);
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
			
			final System consumerSystem = orchestratorStore.getConsumerSystem();
			final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
			
			final int priority = orchestratorStore.getPriority();
			
			orchestratorStoreRepository.deleteById(id);
			orchestratorStoreRepository.flush();
			
			updateInvolvedPriorities(consumerSystem, serviceDefinition, priority);			
	
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
			
			final System consumerSystemForPriorityMapValidation = orchestratorStoreList.get(0).getConsumerSystem();
			final ServiceDefinition serviceDefinitionForPriorityMapValidation = orchestratorStoreList.get(0).getServiceDefinition();			
			validatemodifiedPriorityMapSize(consumerSystemForPriorityMapValidation, serviceDefinitionForPriorityMapValidation, modifiedPriorityMap.size());	
			
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
	private OrchestratorStore validateLocalOrchestratorStoreRequestDTO(
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System validConsumerSystem, final Cloud validProviderCloud) {
		logger.debug("validateOrchestratorStoreRequestDTO started...");
		
		final boolean foreign = false;
		
		final long validProviderSystemId = validateProviderSystemRequestDTO(orchestratorStoreRequestDTO.getProviderSystemDTO());		
		final ServiceDefinition validServiceDefinition = validateServiceDefinitionName(orchestratorStoreRequestDTO.getServiceDefinitionName());	
		final int validPriority = validatePriority(orchestratorStoreRequestDTO.getPriority());
		final ServiceInterface validInterface = validateServiceInterfaceName(orchestratorStoreRequestDTO.getServiceInterfaceName());
		
		checkUniqueConstraintByConsumerSystemAndServiceAndProviderSystemIdAndInterfaceAndForeign(validConsumerSystem, validServiceDefinition, validProviderSystemId, validInterface, foreign);
	
		return new OrchestratorStore(
				validServiceDefinition,
				validConsumerSystem,
				foreign,
				validProviderSystemId,
				validInterface,
				validPriority,
				Utilities.map2Text(orchestratorStoreRequestDTO.getAttribute()),
				null,
				null);	
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceInterface validateServiceInterfaceName(final String serviceInterfaceName) {
		logger.debug("validateServiceInterfaceName started...");
		
		if (Utilities.isEmpty(serviceInterfaceName)) {
			throw new InvalidParameterException("validateServiceInterfaceName " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String validServiceInterfaceName = serviceInterfaceName.trim().toLowerCase();
		
		final Optional<ServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByInterfaceName(validServiceInterfaceName);
		if (serviceInterfaceOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceInterface by serviceDefinitionName " + validServiceInterfaceName + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return serviceInterfaceOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition validateServiceDefinitionName(final String serviceDefinitionName) {
		logger.debug("validateServiceDefinition started...");
		
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionName " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String validServiceDefinitionName = serviceDefinitionName.trim().toLowerCase();
		
		final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(validServiceDefinitionName);
		if (serviceDefinitionOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinition by serviceDefinitionName " + validServiceDefinitionName + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return serviceDefinitionOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private long validateProviderSystemRequestDTO(final SystemRequestDTO providerSystemRequestDTO) {
		logger.debug("validateProviderSystemRequestDTO started...");
		
		if (providerSystemRequestDTO == null) {
			throw new InvalidParameterException("ProviderSystemRequestDTO " + NULL_ERROR_MESAGE);
		}
		
		if (Utilities.isEmpty(providerSystemRequestDTO.getAddress())) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Address " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String address = providerSystemRequestDTO.getAddress().trim().toLowerCase();
		
		if (Utilities.isEmpty(providerSystemRequestDTO.getSystemName())) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.SystemName " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String systemName = providerSystemRequestDTO.getSystemName().trim().toLowerCase();
		
		if (providerSystemRequestDTO.getPort() == null) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Port " + NULL_ERROR_MESAGE);
		}
		final int port = providerSystemRequestDTO.getPort();
		
		final Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(systemName, address, port);
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by systemName: " + systemName + ", address: " + address + ", port: " + port + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return systemOptional.get().getId();
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
	private void checkSystemIdValidity(final Long systemId) {
		logger.debug("checkSystemIdValidity started...");
		
		if (systemId == null) {
			throw new InvalidParameterException("SystemId " + NULL_ERROR_MESAGE);
		}
		
		if (systemId < 1) {
			throw new InvalidParameterException("SystemId " + LESS_THEN_ONE_ERROR_MESAGE);
		}
		
		if (systemRepository.existsById(systemId)) {
			throw new InvalidParameterException("System by id" + systemId + NOT_IN_DB_ERROR_MESAGE );
		}
		
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
	private void checkUniqueConstraintByConsumerSystemAndServiceAndProviderSystemIdAndInterfaceAndForeign(final System consumerSystem, final ServiceDefinition serviceDefinition, final long providerSystemId, final ServiceInterface serviceInterface, final boolean foreign) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId started...");
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemIdAndServiceInterfaceAndForeign( consumerSystem, serviceDefinition, providerSystemId, serviceInterface, foreign);
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
	private Cloud validateProviderCloud(final CloudRequestDTO cloudRequestDTO) {
		logger.debug("validateProviderCloud started...");
		
		if (cloudRequestDTO == null) {
			return null;
		}

		if(Utilities.isEmpty(cloudRequestDTO.getOperator())) {
			throw new InvalidParameterException("Cloud.Operator " + EMPTY_OR_NULL_ERROR_MESAGE );
		}
		final String operator = cloudRequestDTO.getOperator();
		
		if(Utilities.isEmpty(cloudRequestDTO.getName())) {
			throw new InvalidParameterException("Cloud.Name " + EMPTY_OR_NULL_ERROR_MESAGE );
		}
		final String cloudName = cloudRequestDTO.getName();
		
		final Optional<Cloud> cloudOptional = cloudRepository.findByOperatorAndName(operator, cloudName);
		if (cloudOptional.isEmpty()) {
			throw new InvalidParameterException("Cloud by operator :" + operator + ", and name :"+ cloudName + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return cloudOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------	
	private ServiceInterface validateServiceInterface(final Long serviceInterfaceId) {
		logger.debug("validateServiceInterfaceId started...");
		
		if (serviceInterfaceId == null) {
			throw new InvalidParameterException("ServiceInterfaceId " + NULL_ERROR_MESAGE);
		}
		
		if( serviceInterfaceId < 1) {
			throw new InvalidParameterException("ServiceInterfaceId " + LESS_THEN_ONE_ERROR_MESAGE );
		}	

		final Optional<ServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findById(serviceInterfaceId);
		if (serviceInterfaceOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceInterface by id :" + serviceInterfaceId + NOT_IN_DB_ERROR_MESAGE );
		}
		
		return serviceInterfaceOptional.get();
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
	private void validatemodifiedPriorityMapSize(final System anyConsumerSystemForValidation, final ServiceDefinition anyServiceDefinition, final int modifiedPriorityMapSize) {
		logger.debug("validatemodifiedPriorityMapSize started...");
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(anyConsumerSystemForValidation, anyServiceDefinition);
		if (orchestratorStoreList.isEmpty()) {
			throw new InvalidParameterException("Priorities for consumerSystemId : " + anyConsumerSystemForValidation.getId() + ", and serviceDefinitionId : " + anyServiceDefinition.getId() + ", " + NOT_IN_DB_ERROR_MESAGE );
		}
		
		if (orchestratorStoreList.size() != modifiedPriorityMapSize) {
			throw new InvalidParameterException(MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore saveWithPriorityCheck(final OrchestratorStore orchestratorStore) {
		logger.debug("saveWithPriorityCheck started...");

		final System consumerSystem = orchestratorStore.getConsumerSystem();
		final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
		final int priority = orchestratorStore.getPriority();
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(consumerSystem, serviceDefinition);
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
	
	//-------------------------------------------------------------------------------------------------	
	private void updateInvolvedPriorities(final System consumerSystem, final ServiceDefinition serviceDefinition, final int priority) {
		logger.debug("updateInvolvedPriorities started...");
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(
				consumerSystem, serviceDefinition, Sort.by(Direction.ASC, "priority"));
		
		if (orchestratorStoreList.isEmpty()) {
			return;
		}

		final List<OrchestratorStore> updatedOrchestratorStoreEntryList = new ArrayList<>();
		for (final OrchestratorStore orchestratorStoreInList : orchestratorStoreList) {
			
			final int actualPriority = orchestratorStoreInList.getPriority();
			if(actualPriority > priority) {
				orchestratorStoreInList.setPriority( actualPriority - 1 );
				updatedOrchestratorStoreEntryList.add(orchestratorStoreInList);
			}
		}
		
		for (final OrchestratorStore orchestratorStoreToUpdate : updatedOrchestratorStoreEntryList) {
			
			orchestratorStoreRepository.save(orchestratorStoreToUpdate);
		}
		orchestratorStoreRepository.flush();
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private boolean localCloudConditionCheck(final Cloud cloud) {
		
		if (cloud == null || cloud.getOwnCloud()) {
			return true;
		}
	
		return false;
	}

	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore createLocalOrchestratorStoreEntry(
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System  validConsumerSystem, final Cloud validProviderCloud) {
		logger.debug("createLocalOrchestratorStoreEntry started...");
		
		final OrchestratorStore orchestratorStore = validateLocalOrchestratorStoreRequestDTO(orchestratorStoreRequestDTO, validConsumerSystem, validProviderCloud);
		
		return saveWithPriorityCheck(orchestratorStore);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore createForeignOrchestratorStoreEntry(
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System  validConsumerSystem, final Cloud validProviderCloud) {
		logger.debug("createForeignOrchestratorStoreEntry started...");
		
		final OrchestratorStore orchestratorStore = validateForeignOrchestratorStoreRequestDTO(orchestratorStoreRequestDTO, validConsumerSystem, validProviderCloud);
		
		return saveWithPriorityCheck(orchestratorStore);
	}

	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore validateForeignOrchestratorStoreRequestDTO(
			final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System validConsumerSystem,
			final Cloud validProviderCloud) {
		logger.debug("validateForeignOrchestratorStoreRequestDTO started...");
		
		final boolean foreign = true;
		
		final long validProviderSystemId = validateForeinProviderSystemRequestDTO(orchestratorStoreRequestDTO.getProviderSystemDTO(), validProviderCloud);		
		final ServiceDefinition validServiceDefinition = validateForeinServiceDefinitionName(orchestratorStoreRequestDTO.getServiceDefinitionName());	
		final int validPriority = validatePriority(orchestratorStoreRequestDTO.getPriority());
		final ServiceInterface validInterface = validateForeinServiceInterfaceName(orchestratorStoreRequestDTO.getServiceInterfaceName());
		
		checkUniqueConstraintByConsumerSystemAndServiceAndProviderSystemIdAndInterfaceAndForeign(validConsumerSystem, validServiceDefinition, validProviderSystemId, validInterface, foreign);
	
		return new OrchestratorStore(
				validServiceDefinition,
				validConsumerSystem,
				foreign,
				validProviderSystemId,
				validInterface,
				validPriority,
				Utilities.map2Text(orchestratorStoreRequestDTO.getAttribute()),
				null,
				null);
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceInterface validateForeinServiceInterfaceName(final String serviceInterfaceName) {
		logger.debug("validateForeinServiceInterfaceName started...");
		
		if (Utilities.isEmpty(serviceInterfaceName)) {
			throw new InvalidParameterException("validateServiceInterfaceName " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String validServiceInterfaceName = serviceInterfaceName.trim().toLowerCase();
		
		final Optional<ServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByInterfaceName(validServiceInterfaceName);
		if (serviceInterfaceOptional.isEmpty()) {
			
			return serviceInterfaceRepository.saveAndFlush(new ServiceInterface(validServiceInterfaceName));
		}
		
		return serviceInterfaceOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition validateForeinServiceDefinitionName(final String serviceDefinitionName) {
		logger.debug("validateForeinServiceDefinitionName started...");
		
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionName " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String validServiceDefinitionName = serviceDefinitionName.trim().toLowerCase();
		
		final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(validServiceDefinitionName);
		if (serviceDefinitionOptional.isEmpty()) {
			
			return serviceDefinitionRepository.saveAndFlush(new ServiceDefinition(validServiceDefinitionName));
		}
		
		return serviceDefinitionOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private long validateForeinProviderSystemRequestDTO(final SystemRequestDTO providerSystemRequestDTO, final Cloud providerCloud) {
		logger.debug("validateForeinProviderSystemRequestDTO started...");
		
		if (providerSystemRequestDTO == null) {
			throw new InvalidParameterException("ProviderSystemRequestDTO " + NULL_ERROR_MESAGE);
		}
		
		if (Utilities.isEmpty(providerSystemRequestDTO.getAddress())) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Address " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String address = providerSystemRequestDTO.getAddress().trim().toLowerCase();
		
		if (Utilities.isEmpty(providerSystemRequestDTO.getSystemName())) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.SystemName " + EMPTY_OR_NULL_ERROR_MESAGE);
		}
		final String systemName = providerSystemRequestDTO.getSystemName().trim().toLowerCase();
		
		if (providerSystemRequestDTO.getPort() == null) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Port " + NULL_ERROR_MESAGE);
		}
		final int port = providerSystemRequestDTO.getPort();
		
		final Optional<ForeignSystem> foreignSystemOptional = foreignSystemRepository.findBySystemNameAndAddressAndPortAndProviderCloud(systemName, address, port, providerCloud);
		if (foreignSystemOptional.isEmpty()) {
			foreignSystemRepository.saveAndFlush(new ForeignSystem(providerCloud, systemName, address, port, providerSystemRequestDTO.getAuthenticationInfo()));
		}
		
		return foreignSystemOptional.get().getId();
	}
	
}
