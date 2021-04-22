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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class OrchestratorStoreFlexibleDBService {

	private static final Logger logger = LogManager.getLogger(OrchestratorStoreFlexibleDBService.class);
	
	@Autowired
	private OrchestratorStoreFlexibleRepository orchestratorStoreFlexibleRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexibleListResponseDTO getOrchestratorStoreFlexibleEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getOrchestratorStoreFlexibleEntriesResponse started...");
		
		Page<OrchestratorStoreFlexible> entries = getOrchestratorStoreFlexibleEntries(page, size, direction, sortField);
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
	public OrchestratorStoreFlexibleListResponseDTO createOrchestratorStoreFlexibleResponse(final List<OrchestratorStoreFlexibleRequestDTO> requestList) { //TODO junit
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
			validateEntity(candidate);
			candidates.add(candidate);
		}
		
		final List<OrchestratorStoreFlexible> records = saveEntityList(candidates);
		return DTOConverter.convertOrchestratorStoreFlexibleEntryListToOrchestratorStoreFlexibleListResponseDTO(records, records.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexible createOrchestratorStoreFlexible(final String consumerSystemName, final String providerSystemName, final String consumerSystemMetadata, final String providerSystemMetadata,
			 														 final String serviceMetadata, final String serviceInterfaceName, final String serviceDefinitionName, final Integer priority) { //TODO junit
		logger.debug("createOrchestratorStoreFlexible started...");
		final OrchestratorStoreFlexible candidate = new OrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata,
																				  serviceInterfaceName, serviceDefinitionName, priority);
		validateEntity(candidate);
		return saveEntityList(List.of(candidate)).get(0);
	}

	//-------------------------------------------------------------------------------------------------
	public List<OrchestratorStoreFlexible> createOrchestratorStoreFlexible(final List<OrchestratorStoreFlexible> candidates) { //TODO junit
		logger.debug("createOrchestratorStoreFlexible started...");
		Assert.notNull(candidates, "OrchestratorStoreFlexible candidate list is null");
		
		for (final OrchestratorStoreFlexible candidate : candidates) {
			validateEntity(candidate);
		}
		
		return saveEntityList(candidates);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void deleteOrchestratorStoreFlexibleById(final long id) { //TODO junit
		logger.debug("deleteOrchestratorStoreFlexibleById started...");
		
		try {
			if (orchestratorStoreFlexibleRepository.existsById(id)) {
				orchestratorStoreFlexibleRepository.deleteById(id);
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void deleteAllOrchestratorStoreFlexible() { //TODO junit
		logger.debug("deleteAllOrchestratorStoreFlexible started...");
		
		try {
			orchestratorStoreFlexibleRepository.deleteAll();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateEntity(final OrchestratorStoreFlexible entity) {
		logger.debug("validateEntity started...");
		
		if (entity == null) {
			throw new InvalidParameterException("OrchestratorStoreFlexible entity is null");
		}
		if (Utilities.isEmpty(entity.getConsumerSystemName()) && Utilities.isEmpty(entity.getConsumerSystemMetadata())) {
			throw new InvalidParameterException("consumerSystemName and consumerSystemMetadata are both empty");
		}
		if (Utilities.isEmpty(entity.getProviderSystemName()) && Utilities.isEmpty(entity.getProviderSystemMetadata())) {
			throw new InvalidParameterException("providerSystemName and providerSystemMetadata are both null");
		}
		if (Utilities.isEmpty(entity.getServiceDefinitionName())) {
			throw new InvalidParameterException("serviceDefinitionName is empty");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	private List<OrchestratorStoreFlexible> saveEntityList(final List<OrchestratorStoreFlexible> candidates) {
		logger.debug("saveEntityList started...");
		Assert.notNull(candidates, "OrchestratorStoreFlexible list is null");
		
		try {
			return orchestratorStoreFlexibleRepository.saveAll(candidates);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}
