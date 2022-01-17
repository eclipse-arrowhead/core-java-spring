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

package eu.arrowhead.core.choreographer.service;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.internal.ChoreographerExecutorListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressDetector;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;

@Service
public class ChoreographerExecutorService {
	
	//=================================================================================================
    // members
	
	@Autowired
	private ChoreographerExecutorDBService executorDBService;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private NetworkAddressDetector networkAddressDetector;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private ChoreographerDriver driver;
	
	private boolean useStrictServiceDefinitionVerifier = false;
	
	private final Object lock = new Object();
	
	private final Logger logger = LogManager.getLogger(ChoreographerExecutorService.class);
	
	//=================================================================================================
    // methods
	
	//-------------------------------------------------------------------------------------------------	
	public void configure(final boolean useStrictServiceDefinitionVerifier) {
		this.useStrictServiceDefinitionVerifier = useStrictServiceDefinitionVerifier;
	}
	
	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorResponseDTO addExecutorSystem(final ChoreographerExecutorRequestDTO request, final String origin) {
		logger.debug("addExecutorSystem started...");
		Assert.notNull(request, "ChoreographerExecutorRequestDTO is null");
		Assert.isTrue(!Utilities.isEmpty(origin), "origin is empty");
		
		checkAndNormalizeExecutorRequestDTO(request, origin, null);
		final SystemResponseDTO system = driver.registerSystem(request.getSystem());
		return executorDBService.createExecutorResponse(system.getSystemName(), system.getAddress(), system.getPort(), request.getBaseUri(), request.getServiceDefinitionName(),
														request.getMinVersion(), request.getMaxVersion());
	}
	
	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorResponseDTO registerExecutorSystem(final ChoreographerExecutorRequestDTO request, final String origin, final HttpServletRequest servletRequest) {
		logger.debug("registerExecutorSystem started...");
		Assert.notNull(request, "ChoreographerExecutorRequestDTO is null");
		Assert.isTrue(!Utilities.isEmpty(origin), "origin is empty");
		Assert.notNull(servletRequest, "servletRequest is null");
		
		checkAndNormalizeExecutorRequestDTO(request, origin, servletRequest);
		
		SystemResponseDTO system = null;
		boolean recentSRRegistration = false;
		
		try {
			system = driver.queryServiceRegistryBySystem(request.getSystem().getSystemName(), request.getSystem().getAddress(), request.getSystem().getPort());			
		} catch (final BadPayloadException | InvalidParameterException ex) {
			// executor has not yet been registered as system
			system = driver.registerSystem(request.getSystem());
			recentSRRegistration = true;
		}
		
		try {
			return executorDBService.createExecutorResponse(system.getSystemName(), system.getAddress(), system.getPort(), request.getBaseUri(), request.getServiceDefinitionName(),
													        request.getMinVersion(), request.getMaxVersion());
			
		} catch (final ArrowheadException ex) {
			if (recentSRRegistration) {
	        	driver.unregisterSystem(system.getSystemName(), system.getAddress(), system.getPort());
			}
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorListResponseDTO getExecutors(final Integer page, final Integer size, final String direction, final String sortField, final String origin) {
		logger.debug("getExecutors started...");
		Assert.isTrue(!Utilities.isEmpty(origin), "origin is empty");
		
		final ValidatedPageParams validPageParams = CoreUtilities.validatePageParameters(page, size, direction, origin);
        return executorDBService.getExecutorsResponse(validPageParams.getValidatedPage(), validPageParams.getValidatedSize(), validPageParams.getValidatedDirection(), sortField);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorResponseDTO getExecutorById(final long id, final String origin) {
		logger.debug("getExecutorById started...");
		Assert.isTrue(!Utilities.isEmpty(origin), "origin is empty");
		
		if (id < 1) {
            throw new BadPayloadException("ID must be greater than 0.", HttpStatus.SC_BAD_REQUEST, origin);
        }
		
		final Optional<ChoreographerExecutorResponseDTO> optional = executorDBService.getExecutorOptionalByIdResponse(id);
		if (optional.isEmpty()) {
			throw new BadPayloadException(id + " ID not exists", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return optional.get();
	}
	
	//-------------------------------------------------------------------------------------------------	
	public void removeExecutorSystem(final long id, final String origin) {
		logger.debug("removeExecutorSystem started...");
		Assert.isTrue(!Utilities.isEmpty(origin), "origin is empty");

		if (id < 1) {
            throw new BadPayloadException("Id must be greater than 0. ", HttpStatus.SC_BAD_REQUEST, origin);
        }

        final Optional<ChoreographerExecutor> optional = executorDBService.getExecutorOptionalById(id);
        if (optional.isPresent()) {
        	deleteExecutorSafely(optional.get(), origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public void unregisterExecutorSystem(final String name, final String origin) {
		logger.debug("unregisterExecutorSystem started...");
		Assert.isTrue(!Utilities.isEmpty(origin), "origin is empty");
		
		if (Utilities.isEmpty(name)) {
			throw new BadPayloadException("Executor name is empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
        final Optional<ChoreographerExecutor> optional = executorDBService.getExecutorOptionalByName(name);
        if (optional.isPresent()) {
        	deleteExecutorSafely(optional.get(), origin);
		}
	}
	
	//=================================================================================================
    // assistant methods

	//-------------------------------------------------------------------------------------------------
	private void checkAndNormalizeExecutorRequestDTO(final ChoreographerExecutorRequestDTO dto, final String origin, final HttpServletRequest servletRequest) {
		logger.debug("checkAndNormalizeExecutorRequestDTO started...");

		if (dto == null) {
			throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getSystem() == null) {
			throw new BadPayloadException("System is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(dto.getSystem().getSystemName())) {
			throw new BadPayloadException("System name is empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (!Utilities.isEmpty(dto.getSystem().getAddress())) {
			try {
				final String normalizedAddress = networkAddressPreProcessor.normalize(dto.getSystem().getAddress());
				networkAddressVerifier.verify(normalizedAddress);
				dto.getSystem().setAddress(normalizedAddress);
			} catch (final InvalidParameterException ex) {
				throw new BadPayloadException(ex.getMessage(), HttpStatus.SC_BAD_REQUEST, origin);
			}			
			
		} else {
			if (servletRequest == null) {
				throw new BadPayloadException("System address is empty.", HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				final String detectedAddress = detectNetworkAddress(servletRequest, "System address is empty.", origin);
				dto.getSystem().setAddress(detectedAddress);
			}
		}

		if (dto.getSystem().getPort() == null) {
			throw new BadPayloadException("System port is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		// check others
		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(dto.getServiceDefinitionName())) {
			throw new BadPayloadException("Service definition has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getMinVersion() == null) {
			throw new BadPayloadException("minVersion is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (dto.getMaxVersion() == null) {
			throw new BadPayloadException("maxVersion is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (dto.getMinVersion() > dto.getMaxVersion()) {
			throw new InvalidParameterException("minVersion cannot be greater than maxVersion.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String detectNetworkAddress(final HttpServletRequest servletRequest, final String errorMsgPrefix, final String origin) {
		logger.debug("detectNetworkAddress started...");
		
		final AddressDetectionResult result = networkAddressDetector.detect(servletRequest);
		if (result.isSkipped() || !result.isDetectionSuccess()) {
			throw new BadPayloadException(errorMsgPrefix + " " + result.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);				
		}
		
		return result.getDetectedAddress();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void deleteExecutorSafely(final ChoreographerExecutor executor, final String origin) {
		logger.debug("deleteExectuorSafely started...");

		synchronized (lock) {
			if (executorDBService.isExecutorActiveById(executor.getId())) {
				throw new BadPayloadException("Executor is working!", HttpStatus.SC_BAD_REQUEST, origin);
				
			} else {
				if (!executorDBService.lockExecutorById(executor.getId())) { //Locking executor from Executor selection
					//Should not happen
					throw new ArrowheadException("Executor lock failure.", HttpStatus.SC_INTERNAL_SERVER_ERROR);
				} else {
					driver.unregisterSystem(executor.getName(), executor.getAddress(), executor.getPort());
					executorDBService.deleteExecutorById(executor.getId());        		
				}
			}			
		}
	}
}
