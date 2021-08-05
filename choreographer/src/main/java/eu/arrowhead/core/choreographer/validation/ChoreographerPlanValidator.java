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
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class ChoreographerPlanValidator {
	
	//=================================================================================================
	// members
	
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

		//TODO: implement
		return null;
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
		handleNextActionNames(result, origin);
		
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
		
		if (action.getNextActionName() != null) {
			result.setNextActionName(normalizeNames ? action.getNextActionName().trim().toLowerCase() : action.getNextActionName());
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
		
		return nameFound(actionNames, firstActionName);
	}


	//-------------------------------------------------------------------------------------------------
	private boolean nameFound(final Set<String> names, final String candidate) {
		logger.debug("nameFound started...");
		
		for (final String name : names) {
			if (candidate.equals(name)) {
				return true;
			}
		}
		
		return false;
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
	private void handleNextActionNames(final List<ChoreographerActionRequestDTO> actions, final String origin) {
		logger.debug("handleNextActionNames started...");
		
		final Set<String> nextActionNames = actions.stream()
												   .filter(a -> !Utilities.isEmpty(a.getNextActionName()))
												   .map(a -> a.getNextActionName())
												   .collect(Collectors.toSet());
		
		for (final String name : nextActionNames) {
			if (!nameFound(nextActionNames, name)) {
				throw new InvalidParameterException("Action not found: " + name, HttpStatus.SC_BAD_REQUEST, origin);
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
	private List<ChoreographerStepRequestDTO> validateSteps(final List<ChoreographerStepRequestDTO> steps, final List<String> resultFirstStepNames, final String origin, final boolean normalizeNames) {
		logger.debug("validateSteps started...");
		//TODO: 
		// steps check (null, size)
		// check every step
		// first steps are found
		// step name duplication
		// next steps are found
		
		return null;
	}
}