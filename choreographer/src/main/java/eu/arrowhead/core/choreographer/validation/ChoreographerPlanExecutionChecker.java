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
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.executor.ExecutorData;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

@Service
public class ChoreographerPlanExecutionChecker {
	

	//=================================================================================================
	// members
	
	private static final String EXECUTOR_NOT_FOUND_FOR_MSG_PREFIX = "Executor not found for step: ";
	private static final String PROVIDER_NOT_FOUND_FOR_MSG_PREFIX = "Provider not found for step: ";
	private static final String CONNECTION_PROBLEM_MSG_PREFIX = "Something happened when connecting to other core services: ";
	
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
	
	@Value(CoreCommonConstants.$CHOREOGRAPHER_MAX_PLAN_ITERATION_WD)
	private Long maxIteration;
	
	private final Logger logger = LogManager.getLogger(ChoreographerPlanExecutionChecker.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO checkPlanForExecution(final ChoreographerRunPlanRequestDTO request) { 
		logger.debug("checkPlanForExecution started...");
		
		final List<String> errors = basicChecks(request);
		final Long planId = request == null ? null : request.getPlanId();
		final Long quantity = request == null ? null : request.getQuantity();
		
		return errors.isEmpty() ? checkPlanForExecution(request.isAllowInterCloud(), request.getPlanId(), quantity, false) : new ChoreographerRunPlanResponseDTO(planId, quantity, errors, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO checkPlanForExecution(final boolean allowInterCloud, final long planId, final long quantity) {
		logger.debug("checkPlanForExecution started...");
		return checkPlanForExecution(allowInterCloud, planId, quantity, true);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO checkPlanForExecution(final boolean allowInterCloud, final long planId, final long quantity, final boolean dependencyCheck) { 
		logger.debug("checkPlanForExecution started...");
		
		ChoreographerPlan plan = null;
		final List<String> errors = new ArrayList<>();
		
		// existence check
		try {
			plan = planDBService.getPlanById(planId);
		} catch (final InvalidParameterException ex) {
			return new ChoreographerRunPlanResponseDTO(planId, quantity, List.of(ex.getMessage()), false);
		}
		
		// iteration check
		if (quantity <= 0) {
			errors.add("Quantity must be greater than 0.");
		}
		if (quantity > maxIteration) {
			errors.add("Quantity could not be greater than " + maxIteration + ".");
		}
		
		final List<ChoreographerStep> steps = planDBService.collectStepsFromPlan(plan.getId());
			
		// executor check
		final List<ChoreographerStep> stepsWithExecutors = checkAvailableExecutorsInDB(steps, errors);
			
		// provider check for steps
		boolean needInterCloudForSteps = false;
		needInterCloudForSteps = checkAvailableProviders(steps, allowInterCloud, errors);
		
		boolean needInterCloudForExecutors = false;
		if (dependencyCheck) {
			// check executors dependency too (only for steps with executors)
			needInterCloudForExecutors = checkExecutorDependencies(stepsWithExecutors, allowInterCloud, errors);
		}
		
		return new ChoreographerRunPlanResponseDTO(planId, quantity, errors, needInterCloudForSteps || needInterCloudForExecutors);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<String> basicChecks(final ChoreographerRunPlanRequestDTO request) {
		logger.debug("basicChecks started...");

		final List<String> errors = new ArrayList<>();
		
		if (request == null) {
			errors.add("Request is null.");
			return errors; // can't check anything else
		}
		
		if (request.getPlanId() == null || request.getPlanId() <= 0) {
			errors.add("Plan id is not valid.");
		}
		
		if (!Utilities.isEmpty(request.getNotifyAddress())) { // means we have a notify URI
			if (!CommonConstants.HTTP.equalsIgnoreCase(request.getNotifyProtocol()) && !CommonConstants.HTTPS.equalsIgnoreCase(request.getNotifyProtocol())) {
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
	private List<ChoreographerStep> checkAvailableExecutorsInDB(final List<ChoreographerStep> steps, final List<String> errors) {
		logger.debug("checkAvailableExecutorsInDB started...");
		
		final List<ChoreographerStep> result = new ArrayList<>(steps.size());
		
		for (final ChoreographerStep step : steps) {
			final int minVersion = step.getMinVersion() == null ? Defaults.DEFAULT_VERSION :  step.getMinVersion(); 
			final int maxVersion = step.getMaxVersion() == null ? Integer.MAX_VALUE : step.getMaxVersion();
			
			final List<ChoreographerExecutor> executors = executorDBService.getExecutorsByServiceDefinitionAndVersion(step.getServiceDefinition(), minVersion, maxVersion);
			if (executors.isEmpty()) {
				errors.add(EXECUTOR_NOT_FOUND_FOR_MSG_PREFIX + createFullyQualifiedStepName(step));
			} else {
				result.add(step);
			}
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private String createFullyQualifiedStepName(final ChoreographerStep step) {
		logger.debug("createFullyQualifiedStepName started...");
		
		return step.getAction().getPlan().getName() + CommonConstants.DOT + step.getAction().getName() + CommonConstants.DOT + step.getName();
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean checkAvailableProviders(final List<ChoreographerStep> steps, final boolean allowInterCloud, final List<String> errors) {
		logger.debug("checkAvailableProviders started...");
		
		final List<ChoreographerServiceQueryFormDTO> forms = steps.stream().map(s -> Utilities.fromJson(s.getSrTemplate(), ChoreographerServiceQueryFormDTO.class)).collect(Collectors.toList());
		boolean needInterCloud = false;
		
		try {
			final Map<Integer,List<String>> resultMap = driver.searchForServices(new ServiceQueryFormListDTO(forms), allowInterCloud);
			
			for (final Map.Entry<Integer,List<String>> entry : resultMap.entrySet()) {
				final int idx = entry.getKey();
				final List<String> clouds = entry.getValue();
				if (clouds.isEmpty()) {
					errors.add(PROVIDER_NOT_FOUND_FOR_MSG_PREFIX + createFullyQualifiedStepName(steps.get(idx)));
				} else if (!ChoreographerDriver.OWN_CLOUD_MARKER.equals(clouds.get(0))) {
					needInterCloud = true;
				}
			}
		} catch (final Exception ex) {
			errors.add(CONNECTION_PROBLEM_MSG_PREFIX + ex.getMessage());
		}
		
		return needInterCloud;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean checkExecutorDependencies(final List<ChoreographerStep> steps, final boolean allowInterCloud, final List<String> errors) {
		logger.debug("checkExecutorDependencies started...");
		
		boolean needIntercloud = false;
		
		for (final ChoreographerStep step : steps) {
			final ExecutorData selectedExecutor = executorSelector.select(step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion(), null, allowInterCloud);
			if (selectedExecutor == null) {
				errors.add(EXECUTOR_NOT_FOUND_FOR_MSG_PREFIX + createFullyQualifiedStepName(step));
			} else if (selectedExecutor.getUseOtherClouds()) {
				needIntercloud = true;
			}
		}
		
		return needIntercloud;
	}
}