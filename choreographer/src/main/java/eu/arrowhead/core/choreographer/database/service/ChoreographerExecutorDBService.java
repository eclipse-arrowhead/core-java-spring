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

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerExecutorServiceDefinitionRepository;
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
	private CommonNamePartVerifier cnVerifier;
	
	private final Logger logger = LogManager.getLogger(ChoreographerExecutorDBService.class);
	
	//=================================================================================================
    // methods

	///-------------------------------------------------------------------------------------------------
	public ChoreographerExecutorResponseDTO createExecutorResponse(final String systemName, final String address, final int port, final String baseUri, final String serviceDefinitionName,
																   final int minVersion, final int maxVersion) { //TODO junit
		logger.debug("createExecutorResponse started...");
		
		final ChoreographerExecutor executor = createExecutor(systemName, address, port, baseUri, serviceDefinitionName, minVersion, maxVersion);		
		final List<ChoreographerExecutorServiceDefinition> serviceDefinitions = executorServiceDefinitionRepository.findAllByExecutor(executor);
		
		return DTOConverter.convertExecutorToExecutorResponseDTO(executor, serviceDefinitions);
	}
	
    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerExecutor createExecutor(final String systemName, final String address, final int port, final String baseUri, final String serviceDefinitionName,
    											final int minVersion, final int maxVersion) { //TODO junit
		logger.debug("createExecutor started...");
		Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is empty");
		Assert.isTrue(!Utilities.isEmpty(address), "address is empty");
		// System name and address is coming from SR registration result, so should be verified
		Assert.isTrue(!Utilities.isEmpty(serviceDefinitionName), "serviceDefinitionName is empty");
		
		try {
			final String validatedBaseUri = Utilities.isEmpty(baseUri) ? "" : baseUri;
			
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
}
