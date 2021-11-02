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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.choreographer.graph.StepGraph;
import eu.arrowhead.core.choreographer.graph.StepGraphUtils;

@Service
public class ChoreographerPlanValidator {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ActionCircleDetector actionCircleDetector;
	
	@Autowired
	private StepGraphUtils stepGraphUtils;
	
	@Autowired
	private ActionUtils actionUtils;
	
	private final Logger logger = LogManager.getLogger(ChoreographerPlanValidator.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlanRequestDTO validatePlan(final ChoreographerPlanRequestDTO request, final String origin, final boolean normalizeNames) { 
		logger.debug("validatePlan started...");
		
		return validatePlanImpl(request, origin, normalizeNames);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlanRequestDTO validatePlan(final ChoreographerPlanRequestDTO request, final String origin) {
		return validatePlan(request, origin, true);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlanRequestDTO validateAndNormalizePlan(final ChoreographerPlanRequestDTO request, final boolean normalizeNames) {
		logger.debug("validateAndNormalizePlan started...");

		final ChoreographerPlanRequestDTO validatedPlan = validatePlan(request, null, normalizeNames);
		
		if (actionCircleDetector.hasCircle(validatedPlan)) {
			throw new InvalidParameterException("An action references a previous action as its next action (or referencing itself).");
		}
		
		validatedPlan.setActions(validateAndNormalizeActions(validatedPlan.getActions()));

		return validatedPlan;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlanRequestDTO validateAndNormalizePlan(final ChoreographerPlanRequestDTO request) {
		return validateAndNormalizePlan(request, true);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerPlanRequestDTO validatePlanImpl(final ChoreographerPlanRequestDTO request, final String origin, final boolean normalizeNames) {
		logger.debug("validatePlanImpl started...");
		
		if (request == null) {
			throw new InvalidParameterException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final ChoreographerPlanRequestDTO result = new ChoreographerPlanRequestDTO();
		result.setName(handleName(request.getName(), "Plan name is null or blank.", origin, normalizeNames));
		result.setFirstActionName(handleName(request.getFirstActionName(), "First action is not specified.", origin, normalizeNames));
		result.setActions(validateActions(request.getActions(), result.getFirstActionName(), origin, normalizeNames));		
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String handleName(final String name, final String msg, final String origin, final boolean normalize) {
		logger.debug("handleName started...");
		
		if (Utilities.isEmpty(name)) {
			throw new InvalidParameterException(msg, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return normalize ? name.trim().toLowerCase() : name;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ChoreographerActionRequestDTO> validateActions(final List<ChoreographerActionRequestDTO> actions, final String firstActionName, final String origin, final boolean normalizeNames) {
		logger.debug("validateActions started...");
		
		if (actions == null || actions.isEmpty()) {
			throw new InvalidParameterException("Action list is null or empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final List<ChoreographerActionRequestDTO> result = new ArrayList<>(actions.size());
		
		for (final ChoreographerActionRequestDTO action : actions) {
			result.add(validateAction(action, origin, normalizeNames));
		}
		
		if (!firstActionNameFound(result, firstActionName)) {
			throw new InvalidParameterException("Specified first action is not found in the actions list.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		handleActionNameDuplications(result, origin);
		handleNextActionNames(result, firstActionName, origin);
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private ChoreographerActionRequestDTO validateAction(final ChoreographerActionRequestDTO action, final String origin, final boolean normalizeNames) {
		logger.debug("validateAction started...");
		
		if (action == null) {
			throw new InvalidParameterException("Action is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final ChoreographerActionRequestDTO result = new ChoreographerActionRequestDTO();
		result.setName(handleName(action.getName(), "Action name is null or blank.", origin, normalizeNames));
		
		if (action.getNextActionName() != null && normalizeNames) {
			result.setNextActionName(action.getNextActionName().trim().toLowerCase());
		}
		
		if (result.getName().equals(result.getNextActionName())) {
			throw new InvalidParameterException("Action references itself as next action.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (action.getFirstStepNames() == null || action.getFirstStepNames().isEmpty()) {
			throw new InvalidParameterException("First steps list is null or empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		result.setFirstStepNames(handleFirstStepNames(action.getFirstStepNames(), origin, normalizeNames));
		result.setSteps(validateSteps(action.getSteps(), result.getFirstStepNames(), origin, normalizeNames));
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean firstActionNameFound(final List<ChoreographerActionRequestDTO> actions, final String firstActionName) {
		logger.debug("firstActionNameFound started...");
		
		final Set<String> actionNames = actions.stream()
				   							   .map(a -> a.getName())
				   							   .collect(Collectors.toSet());
		
		return actionNames.contains(firstActionName);
	}

	//-------------------------------------------------------------------------------------------------
	private void handleActionNameDuplications(final List<ChoreographerActionRequestDTO> actions, final String origin) {
		logger.debug("handleActionNameDuplications started...");
		
		final Set<String> actionNames = actions.stream()
											   .map(a -> a.getName())
											   .collect(Collectors.toSet());
		if (actionNames.size() != actions.size()) {
			throw new InvalidParameterException("Action name duplication found.", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void handleNextActionNames(final List<ChoreographerActionRequestDTO> actions, final String firstActionName, final String origin) {
		logger.debug("handleNextActionNames started...");
		
		final Set<String> actionNames = actions.stream()
				   							   .map(a -> a.getName())
				   							   .collect(Collectors.toSet());

		final Set<String> nextActionNames = actions.stream()
												   .filter(a -> !Utilities.isEmpty(a.getNextActionName()))
												   .map(a -> a.getNextActionName())
												   .collect(Collectors.toSet());
		// check if every next action reference is valid
		for (final String name : nextActionNames) {
			if (!actionNames.contains(name)) {
				throw new InvalidParameterException("Action not found: " + name, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		// check if every action is reachable (appears as first or next action)
		for (final String name : actionNames) {
			if (!(firstActionName.equals(name) || nextActionNames.contains(name))) {
				throw new InvalidParameterException("Unreachable action detected: " + name, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<String> handleFirstStepNames(final List<String> firstStepNames, final String origin, final boolean normalizeNames) {
		final List<String> result = new ArrayList<>(firstStepNames.size());
		for (final String name : firstStepNames) {
			result.add(handleName(name, "First step name is null or blank.", origin, normalizeNames));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ChoreographerStepRequestDTO> validateSteps(final List<ChoreographerStepRequestDTO> steps, final List<String> firstStepNames, final String origin, final boolean normalizeNames) {
		logger.debug("validateSteps started...");
		
		if (steps == null) {
			throw new InvalidParameterException("Step list is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (steps.size() < firstStepNames.size()) {
			throw new InvalidParameterException("The size of the step list is lesser than the size of the first step list.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final List<ChoreographerStepRequestDTO> result = new ArrayList<>(steps.size());
		
		for (final ChoreographerStepRequestDTO step : steps) {
			result.add(validateStep(step, origin, normalizeNames));
		}
		
		final Set<String> stepNames = result.stream()
					 					   	.map(sn -> sn.getName())
					 					   	.collect(Collectors.toSet());
		for (final String firstStepName : firstStepNames) {
			if (!stepNames.contains(firstStepName)) {
				throw new InvalidParameterException("Specified first step " + firstStepName + " is not found in the steps list.", HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		handleStepNameDuplications(stepNames, result.size(), origin);
		handleNextStepNames(result, firstStepNames, origin);
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private ChoreographerStepRequestDTO validateStep(final ChoreographerStepRequestDTO step, final String origin, final boolean normalizeNames) {
		logger.debug("validateStep started...");
		
		if (step == null) {
			throw new InvalidParameterException("Step is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final ChoreographerStepRequestDTO result = new ChoreographerStepRequestDTO();
		result.setName(handleName(step.getName(), "Step name is null or blank.", origin, normalizeNames));
		result.setStaticParameters(step.getStaticParameters());
		result.setQuantity(step.getQuantity() != null ? step.getQuantity() : 1);
		result.setServiceRequirement(handleServiceRequirement(step.getServiceRequirement(), origin));
		
		if (step.getNextStepNames() != null) {
			final List<String> resultNextStepNames = new ArrayList<String>(step.getNextStepNames().size());
			for (final String name : step.getNextStepNames()) {
				resultNextStepNames.add(normalizeNames ? name.trim().toLowerCase() : name);
			}
			
			result.setNextStepNames(resultNextStepNames);
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerServiceQueryFormDTO handleServiceRequirement(final ChoreographerServiceQueryFormDTO serviceRequirement, final String origin) {
		logger.debug("handleServiceRequirement started...");
		
		if (serviceRequirement == null) {
			throw new InvalidParameterException("Service requirement is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final ChoreographerServiceQueryFormDTO result = new ChoreographerServiceQueryFormDTO(serviceRequirement, serviceRequirement.isLocalCloudOnly());
		result.setServiceDefinitionRequirement(handleName(serviceRequirement.getServiceDefinitionRequirement(), "Service definition is null or blank.", origin, true));
		
		if (serviceRequirement.getMinVersionRequirement() != null && serviceRequirement.getMaxVersionRequirement() != null && serviceRequirement.getMinVersionRequirement() > serviceRequirement.getMaxVersionRequirement()) {
			throw new InvalidParameterException("Minimum version cannot be greater than maximum version.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private void handleStepNameDuplications(final Set<String> stepNames, final int stepCount, final String origin) {
		logger.debug("handleStepNameDuplications started...");
		
		if (stepNames.size() != stepCount) {
			throw new InvalidParameterException("Step name duplication found.", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void handleNextStepNames(final List<ChoreographerStepRequestDTO> steps, final List<String> firstStepNames, final String origin) {
		logger.debug("handleNextStepNames started...");
		
		final Set<String> stepNames = steps.stream()
				   						   .map(a -> a.getName())
				   						   .collect(Collectors.toSet());
		
		final Set<String> nextStepNames = steps.stream()
											   .filter(s -> s.getNextStepNames() != null)
											   .flatMap(s -> s.getNextStepNames().stream())
											   .collect(Collectors.toSet());
		
		// check if every next step reference is valid
		for (final String name : nextStepNames) {
			if (!stepNames.contains(name)) {
				throw new InvalidParameterException("Step not found: " + name, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		// check is every step is reachable (appears as first or next step)
		for (final String name : stepNames) {
			if (!(firstStepNames.contains(name) || nextStepNames.contains(name))) {
				throw new InvalidParameterException("Step is unreachable: " + name, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ChoreographerActionRequestDTO> validateAndNormalizeActions(final List<ChoreographerActionRequestDTO> actions) {
		logger.debug("validateAndNormalizeActions started...");
		
		final List<ChoreographerActionRequestDTO> result = new ArrayList<>(actions.size());
		for (final ChoreographerActionRequestDTO action : actions) {
			result.add(validateAndNormalizeAction(action));
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private ChoreographerActionRequestDTO validateAndNormalizeAction(final ChoreographerActionRequestDTO action) {
		logger.debug("validateAndNormalizeAction started...");
		
		StepGraph graph = actionUtils.createStepGraphFromAction(action);

		// circle detection
		if (stepGraphUtils.hasCircle(graph)) {
			throw new InvalidParameterException("Circular reference detected between the steps of action: " + action.getName());
		}
		
		// normalization
		graph = stepGraphUtils.normalizeStepGraph(graph);
		
		return actionUtils.transformActionWithGraph(graph, action);
	}
}