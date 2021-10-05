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

package eu.arrowhead.core.choreographer.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerExecutorServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionStepRepository;
import eu.arrowhead.common.dto.internal.ChoreographerExecutorListResponseDTO;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@Service
public class ChoreographerExecutorDBService {

	//=================================================================================================
    // members
	
	@Autowired
    private ChoreographerExecutorRepository executorRepository;
	
	@Autowired
	private ChoreographerExecutorServiceDefinitionRepository executorServiceDefinitionRepository;
	
	@Autowired
	private ChoreographerSessionStepRepository sessionStepRepository;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	private static final Set<ChoreographerSessionStepStatus> ACTIVE_SESSION_STEP_STATES = Set.of(ChoreographerSessionStepStatus.WAITING, ChoreographerSessionStepStatus.RUNNING);
	
	private final Logger logger = LogManager.getLogger(ChoreographerExecutorDBService.class);
	
	//=================================================================================================
    // methods

	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutorResponseDTO createExecutorResponse(final String systemName, final String address, final int port, final String baseUri, final String serviceDefinitionName,
																   final int minVersion, final int maxVersion) {
		logger.debug("createExecutorResponse started...");
		
		final ChoreographerExecutor executor = createExecutorWithoutSystemNameAndAddressValidation(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);		
		final List<ChoreographerExecutorServiceDefinition> serviceDefinitions = executorServiceDefinitionRepository.findAllByExecutor(executor);
		
		return DTOConverter.convertExecutorToExecutorResponseDTO(executor, serviceDefinitions);
	}
	
    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerExecutor createExecutorWithoutSystemNameAndAddressValidation(final String systemName, final String address, final int port, final String baseUri, final String serviceDefinitionName,
    																				 final int minVersion, final int maxVersion) {
		logger.debug("createExecutor started...");
		Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is empty");
		Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
		// System name and address is coming from SR registration result, so should be verified (verification is depending on SR's application properties)
		Assert.isTrue(!Utilities.isEmpty(serviceDefinitionName), "serviceDefinitionName is empty");
		
		try {			
			if (executorRepository.findByName(systemName).isPresent()) {
				throw new InvalidParameterException("Executor with name '" + systemName +  "' already exists!");
			}
			
			final String validatedBaseUri = Utilities.isEmpty(baseUri) ? "" : baseUri.trim();
			
			ChoreographerExecutor executor;
			final Optional<ChoreographerExecutor> optionalExecutor = executorRepository.findByAddressAndPortAndBaseUri(address, port, validatedBaseUri);
			if (optionalExecutor.isPresent()) {
				executor = optionalExecutor.get();
			} else {			
				executor = executorRepository.saveAndFlush(new ChoreographerExecutor(systemName, address, port, validatedBaseUri));
			}
			
			final String validatedServiceDefinitionName = serviceDefinitionName.trim().toLowerCase();
			if (!cnVerifier.isValid(validatedServiceDefinitionName)) {
				throw new InvalidParameterException("Service definition has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING);
			}		
			if (minVersion > maxVersion) {
				throw new InvalidParameterException("minVersion cannot be higher than maxVersion");
			}
			
			final Optional<ChoreographerExecutorServiceDefinition> optionalExecutorServiceDef = executorServiceDefinitionRepository.findByExecutorAndServiceDefinition(executor, validatedServiceDefinitionName);
			if (optionalExecutorServiceDef.isEmpty()) {
				executorServiceDefinitionRepository.saveAndFlush(new ChoreographerExecutorServiceDefinition(executor, validatedServiceDefinitionName, minVersion, maxVersion));
				
			} else {
				final ChoreographerExecutorServiceDefinition executorServiceDefinition = optionalExecutorServiceDef.get();
				if (minVersion != executorServiceDefinition.getMinVersion() || maxVersion != executorServiceDefinition.getMaxVersion()) {
					executorServiceDefinition.setMinVersion(minVersion);
					executorServiceDefinition.setMaxVersion(maxVersion);
					executorServiceDefinitionRepository.saveAndFlush(executorServiceDefinition);
				}
			}
			
			return executor;
			
		} catch (final InvalidParameterException ex) {
			throw ex;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutorListResponseDTO getExecutorsResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getExecutorsResponse started...");
		
		final Page<ChoreographerExecutor> executors = getExecutors(page, size, direction, sortField);
		final List<ChoreographerExecutorResponseDTO> responseData = new ArrayList<>(executors.getSize());
		for (final ChoreographerExecutor entry : executors) {
			final ChoreographerExecutorResponseDTO dto = DTOConverter.convertExecutorToExecutorResponseDTO(entry, executorServiceDefinitionRepository.findAllByExecutor(entry));
			responseData.add(dto);
		}
		
		return new ChoreographerExecutorListResponseDTO(responseData, executors.getTotalElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public Page<ChoreographerExecutor> getExecutors(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getExecutors started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!ChoreographerExecutor.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return executorRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ChoreographerExecutorResponseDTO> getExecutorOptionalByIdResponse(final long id) {
		logger.debug("getExecutorOptionalByIdResponse started...");
		
		final Optional<ChoreographerExecutor> optional = getExecutorOptionalById(id);
		if (optional.isEmpty()) {
			return Optional.empty();
		}
		
		final ChoreographerExecutorResponseDTO dto = DTOConverter.convertExecutorToExecutorResponseDTO(optional.get(), executorServiceDefinitionRepository.findAllByExecutor(optional.get()));
		return Optional.of(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ChoreographerExecutor> getExecutorOptionalById(final long id) {
		logger.debug("getExecutorById started...");
		
		try {
			return executorRepository.findById(id);	
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ChoreographerExecutor> getExecutorOptionalByAddressAndPortAndBaseUri(final String address, final int port, final String baseUri) {
		logger.debug("getExecutorById started...");
		Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
		
		try {
			return executorRepository.findByAddressAndPortAndBaseUri(address.toLowerCase().trim(), port, Utilities.isEmpty(baseUri) ? "" : baseUri.trim());	
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ChoreographerExecutor> getExecutorOptionalByName(final String name) {
		logger.debug("getExecutorOptionalByName started...");
		Assert.isTrue(!Utilities.isEmpty(name), "name is empty");
		
		try {
			return executorRepository.findByName(name.toLowerCase().trim());	
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerExecutor> getExecutorsByServiceDefinitionAndVersion(final String serviceDefinition, final int minVersion, final int maxVersion) {
		logger.debug("getExecutorsByServiceDefinitionAndVersion started...");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "serviceDefinition is empty");
		
		try {
			final List<ChoreographerExecutor> results = new ArrayList<>();
			for (final ChoreographerExecutorServiceDefinition entry : executorServiceDefinitionRepository.findAllByServiceDefinition(serviceDefinition)) {
				if (entry.getMinVersion() >= minVersion && entry.getMaxVersion() <= maxVersion) {
					results.add(entry.getExecutor());
				}
			}
			return results;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public void deleteExecutorById(final long id) {
		logger.debug("deleteExecutorById started...");
		
		try {
			final Optional<ChoreographerExecutor> optional = executorRepository.findById(id);
			if (optional.isPresent()) {
				if (sessionStepRepository.existsByExecutorAndStatusIn(optional.get(), ACTIVE_SESSION_STEP_STATES)) {
					throw new InvalidParameterException("Executor is working!");
				} else {
					executorRepository.deleteById(id);
					executorRepository.flush();					
				}
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public void deleteExecutorByAddressAndPortAndBaseUri(final String address, final int port, final String baseUri) {
		logger.debug("deleteExecutorByAddressAndPortAndBaseUri started...");
		Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
		
		try {
			final Optional<ChoreographerExecutor> opt = executorRepository.findByAddressAndPortAndBaseUri(address.toLowerCase().trim(), port, Utilities.isEmpty(baseUri) ? "" : baseUri.trim());
			if (opt.isPresent()) {
				deleteExecutorById(opt.get().getId());
			}
			
		} catch (final InvalidParameterException ex) {
			throw ex;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public boolean lockExecutorById(final long id) {
		logger.debug("lockExecutorById started...");
		
		try {
			final Optional<ChoreographerExecutor> optional = executorRepository.findById(id);
			if (optional.isEmpty()) {
				return false;
			}
			
			final ChoreographerExecutor executor = optional.get();
			executor.setLocked(true);
			executorRepository.saveAndFlush(executor);
			return true;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isExecutorActiveById(final long id) {		
		logger.debug("isExecutorActiveById started...");		
		
		try {
			final Optional<ChoreographerExecutor> optional = executorRepository.findById(id);
			if (optional.isEmpty()) {
				return false;
			}			
			return sessionStepRepository.existsByExecutorAndStatusIn(optional.get(), ACTIVE_SESSION_STEP_STATES);
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
}
