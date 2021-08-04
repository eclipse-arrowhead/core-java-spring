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
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerExecutorServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerExecutorServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@Service
public class ChoreographerExecutorDBService {

	//=================================================================================================
    // members
	
	@Autowired
    private ChoreographerExecutorRepository executorRepository;
	
	@Autowired
	private ChoreographerExecutorServiceDefinitionRepository executorServiceDefinitionRepository;
	
	@Autowired
    private SystemRepository systemRepository;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	private final Logger logger = LogManager.getLogger(ChoreographerExecutorDBService.class);
	
	//=================================================================================================
    // methods

	///-------------------------------------------------------------------------------------------------
	public ChoreographerExecutorResponseDTO createExecutorResponse(final ChoreographerExecutorRequestDTO request) { //TODO junit
		logger.debug("createExecutorResponse started...");
		Assert.notNull(request, "request is null");
		
		final ChoreographerExecutor executor = createExecutor(request.getSystem().getSystemName(), request.getSystem().getAddress(), request.getSystem().getPort(),
															  request.getSystem().getAuthenticationInfo(), request.getSystem().getMetadata(), request.getBaseUri(),
															  request.getServiceDefinitionName(), request.getMinVersion(), request.getMaxVersion());
		
		final List<ChoreographerExecutorServiceDefinition> serviceDefinitions = executorServiceDefinitionRepository.findAllByExecutor(executor);
		
		return DTOConverter.convertExecutorToExecutorResponseDTO(executor, serviceDefinitions);
	}
	
    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerExecutor createExecutor(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata,
    											final String baseUri, final String serviceDefinitionName, final int minVersion, final int maxVersion) { //TODO junit
		logger.debug("createExecutor started...");
		
		try {
			final System system = createSystem(systemName, address, port, authenticationInfo, metadata);
			return createExecutorWithServiceDefinition(system, baseUri, serviceDefinitionName, minVersion, maxVersion);
		} catch (final InvalidParameterException ex) {
			throw ex;
			
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
	private System createSystem(final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata) {
		logger.debug("registerSystemToServiceRegistry started...");

		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException("System name is null or empty");
		}
		final String validatedSystemName = systemName.trim().toLowerCase();
		if (!cnVerifier.isValid(validatedSystemName)) {
			throw new InvalidParameterException("System name has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING);
		}

		final String validatedAddress = networkAddressPreProcessor.normalize(address);
		networkAddressVerifier.verify(validatedAddress);
		
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		final Optional<System> optional = systemRepository.findBySystemNameAndAddressAndPort(validatedSystemName, validatedAddress, port);
		if (optional.isPresent()) {
			return optional.get();
		}
		
		return systemRepository.saveAndFlush(new System(validatedSystemName, validatedAddress, port, authenticationInfo, Utilities.map2Text(metadata)));
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerExecutor createExecutorWithServiceDefinition(final System system, final String baseUri, final String serviceDefinitionName, final int minVersion, final int maxVersion) {
		logger.debug("createExecutor started...");
		
		final String validatedBaseUri = Utilities.isEmpty(baseUri) ? "" : baseUri;
		
		ChoreographerExecutor executor;
		final Optional<ChoreographerExecutor> optionalExecutor = executorRepository.findByAddressAndPortAndBaseUri(system.getAddress(), system.getPort(), validatedBaseUri);
		if (optionalExecutor.isPresent()) {
			executor = optionalExecutor.get();
		} else {			
			executor = executorRepository.saveAndFlush(new ChoreographerExecutor(system.getSystemName(), system.getAddress(), system.getPort(), validatedBaseUri));
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
	}
}
