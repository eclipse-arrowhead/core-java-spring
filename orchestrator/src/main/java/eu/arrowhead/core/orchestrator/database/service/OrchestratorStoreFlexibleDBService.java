/********************************************************************************
 * Copyright (c) 2021 AITIA
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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.database.repository.OrchestratorStoreFlexibleRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;

@Service
public class OrchestratorStoreFlexibleDBService {

	private static final Logger logger = LogManager.getLogger(OrchestratorStoreFlexibleDBService.class);
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";
	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE = "Service definition has invalid format. Service definition only contains letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";
	
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;
	
	@Autowired
	private OrchestratorStoreFlexibleRepository orchestratorStoreFlexibleRepository;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexibleListResponseDTO getOrchestratorStoreFlexibleEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreFlexibleEntriesResponse started...");
		
		final Page<OrchestratorStoreFlexible> entries = getOrchestratorStoreFlexibleEntries(page, size, direction, sortField);
		return DTOConverter.convertOrchestratorStoreFlexibleEntryListToOrchestratorStoreFlexibleListResponseDTO(entries, entries.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStoreFlexible> getOrchestratorStoreFlexibleEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreFlexibleEntries started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!OrchestratorStoreFlexible.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return orchestratorStoreFlexibleRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStoreFlexibleListResponseDTO createOrchestratorStoreFlexibleResponse(final List<OrchestratorStoreFlexibleRequestDTO> requestList) {
		logger.debug("createOrchestratorStoreFlexibleResponse started...");
		Assert.notNull(requestList, "OrchestratorStoreFlexible requestList is null");
		
		final List<OrchestratorStoreFlexible> candidates = new ArrayList<>(requestList.size());
		for (final OrchestratorStoreFlexibleRequestDTO dto : requestList) {
			Assert.notNull(dto, "OrchestratorStoreFlexibleRequestDTO is null");
			final OrchestratorStoreFlexible candidate = new OrchestratorStoreFlexible(dto.getConsumerSystem().getSystemName(),
																					  dto.getProviderSystem().getSystemName(),
																					  Utilities.map2Text(dto.getConsumerSystem().getMetadata()),
																					  Utilities.map2Text(dto.getProviderSystem().getMetadata()),
																					  Utilities.map2Text(dto.getServiceMetadata()),
																					  dto.getServiceInterfaceName(),
																					  dto.getServiceDefinitionName(),
																					  dto.getPriority());
			normalizeEntity(candidate);
			validateEntity(candidate);
			candidates.add(candidate);
		}
		
		final List<OrchestratorStoreFlexible> records = saveEntityList(candidates);
		return DTOConverter.convertOrchestratorStoreFlexibleEntryListToOrchestratorStoreFlexibleListResponseDTO(records, records.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public OrchestratorStoreFlexible createOrchestratorStoreFlexible(final String consumerSystemName, final String providerSystemName, final String consumerSystemMetadata, final String providerSystemMetadata,
			 														 final String serviceMetadata, final String serviceInterfaceName, final String serviceDefinitionName, final Integer priority) {
		logger.debug("createOrchestratorStoreFlexible started...");
		final OrchestratorStoreFlexible candidate = new OrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata,
																				  serviceInterfaceName, serviceDefinitionName, priority);
		normalizeEntity(candidate);
		validateEntity(candidate);
		return saveEntityList(List.of(candidate)).get(0);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<OrchestratorStoreFlexible> createOrchestratorStoreFlexible(final List<OrchestratorStoreFlexible> candidates) {
		logger.debug("createOrchestratorStoreFlexible started...");
		Assert.notNull(candidates, "OrchestratorStoreFlexible candidate list is null");
		
		for (final OrchestratorStoreFlexible candidate : candidates) {
			normalizeEntity(candidate);
			validateEntity(candidate);
		}
		
		return saveEntityList(candidates);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void deleteOrchestratorStoreFlexibleById(final long id) {
		logger.debug("deleteOrchestratorStoreFlexibleById started...");
		
		try {
			if (orchestratorStoreFlexibleRepository.existsById(id)) {
				orchestratorStoreFlexibleRepository.deleteById(id);
				orchestratorStoreFlexibleRepository.flush();
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void deleteAllOrchestratorStoreFlexible() {
		logger.debug("deleteAllOrchestratorStoreFlexible started...");
		
		try {
			orchestratorStoreFlexibleRepository.deleteAll();
			orchestratorStoreFlexibleRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<OrchestratorStoreFlexible> getMatchedRulesByServiceDefinitionAndConsumerName(final String serviceDefinition, final String consumerSystemName) {
		logger.debug("getMatchedRulesByServiceDefinitionAndConsumerName started...");
		
		if (Utilities.isEmpty(serviceDefinition)) {
			throw new InvalidParameterException("Service definition is empty");
		}
		
		if (Utilities.isEmpty(consumerSystemName)) {
			throw new InvalidParameterException("Consumer system name is empty");
		}
		
		final String _serviceDefinition = serviceDefinition.toLowerCase().trim();
		final String _consumerSystemName = consumerSystemName.toLowerCase().trim();
		
		try {
			return orchestratorStoreFlexibleRepository.findByServiceDefinitionNameAndConsumerSystemNameAndConsumerSystemMetadataIsNull(_serviceDefinition, _consumerSystemName);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<OrchestratorStoreFlexible> getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata(final String serviceDefinition) { 
		logger.debug("getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata started...");
		
		if (Utilities.isEmpty(serviceDefinition)) {
			throw new InvalidParameterException("Service definition is empty");
		}
		
		final String _serviceDefinition = serviceDefinition.toLowerCase().trim();
		
		try {
			return orchestratorStoreFlexibleRepository.findByServiceDefinitionNameAndConsumerSystemMetadataIsNotNull(_serviceDefinition);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void normalizeEntity(final OrchestratorStoreFlexible entity) {
		logger.debug("normalizeEntity started...");
		
		if (!Utilities.isEmpty(entity.getConsumerSystemName())) {
			entity.setConsumerSystemName(entity.getConsumerSystemName().toLowerCase().trim());			
		}
		if (!Utilities.isEmpty(entity.getProviderSystemName())) {
			entity.setProviderSystemName(entity.getProviderSystemName().toLowerCase().trim());			
		}
		if (!Utilities.isEmpty(entity.getServiceDefinitionName())) {
			entity.setServiceDefinitionName(entity.getServiceDefinitionName().toLowerCase().trim());
		}
		if (!Utilities.isEmpty(entity.getServiceInterfaceName())) {
			entity.setServiceInterfaceName(entity.getServiceInterfaceName().toUpperCase().trim());
		}
		
		if (Utilities.isEmpty(entity.getConsumerSystemMetadata())) {
			entity.setConsumerSystemMetadata(null);
		}
		if (Utilities.isEmpty(entity.getProviderSystemMetadata())) {
			entity.setProviderSystemMetadata(null);
		}
		if (Utilities.isEmpty(entity.getServiceMetadata())) {
			entity.setServiceMetadata(null);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateEntity(final OrchestratorStoreFlexible entity) {
		logger.debug("validateEntity started...");
		
		if (entity == null) {
			throw new InvalidParameterException("OrchestratorStoreFlexible entity is null");
		}
		
		if (Utilities.isEmpty(entity.getConsumerSystemName()) && Utilities.isEmpty(entity.getConsumerSystemMetadata())) {
			throw new InvalidParameterException("consumerSystemName and consumerSystemMetadata are both empty");
		}
		if (!Utilities.isEmpty(entity.getConsumerSystemName()) && !cnVerifier.isValid(entity.getConsumerSystemName())) {
			throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(entity.getProviderSystemName()) && Utilities.isEmpty(entity.getProviderSystemMetadata())) {
			throw new InvalidParameterException("providerSystemName and providerSystemMetadata are both empty");
		}
		if (!Utilities.isEmpty(entity.getProviderSystemName()) && !cnVerifier.isValid(entity.getProviderSystemName())) {
			throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(entity.getServiceDefinitionName())) {
			throw new InvalidParameterException("serviceDefinitionName is empty");
		} else if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(entity.getServiceDefinitionName())) {
			throw new InvalidParameterException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE);
		}
		
		if (!Utilities.isEmpty(entity.getServiceInterfaceName()) && !interfaceNameVerifier.isValid(entity.getServiceInterfaceName())) {
			throw new InvalidParameterException("Specified interface name is not valid: " + entity.getServiceInterfaceName());
		}
		
		if (entity.getPriority() <= 0) {
			throw new InvalidParameterException("Priority must be a positive number");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreFlexible> saveEntityList(final List<OrchestratorStoreFlexible> candidates) {
		logger.debug("saveEntityList started...");
		Assert.notNull(candidates, "OrchestratorStoreFlexible list is null");
		
		try {
			final List<OrchestratorStoreFlexible> saved = orchestratorStoreFlexibleRepository.saveAll(candidates);
			orchestratorStoreFlexibleRepository.flush();
			return saved;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}
