/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.orchestrator.database.service;

import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
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
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreRequestDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;

@Service
public class OrchestratorStoreDBService {
	
	//=================================================================================================
	// members
	
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = "The following sortable field  is not available : ";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";
	private static final String ORCHESTRATOR_STORE_REQUEST_BY_ID_DTO_VALIDATION_EXCEPTION_MESSAGE = "Exception in OrchestratorStoreRequestByIdDTO validation, entry not going to be added to save list." ;
	private static final String VIOLATES_UNIQUE_CONSTRAINT = " violates uniqueConstraint rules";
	private static final String MODIFY_PRIORITY_MAP_EXCEPTION_MESSAGE = "The given PriorityMap has different size than the size of consumer-serviceDeffinition pars in DB";
	private static final String NOT_VALID_ERROR_MESSAGE = " is not valid.";
	private static final String NOT_FOREIGN_ERROR_MESSAGE = " is not foreign";
	
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
	
	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO getOrchestratorStoreByIdResponse(final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started...");
		
		final OrchestratorStore orchestratorStore = getOrchestratorStoreById(orchestratorStoreId);
		if (orchestratorStore.isForeign()) {
			return getForeignResponseDTO(orchestratorStore);
		} else {
			return getLocalResponseDTO(orchestratorStore);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	public OrchestratorStore getOrchestratorStoreById(final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started...");
		
		if (orchestratorStoreId < 1) {
			throw new InvalidParameterException("OrchestratorStoreId " + LESS_THAN_ONE_ERROR_MESSAGE );
		}
		
		try {
			final Optional<OrchestratorStore> orchestratorStoreOption = orchestratorStoreRepository.findById(orchestratorStoreId);
			if (orchestratorStoreOption.isEmpty()){
				throw new InvalidParameterException("OrchestratorStore with id " + orchestratorStoreId + " not found.");		
			}
	
			return orchestratorStoreOption.get();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoreEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final Page<OrchestratorStore> orchestratorStorePage = getOrchestratorStoreEntries(page, size, direction, sortField);
		final long totalElements = orchestratorStorePage.getTotalElements();
		
		return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(getOrchestratorDTOListFromPage(orchestratorStorePage), totalElements);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStore> getOrchestratorStoreEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE + validatedSortField);
		}
		
		try {
			return orchestratorStoreRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStoreEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
		
		final Page<OrchestratorStore> orchestratorStorePage = getAllTopPriorityOrchestratorStoreEntries(page, size, direction, sortField);
		final long totalElements = orchestratorStorePage.getTotalElements();
		
		return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(getOrchestratorDTOListFromPage(orchestratorStorePage), totalElements);		
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStore> getAllTopPriorityOrchestratorStoreEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAllTopPriorityOrchestratorStoreEntries started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE + validatedSortField);
		}
		
		try {
			return orchestratorStoreRepository.findAllByPriority(CoreCommonConstants.TOP_PRIORITY, PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreListResponseDTO getOrchestratorStoresByConsumerResponse(final int page, final int size, final Direction direction, final String sortField, final long consumerSystemId,
																					final String serviceDefinitionName, final String serviceInterfaceName) {
		logger.debug("getOrchestratorStoreEntriesResponse started...");
				
		final Page<OrchestratorStore> orchestratorStorePage = getOrchestratorStoresByConsumer(page,	size, direction, sortField, consumerSystemId, serviceDefinitionName, serviceInterfaceName);
		final long totalElements = orchestratorStorePage.getTotalElements();
		
		return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(getOrchestratorDTOListFromPage(orchestratorStorePage), totalElements);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	public Page<OrchestratorStore> getOrchestratorStoresByConsumer(final int page, final int size, final Direction direction, final String sortField, final long consumerSystemId,
																   final String serviceDefinitionName, final String serviceInterfaceName) {
		logger.debug("getOrchestratorStoresByConsumer started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStore.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE + validatedSortField);
		}
		
		if (consumerSystemId < 1) {
			throw new InvalidParameterException("ConsumerSystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		final Optional<System> consumerOption = systemRepository.findById(consumerSystemId);
			if (consumerOption.isEmpty() ) {
				throw new InvalidParameterException("ConsumerSystemId " + NOT_IN_DB_ERROR_MESSAGE);
			}
				
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionId " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		final Optional<ServiceDefinition> serviceDefinitionOption = serviceDefinitionRepository.findByServiceDefinition(serviceDefinitionName.toLowerCase().trim());
		if (serviceDefinitionOption.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinitionName " + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		final ServiceInterface validServiceInterface; 
		if (serviceInterfaceName != null) {
			if (interfaceNameVerifier.isValid(serviceInterfaceName)) {
				final Optional<ServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByInterfaceName(serviceInterfaceName.toUpperCase().trim());
				if (serviceInterfaceOptional.isEmpty())  {
					throw new InvalidParameterException("ServiceInterfaceName " + NOT_IN_DB_ERROR_MESSAGE);
				}
				
				validServiceInterface = serviceInterfaceOptional.get();
			} else {
				throw new InvalidParameterException("ServiceInterfaceName " + NOT_VALID_ERROR_MESSAGE);
			}
		} else {
			validServiceInterface = null;
		}
		
		try {		
			final PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField);
			if (validServiceInterface != null) {
				return orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(consumerOption.get(), serviceDefinitionOption.get(), validServiceInterface,
																												  pageRequest);
			} else {
				return orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(consumerOption.get(), serviceDefinitionOption.get(), pageRequest);
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<OrchestratorStore> getOrchestratorStoresByConsumerIdAndServiceDefinition(final long consumerSystemId, final String serviceDefinitionName) {
		logger.debug("getOrchestratorStoresByConsumerIdAndServiceDefinition started...");
		
		if (consumerSystemId < 1) {
			throw new InvalidParameterException("ConsumerSystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionId " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		final Optional<System> consumerOption = systemRepository.findById(consumerSystemId);
		if (consumerOption.isEmpty()) {
			throw new InvalidParameterException("ConsumerSystemId " + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		final Optional<ServiceDefinition> serviceDefinitionOption = serviceDefinitionRepository.findByServiceDefinition(serviceDefinitionName.toLowerCase().trim());
		if (serviceDefinitionOption.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinitionName " + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		try {		
			return orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(consumerOption.get(), serviceDefinitionOption.get(), Sort.by(CoreCommonConstants.SORT_FIELD_PRIORITY));			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<OrchestratorStore> getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(final long consumerSystemId, final String serviceDefinitionName,
																											final String serviceInterfaceName) {
		logger.debug("getOrchestratorStoresByConsumerIdAndServiceDefinition started...");
		
		if (consumerSystemId < 1) {
			throw new InvalidParameterException("ConsumerSystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionName " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(serviceInterfaceName)) {
			throw new InvalidParameterException("ServiceInterface " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		final Optional<System> consumerOption = systemRepository.findById(consumerSystemId);
		if (consumerOption.isEmpty()) {
			throw new InvalidParameterException("ConsumerSystemId " + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		final Optional<ServiceDefinition> serviceDefinitionOption = serviceDefinitionRepository.findByServiceDefinition(serviceDefinitionName.toLowerCase().trim());
		if (serviceDefinitionOption.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinitionName " + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		final Optional<ServiceInterface> serviceInterfaceOption = serviceInterfaceRepository.findByInterfaceName(serviceInterfaceName);
		if (serviceInterfaceOption.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinitionName " + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		try {		
			return orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(consumerOption.get(),	serviceDefinitionOption.get(), serviceInterfaceOption.get(),
																											  Sort.by(CoreCommonConstants.SORT_FIELD_PRIORITY));			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStoreListResponseDTO createOrchestratorStoresResponse(final List<OrchestratorStoreRequestDTO> request) {
		logger.debug("createOrchestratorStoresResponse started...");
		
		try {			
			final List<OrchestratorStore> savedOrchestratorStoreEntries = createOrchestratorStores(request);
			
			return DTOConverter.convertOrchestratorStoreEntryListToOrchestratorStoreListResponseDTO(getOrchestratorDTOListFromList(savedOrchestratorStoreEntries), savedOrchestratorStoreEntries.size());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<OrchestratorStore> createOrchestratorStores(final List<OrchestratorStoreRequestDTO> request) {
		logger.debug("createOrchestratorStoresById started...");
		
		if (request == null || request.isEmpty()) {
			throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		try {
			final List<OrchestratorStore> savedOrchestratorStoreEntries = new ArrayList<>();
			for (final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO : request) {
				final OrchestratorStore orchStore = saveOrchestratorStoreEntry(orchestratorStoreRequestDTO);
				if (orchStore != null) {
					savedOrchestratorStoreEntries.add(orchStore);
				}
			}
			
			return savedOrchestratorStoreEntries;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStore createOrchestratorStoreEntity(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO) {
		logger.debug("createOrchestratorStoreEntity started...");
		
		final System validConsumerSystem = validateSystemId(orchestratorStoreRequestDTO.getConsumerSystemId()); 
		final Cloud validCloud = validateProviderCloud(orchestratorStoreRequestDTO.getCloud());	
		final boolean isLocalCloud = localCloudConditionCheck(validCloud);
		
		if (isLocalCloud) {
			return createLocalOrchestratorStoreEntry(orchestratorStoreRequestDTO, validConsumerSystem);
		} else {
			return createForeignOrchestratorStoreEntry(orchestratorStoreRequestDTO, validConsumerSystem, validCloud);
		}		
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeOrchestratorStoreById(final long id) {
		logger.debug("removeOrchestratorStoreById started...");
		
		if (id < 1) {
			throw new InvalidParameterException("OrchestratorStore" + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		try {
			final Optional<OrchestratorStore> orchestratorStoreOption = orchestratorStoreRepository.findById(id);
			if (orchestratorStoreOption.isEmpty()) {
				throw new InvalidParameterException("OrchestratorStore" + NOT_IN_DB_ERROR_MESSAGE);
			}
			
			final OrchestratorStore orchestratorStore = orchestratorStoreOption.get();
			final System consumerSystem = orchestratorStore.getConsumerSystem();
			final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
			final ServiceInterface serviceInterface = orchestratorStore.getServiceInterface();
			final int priority = orchestratorStore.getPriority();
			
			orchestratorStoreRepository.deleteById(id);
			orchestratorStoreRepository.flush();
			
			updateInvolvedPriorities(consumerSystem, serviceDefinition, serviceInterface, priority);			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void modifyOrchestratorStorePriorityResponse(final OrchestratorStoreModifyPriorityRequestDTO request) {
		logger.debug("modifyOrchestratorStorePriorityResponse started...");
		
		try {
			if (request == null || request.getPriorityMap().isEmpty()) {
				throw new InvalidParameterException("OrchestratorStoreRequestDTOList " + EMPTY_OR_NULL_ERROR_MESSAGE);
			}
			
			final Map<Long,Integer> modifiedPriorityMap = request.getPriorityMap();
			final List<OrchestratorStore> orchestratorStoreList = getInvolvedOrchestratorStoreListByPriorityMap(modifiedPriorityMap);	
			final System consumerSystemForPriorityMapValidation = orchestratorStoreList.get(0).getConsumerSystem();
			final ServiceDefinition serviceDefinitionForPriorityMapValidation = orchestratorStoreList.get(0).getServiceDefinition();	
			final ServiceInterface serviceInterfaceForPriorityMapValidation = orchestratorStoreList.get(0).getServiceInterface();
			validateModifiedPriorityMapSize(consumerSystemForPriorityMapValidation,	serviceDefinitionForPriorityMapValidation, serviceInterfaceForPriorityMapValidation, modifiedPriorityMap.size());
			
			refreshOrchestratorStoreListByModifiedPriorityMap(orchestratorStoreList, modifiedPriorityMap);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<OrchestratorStore> getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(final long consumerSystemId) {
		logger.debug("getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId started...");
		
		if (consumerSystemId < 1) {
			throw new InvalidParameterException( "ConsumerSystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		try {
			return orchestratorStoreRepository.findAllByPriorityAndSystemId(CoreCommonConstants.TOP_PRIORITY, consumerSystemId);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	public OrchestratorStoreResponseDTO getForeignResponseDTO(final OrchestratorStore orchestratorStore) {
		logger.debug("getForeignResponseDTO started...");
		
		if (orchestratorStore == null) {
			throw new InvalidParameterException("OrchestratorStore " + NULL_ERROR_MESSAGE);
		}
		
		if (!orchestratorStore.isForeign()) {
			throw new InvalidParameterException("OrchestratorStore " + NOT_FOREIGN_ERROR_MESSAGE);
		}
		
		final Optional<ForeignSystem> foreignSystemOptional = foreignSystemRepository.findById(orchestratorStore.getProviderSystemId());
		if (foreignSystemOptional.isEmpty()) {
			throw new InvalidParameterException("ForeignSystemOptional by id: " + orchestratorStore.getProviderSystemId() + NOT_IN_DB_ERROR_MESSAGE);
		}
		final ForeignSystem foreignSystem = foreignSystemOptional.get();
		
		final SystemResponseDTO providerSystem = DTOConverter.convertForeignSystemToSystemResponseDTO(foreignSystem);		
		final CloudResponseDTO providerCloud = DTOConverter.convertCloudToCloudResponseDTO(foreignSystem.getProviderCloud());
		
		return DTOConverter.convertOrchestratorStoreToOrchestratorStoreResponseDTO(orchestratorStore, providerSystem, providerCloud);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore validateLocalOrchestratorStoreRequestDTO(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System validConsumerSystem) {
		logger.debug("validateOrchestratorStoreRequestDTO started...");
		
		final long validProviderSystemId = validateProviderSystemRequestDTO(orchestratorStoreRequestDTO.getProviderSystem());		
		final ServiceDefinition validServiceDefinition = validateServiceDefinitionName(orchestratorStoreRequestDTO.getServiceDefinitionName());	
		final int validPriority = validatePriority(orchestratorStoreRequestDTO.getPriority());
		final ServiceInterface validInterface = validateServiceInterfaceName(orchestratorStoreRequestDTO.getServiceInterfaceName());
		final String validAttribute = Utilities.map2Text(orchestratorStoreRequestDTO.getAttribute());
		
		checkUniqueConstraintByConsumerSystemAndServiceAndProviderSystemIdAndInterfaceAndForeign(validConsumerSystem, validServiceDefinition, validProviderSystemId, validInterface, false);
	
		return new OrchestratorStore(validServiceDefinition, validConsumerSystem, false, validProviderSystemId, validInterface, validPriority, validAttribute);	
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private ServiceInterface validateServiceInterfaceName(final String serviceInterfaceName) {
		logger.debug("validateServiceInterfaceName started...");
		
		if (!interfaceNameVerifier.isValid(serviceInterfaceName)) {
			throw new InvalidParameterException("ServiceInterfaceName " + NOT_VALID_ERROR_MESSAGE);
		}
		
		final String validServiceInterfaceName = serviceInterfaceName.toUpperCase().trim();
		final Optional<ServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByInterfaceName(validServiceInterfaceName);
		if (serviceInterfaceOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceInterface by serviceDefinitionName " + validServiceInterfaceName + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		return serviceInterfaceOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private ServiceDefinition validateServiceDefinitionName(final String serviceDefinitionName) {
		logger.debug("validateServiceDefinition started...");
		
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionName " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		final String validServiceDefinitionName = serviceDefinitionName.trim().toLowerCase();
		final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(validServiceDefinitionName);
		if (serviceDefinitionOptional.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinition by serviceDefinitionName " + validServiceDefinitionName + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		return serviceDefinitionOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateOnlyProviderSystemRequestDTO(final SystemRequestDTO providerSystemRequestDTO) {
		logger.debug("validateOnlyProviderSystemRequestDTO started...");
		
		if (providerSystemRequestDTO == null) {
			throw new InvalidParameterException("ProviderSystemRequestDTO " + NULL_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(providerSystemRequestDTO.getAddress())) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Address " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(providerSystemRequestDTO.getSystemName())) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.SystemName " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		if (providerSystemRequestDTO.getPort() == null) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Port " + NULL_ERROR_MESSAGE);
		}

		final int validPort = providerSystemRequestDTO.getPort();
		if (validPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("ProviderSystemRequestDTO.Port " + NOT_VALID_ERROR_MESSAGE);
		}		
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private long validateProviderSystemRequestDTO(final SystemRequestDTO providerSystemRequestDTO) {
		logger.debug("validateProviderSystemRequestDTO started...");
		
		validateOnlyProviderSystemRequestDTO(providerSystemRequestDTO);

		final String address = providerSystemRequestDTO.getAddress().trim().toLowerCase();
		final String systemName = providerSystemRequestDTO.getSystemName().trim().toLowerCase();
		final int port = providerSystemRequestDTO.getPort();
		
		final Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(systemName, address, port);
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by systemName: " + systemName + ", address: " + address + ", port: " + port + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		return systemOptional.get().getId();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private System validateSystemId(final Long systemId) {
		logger.debug("validateSystemId started...");
		
		if (systemId == null) {
			throw new InvalidParameterException("SystemId " + NULL_ERROR_MESSAGE);
		}
		
		if (systemId < 1) {
			throw new InvalidParameterException("SystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		final Optional<System> systemOptional = systemRepository.findById(systemId);
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by id " + systemId + NOT_IN_DB_ERROR_MESSAGE );
		}
		
		return systemOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkUniqueConstraintByConsumerSystemAndServiceAndProviderSystemIdAndInterfaceAndForeign(final System consumerSystem, final ServiceDefinition serviceDefinition,
																										  final long providerSystemId, final ServiceInterface serviceInterface, final boolean foreign) {
		logger.debug("checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId started...");
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemIdAndServiceInterfaceAndForeign(
																														consumerSystem, serviceDefinition, providerSystemId, serviceInterface, foreign);
		if (orchestratorStoreOptional.isPresent()) {
			throw new InvalidParameterException("OrchestratorStore checkUniqueConstraintByConsumerSystemIdAndServiceIdAndProviderSystemId " + VIOLATES_UNIQUE_CONSTRAINT);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@SuppressWarnings("squid:S3655")
	private Cloud validateProviderCloud(final CloudRequestDTO cloudRequestDTO) {
		logger.debug("validateProviderCloud started...");
		
		if (cloudRequestDTO == null) {
			return null;
		}

		if (Utilities.isEmpty(cloudRequestDTO.getOperator())) {
			throw new InvalidParameterException("Cloud.Operator " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		final String operator = cloudRequestDTO.getOperator().trim().toLowerCase();
		
		if (Utilities.isEmpty(cloudRequestDTO.getName())) {
			throw new InvalidParameterException("Cloud.Name " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		final String cloudName = cloudRequestDTO.getName().trim().toLowerCase();
		
		final Optional<Cloud> cloudOptional = cloudRepository.findByOperatorAndName(operator, cloudName);
		if (cloudOptional.isEmpty()) {
			throw new InvalidParameterException("Cloud by operator: " + operator + " and name: " + cloudName + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		return cloudOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	private int validatePriority(final Integer priority) {
		logger.debug("validatePriority started...");
		
		if (priority == null) {
			throw new InvalidParameterException("Priority " + NULL_ERROR_MESSAGE);
		} 
		
		if (priority < 1) {
			throw new InvalidParameterException("Priority " + LESS_THAN_ONE_ERROR_MESSAGE);
		}		

		return priority;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<Long,Integer> getPriorityMap(final List<OrchestratorStore> orchestratorStoreList) {
		logger.debug("getPriorityMap started...");
		
		final Map<Long,Integer> priorityMap = new HashMap<>(orchestratorStoreList.size());
		
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			priorityMap.put(orchestratorStore.getId(), orchestratorStore.getPriority());
		}
		
		return priorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void refreshOrchestratorStoreListByModifiedPriorityMap(final List<OrchestratorStore> orchestratorStoreList, final Map<Long,Integer> priorityMap ) {
		logger.debug("updateOrchestratorStoreListByModifiedPriorityMap started...");
		
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
	@SuppressWarnings("squid:S3655")
	private List<OrchestratorStore> getInvolvedOrchestratorStoreListByPriorityMap(final Map<Long,Integer> modifiedPriorityMap) {
		logger.debug("getOrchestratorStoreList started...");

		final List<OrchestratorStore> orchestratorStoreList = new ArrayList<>(modifiedPriorityMap.size()); 
		for (final Long orchestratorStoreId : modifiedPriorityMap.keySet()) {		
			final Optional<OrchestratorStore> orchestratorStoreOptional = orchestratorStoreRepository.findById(orchestratorStoreId);
			if (orchestratorStoreOptional.isEmpty()) {
				throw new InvalidParameterException("OrchestratorStore by id: " + orchestratorStoreId + NOT_IN_DB_ERROR_MESSAGE);
			}
			
			orchestratorStoreList.add(orchestratorStoreOptional.get());
		}

		return orchestratorStoreList;
	}

	//-------------------------------------------------------------------------------------------------
	private void validateModifiedPriorityMapSize(final System anyConsumerSystemForValidation, final ServiceDefinition anyServiceDefinition,	final ServiceInterface anyServiceInterface,
												 final int modifiedPriorityMapSize) {
		logger.debug("validateModifiedPriorityMapSize started...");
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(anyConsumerSystemForValidation,
																																						 anyServiceDefinition, anyServiceInterface);
		if (orchestratorStoreList.isEmpty()) {
			throw new InvalidParameterException("Priorities for consumerSystemId: " + anyConsumerSystemForValidation.getId() + ", serviceDefinitionId: " + anyServiceDefinition.getId() + 
											    "and serviceInterfaceId: " + anyServiceInterface.getId() + NOT_IN_DB_ERROR_MESSAGE);
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
		final ServiceInterface serviceInterface = orchestratorStore.getServiceInterface();
		final int priority = orchestratorStore.getPriority();
		
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(consumerSystem, serviceDefinition,
																																					     serviceInterface);
		if (orchestratorStoreList.isEmpty()) {
			orchestratorStore.setPriority(CoreCommonConstants.TOP_PRIORITY);
			
			return orchestratorStoreRepository.saveAndFlush(orchestratorStore);
		} else {
			final Map<Long,Integer> priorityMap = getPriorityMap(orchestratorStoreList);
			
			if (priorityMap.containsValue(priority)) {
				orchestratorStore.setPriority(priority);
				
				return insertOrchestratorStoreWithPriority(orchestratorStoreList, orchestratorStore, priority);
			} else {
				orchestratorStore.setPriority(orchestratorStoreList.size() + 1);
				
				return orchestratorStoreRepository.saveAndFlush(orchestratorStore);
			}
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore insertOrchestratorStoreWithPriority(final List<OrchestratorStore> orchestratorStoreList, final OrchestratorStore orchestratorStoreToInsert, final int priority) {
		logger.debug("insertOrchestratorStoreWithPriority started...");

		orchestratorStoreList.sort((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()));
		Collections.reverse(orchestratorStoreList);
		
		for (final OrchestratorStore ochestratorStore : orchestratorStoreList) {
			final int priorityFromList = ochestratorStore.getPriority();
			
			if (priority <= priorityFromList) {
				ochestratorStore.setPriority(priorityFromList + 1);
				orchestratorStoreRepository.saveAndFlush(ochestratorStore);
			} 
		}
		
		return orchestratorStoreRepository.saveAndFlush(orchestratorStoreToInsert);
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void updateInvolvedPriorities(final System consumerSystem, final ServiceDefinition serviceDefinition, final ServiceInterface serviceInterface, final int priority) {
		logger.debug("updateInvolvedPriorities started...");
		
		final Sort sortField = Sort.by(Direction.ASC, OrchestratorStore.FIELD_NAME_PRIORITY);
		final List<OrchestratorStore> orchestratorStoreList = orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(consumerSystem, serviceDefinition,
																																						 serviceInterface, sortField);
		
		if (orchestratorStoreList.isEmpty()) {
			return;
		}

		final List<OrchestratorStore> updatedOrchestratorStoreEntryList = new ArrayList<>();
		for (final OrchestratorStore orchestratorStoreInList : orchestratorStoreList) {
			final int actualPriority = orchestratorStoreInList.getPriority();
			if (actualPriority > priority) {
				orchestratorStoreInList.setPriority(actualPriority - 1);
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
		return cloud == null || cloud.getOwnCloud();
	}

	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore createLocalOrchestratorStoreEntry(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System validConsumerSystem) {
		logger.debug("createLocalOrchestratorStoreEntry started...");
		
		final OrchestratorStore orchestratorStore = validateLocalOrchestratorStoreRequestDTO(orchestratorStoreRequestDTO, validConsumerSystem);
		
		return saveWithPriorityCheck(orchestratorStore);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore createForeignOrchestratorStoreEntry(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System  validConsumerSystem, final Cloud validProviderCloud) {
		logger.debug("createForeignOrchestratorStoreEntry started...");
		
		final OrchestratorStore orchestratorStore = validateForeignOrchestratorStoreRequestDTO(orchestratorStoreRequestDTO, validConsumerSystem, validProviderCloud);
		
		return saveWithPriorityCheck(orchestratorStore);
	}

	//-------------------------------------------------------------------------------------------------	
	private OrchestratorStore validateForeignOrchestratorStoreRequestDTO(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO, final System validConsumerSystem, 
																		 final Cloud validProviderCloud) {
		logger.debug("validateForeignOrchestratorStoreRequestDTO started...");
		
		final long validProviderSystemId = validateForeignProviderSystemRequestDTO(orchestratorStoreRequestDTO.getProviderSystem(), validProviderCloud);		
		final ServiceDefinition validServiceDefinition = validateForeignServiceDefinitionName(orchestratorStoreRequestDTO.getServiceDefinitionName());	
		final int validPriority = validatePriority(orchestratorStoreRequestDTO.getPriority());
		final ServiceInterface validInterface = validateForeignServiceInterfaceName(orchestratorStoreRequestDTO.getServiceInterfaceName());
		final String validAttribute = Utilities.map2Text(orchestratorStoreRequestDTO.getAttribute());
		
		checkUniqueConstraintByConsumerSystemAndServiceAndProviderSystemIdAndInterfaceAndForeign(validConsumerSystem, validServiceDefinition, validProviderSystemId, validInterface, true);
	
		return new OrchestratorStore(validServiceDefinition, validConsumerSystem, true, validProviderSystemId, validInterface, validPriority, validAttribute);
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private ServiceInterface validateForeignServiceInterfaceName(final String serviceInterfaceName) {
		logger.debug("validateForeinServiceInterfaceName started...");
		
		if (!interfaceNameVerifier.isValid(serviceInterfaceName)) {
			throw new InvalidParameterException("ServiceInterfaceName " + NOT_VALID_ERROR_MESSAGE);
		}
		final String validServiceInterfaceName = serviceInterfaceName.toUpperCase().trim();
		
		final Optional<ServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByInterfaceName(validServiceInterfaceName);
		if (serviceInterfaceOptional.isEmpty()) {
			return serviceInterfaceRepository.saveAndFlush(new ServiceInterface(validServiceInterfaceName));
		}
		
		return serviceInterfaceOptional.get();
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private ServiceDefinition validateForeignServiceDefinitionName(final String serviceDefinitionName) {
		logger.debug("validateForeinServiceDefinitionName started...");
		
		if (Utilities.isEmpty(serviceDefinitionName)) {
			throw new InvalidParameterException("ServiceDefinitionName " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		final String validServiceDefinitionName = serviceDefinitionName.trim().toLowerCase();
		
		final Optional<ServiceDefinition> serviceDefinitionOptional = serviceDefinitionRepository.findByServiceDefinition(validServiceDefinitionName);
		if (serviceDefinitionOptional.isEmpty()) {
			return serviceDefinitionRepository.saveAndFlush(new ServiceDefinition(validServiceDefinitionName));
		}
		
		return serviceDefinitionOptional.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private long validateForeignProviderSystemRequestDTO(final SystemRequestDTO providerSystemRequestDTO, final Cloud providerCloud) {
		logger.debug("validateForeignProviderSystemRequestDTO started...");
		
		validateOnlyProviderSystemRequestDTO(providerSystemRequestDTO);
		
		final String address = providerSystemRequestDTO.getAddress().trim().toLowerCase();
		final String systemName = providerSystemRequestDTO.getSystemName().trim().toLowerCase();
		final int validPort = providerSystemRequestDTO.getPort();
		
		final Optional<ForeignSystem> foreignSystemOptional = foreignSystemRepository.findBySystemNameAndAddressAndPortAndProviderCloud(systemName, address, validPort, providerCloud);
		if (foreignSystemOptional.isEmpty()) {
			return foreignSystemRepository.saveAndFlush(new ForeignSystem(providerCloud, systemName, address, validPort, providerSystemRequestDTO.getAuthenticationInfo(), Utilities.map2Text(providerSystemRequestDTO.getMetadata()))).getId();
		}
		
		return foreignSystemOptional.get().getId();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private OrchestratorStoreResponseDTO getLocalResponseDTO(final OrchestratorStore orchestratorStore) {
		final Optional<System> systemOptional = systemRepository.findById(orchestratorStore.getProviderSystemId());
		
		SystemResponseDTO providerSystem = new SystemResponseDTO();
		if (systemOptional.isEmpty()) {
			providerSystem.setId(orchestratorStore.getProviderSystemId());
			providerSystem.setSystemName("unknown-" + orchestratorStore.getProviderSystemId());
			providerSystem.setAddress("unknown");
		} else {
			providerSystem = DTOConverter.convertSystemToSystemResponseDTO(systemOptional.get());		
		}
		
		
		return DTOConverter.convertOrchestratorStoreToOrchestratorStoreResponseDTO(orchestratorStore, providerSystem, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreResponseDTO> getOrchestratorDTOListFromPage(final Page<OrchestratorStore> orchestratorStorePage) {
		final List<OrchestratorStoreResponseDTO> orchestratorStoreResponseDTOList = new ArrayList<>(orchestratorStorePage.getNumberOfElements());
		for (final OrchestratorStore orchestratorStore : orchestratorStorePage.getContent()) {
			if (orchestratorStore.isForeign()) {
				orchestratorStoreResponseDTOList.add(getForeignResponseDTO(orchestratorStore));
			} else {
				orchestratorStoreResponseDTOList.add(getLocalResponseDTO(orchestratorStore));
			}
		}
		
		return orchestratorStoreResponseDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreResponseDTO> getOrchestratorDTOListFromList(final List<OrchestratorStore> orchestratorStoreList) {
		final List<OrchestratorStoreResponseDTO> orchestratorStoreResponseDTOList = new ArrayList<>(orchestratorStoreList.size());
		for (final OrchestratorStore orchestratorStore : orchestratorStoreList) {
			if (orchestratorStore.isForeign()) {
				orchestratorStoreResponseDTOList.add(getForeignResponseDTO(orchestratorStore));
			} else {
				orchestratorStoreResponseDTOList.add(getLocalResponseDTO(orchestratorStore));
			}
		}
		
		return orchestratorStoreResponseDTOList;
	}

	//-------------------------------------------------------------------------------------------------
	@Nullable
	private OrchestratorStore saveOrchestratorStoreEntry(final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO) {
		try {					
			return createOrchestratorStoreEntity(orchestratorStoreRequestDTO);
		} catch (final Exception ex) {
			logger.debug(ORCHESTRATOR_STORE_REQUEST_BY_ID_DTO_VALIDATION_EXCEPTION_MESSAGE + ex.getMessage(), ex);
			return null;
		}
	}
}