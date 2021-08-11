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

package eu.arrowhead.core.choreographer.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

@Service
public class ChoreographerPlanExecutionChecker {
	

	//=================================================================================================
	// members
	
	private static final String EXECUTOR_NOT_FOUND_FOR_MSG_PREFIX = "Executor not found for step: ";
	private static final String PROVIDERS_NOT_FOUND_FOR_MSG_PREFIX = "Providers not found for step: ";
	private static final String SR_CONNECTION_PROBLEM_MSG_PREFIX = "Something happened when connecting to the Service Registry: ";
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Autowired
	private ChoreographerPlanDBService planDBService;
	
	@Autowired
	private ChoreographerExecutorDBService executorDBService;
	
	@Autowired
	private ChoreographerDriver driver;
	
	@Autowired
	private ExecutorSelector executorSelector;
	
	private final Logger logger = LogManager.getLogger(ChoreographerPlanExecutionChecker.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO checkPlanForExecution(final ChoreographerRunPlanRequestDTO request) {
		logger.debug("checkPlanForExecution started...");
		
		final List<String> errors = basicChecks(request);
		final Long planId = request == null ? null : request.getPlanId();
		
		return errors.isEmpty() ? checkPlanForExecution(request.getPlanId(), false) : new ChoreographerRunPlanResponseDTO(planId, errors);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO checkPlanForExecution(final long planId) {
		logger.debug("checkPlanForExecution started...");
		return checkPlanForExecution(planId, true);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO checkPlanForExecution(final long planId, final boolean dependencyCheck) {
		logger.debug("checkPlanForExecution started...");
		
		ChoreographerPlan plan = null;
		List<String> errors = new ArrayList<>();
		
		// existence check
		try {
			plan = planDBService.getPlanById(planId);
		} catch (final InvalidParameterException ex) {
			return new ChoreographerRunPlanResponseDTO(planId, List.of(ex.getMessage()));
		}
		
		final List<ChoreographerStep> steps = planDBService.collectStepsPlan(plan);
			
		// executor check
		checkAvailableExecutorsInDB(steps, errors);
			
		// provider check for steps
		checkAvailableProviders(steps, errors);
			
		if (dependencyCheck) {
			// check executors dependency too
			checkExecutorDependencies(steps, errors);
		}
		
		return errors.isEmpty() ? null : new ChoreographerRunPlanResponseDTO(planId, errors);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<String> basicChecks(final ChoreographerRunPlanRequestDTO request) {
		logger.debug("basicChecks started...");

		final List<String> errors = new ArrayList<>();
		
		if (request == null) {
			errors.add("Request is null.");
		}
		
		if (request.getPlanId() == null || request.getPlanId() <= 0) {
			errors.add("Plan id is not valid.");
		}
		
		if (!Utilities.isEmpty(request.getNotifyAddress())) { // means we have a notify URI
			if (!CommonConstants.HTTP.equalsIgnoreCase(request.getNotifyProtocol()) || !CommonConstants.HTTPS.equalsIgnoreCase(request.getNotifyProtocol())) {
				errors.add("Invalid notify protocol.");
			}
			
			try {
				networkAddressVerifier.verify(request.getNotifyAddress());
			} catch (final InvalidParameterException ex) {
				errors.add("Invalid notify address. " + ex.getMessage());
			}
			
			final int validatedPort = request.getNotifyPort();
			if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
				errors.add("Notify port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
			}
			
			if (request.getNotifyPath() == null) {
				request.setNotifyPath("");
			}
		}
		
		return errors;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkAvailableExecutorsInDB(final List<ChoreographerStep> steps, final List<String> errors) {
		logger.debug("checkAvailableExecutorsInDB started...");
		
		for (final ChoreographerStep step : steps) {
			final int minVersion = step.getMinVersion() == null ? 1 :  step.getMinVersion(); //TODO 1->CommonConstants
			final int maxVersion = step.getMaxVersion() == null ? Integer.MAX_VALUE : step.getMaxVersion();
			
			final List<ChoreographerExecutor> executors = executorDBService.getExecutorsByServiceDefinitionAndVersion(step.getServiceDefinition(), minVersion, maxVersion);
			if (executors.isEmpty()) {
				errors.add(EXECUTOR_NOT_FOUND_FOR_MSG_PREFIX + createFullyQualifiedStepName(step));
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private String createFullyQualifiedStepName(final ChoreographerStep step) {
		logger.debug("createFullyQualifiedStepName started...");
		
		return step.getAction().getPlan().getName() + CommonConstants.DOT + step.getAction().getName() + CommonConstants.DOT + step.getName();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkAvailableProviders(final List<ChoreographerStep> steps, final List<String> errors) {
		logger.debug("checkAvailableProviders started...");
		
		final List<ServiceQueryFormDTO> forms = steps.stream().map(s -> Utilities.fromJson(s.getSrTemplate(), ServiceQueryFormDTO.class)).collect(Collectors.toList());
		
		try {
			final ServiceQueryResultListDTO response = driver.multiQueryServiceRegistry(new ServiceQueryFormListDTO(forms));
			for (int i = 0; i < response.getResults().size(); ++i) {
				final ServiceQueryResultDTO result = response.getResults().get(i);
				if (result.getServiceQueryData().isEmpty()) {
					errors.add(PROVIDERS_NOT_FOUND_FOR_MSG_PREFIX + createFullyQualifiedStepName(steps.get(i)));
				}
			}
		} catch (final Exception ex) {
			errors.add(SR_CONNECTION_PROBLEM_MSG_PREFIX + ex.getMessage());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkExecutorDependencies(final List<ChoreographerStep> steps, final List<String> errors) {
		logger.debug("checkExecutorDependencies started...");
		
		for (final ChoreographerStep step : steps) {
			final ChoreographerExecutor selectedExecutor = executorSelector.select(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion(), null);
			if (selectedExecutor == null) {
				errors.add(EXECUTOR_NOT_FOUND_FOR_MSG_PREFIX + createFullyQualifiedStepName(step));
			}
		}
	}
}