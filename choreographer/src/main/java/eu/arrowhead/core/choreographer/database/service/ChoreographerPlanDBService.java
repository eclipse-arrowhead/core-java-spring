/********************************************************************************
 * Copyright (c) 2020 AITIA
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepNextStepConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanValidator;

@Service
public class ChoreographerPlanDBService {
	
	//=================================================================================================
	// members

	@Autowired
	private ChoreographerPlanRepository choreographerPlanRepository;

	@Autowired
	private ChoreographerActionRepository choreographerActionRepository;

	@Autowired
	private ChoreographerStepRepository choreographerStepRepository;

	@Autowired
	private ChoreographerStepNextStepConnectionRepository choreographerStepNextStepConnectionRepository;

	@Autowired
	private ChoreographerSessionRepository choreographerSessionRepository;

	@Autowired
	private ChoreographerPlanValidator planValidator;

	private final Logger logger = LogManager.getLogger(ChoreographerPlanDBService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Page<ChoreographerPlan> getPlanEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getPlanEntries started... ");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

		if (!ChoreographerPlan.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '"
					+ validatedSortField + "' is not available");
		}

		try {
			return choreographerPlanRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlanListResponseDTO getPlanEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getPlanEntriesResponse started...");

		final Page<ChoreographerPlan> planEntries = getPlanEntries(page, size, direction, sortField);

		final List<ChoreographerPlanResponseDTO> planDTOs = new ArrayList<>(planEntries.getSize());
		try {
			for (final ChoreographerPlan plan : planEntries) {
				planDTOs.add(DTOConverter.convertPlanToPlanResponseDTO(plan, getPlanDetails(plan)));
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}

		return new ChoreographerPlanListResponseDTO(planDTOs, planEntries.getTotalElements());
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlan getPlanById(final long id) {
		logger.debug("getPlanById started...");

		try {
			final Optional<ChoreographerPlan> planOpt = choreographerPlanRepository.findById(id);
			if (planOpt.isPresent()) {
				return planOpt.get();
			} else {
				throw new InvalidParameterException("Choreographer Plan with id of '"
						+ id + "' doesn't exist!");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ChoreographerPlan> getPlanByName(final String name) {
		logger.debug("getPlanByName started...");
		
		return choreographerPlanRepository.findByName(name);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerPlanResponseDTO getPlanByIdResponse(final long id) {
		logger.debug("getPlanByIdResponse started...");

		final ChoreographerPlan plan = getPlanById(id);
		try {
			return DTOConverter.convertPlanToPlanResponseDTO(plan, getPlanDetails(plan));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removePlanEntryById(final long id) {
		logger.debug("removePlanEntryById started...");

		try {
			final Optional<ChoreographerPlan> planOpt = choreographerPlanRepository.findById(id);
			if (planOpt.isEmpty()) {
				throw new InvalidParameterException("Choreographer Plan with id of '" + id + "' doesn't exist!");
			}

			final List<ChoreographerSession> sessions = choreographerSessionRepository.findByPlanAndStatusIn(planOpt.get(), List.of(ChoreographerSessionStatus.INITIATED, ChoreographerSessionStatus.RUNNING));
			if (!sessions.isEmpty()) {
				throw new ArrowheadException("Choreographer Plan cannot be deleted, because it is currently executed.");
			}

			choreographerPlanRepository.deleteById(id);
			choreographerPlanRepository.flush();
		} catch (final ArrowheadException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ChoreographerPlanResponseDTO createPlanResponse(final ChoreographerPlanRequestDTO request) {
		logger.debug("createPlanResponse started...");

		final ChoreographerPlan planEntry = createPlan(request);
		try {
			final Map<ChoreographerAction,List<ChoreographerStep>> planDetails = getPlanDetails(planEntry);

			return DTOConverter.convertPlanToPlanResponseDTO(planEntry, planDetails);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public ChoreographerPlan createPlan(final ChoreographerPlanRequestDTO request) {
		logger.debug("createPlan started...");
		
		if (request == null) {
			throw new InvalidParameterException("Request is null!");
		}

		if (Utilities.isEmpty(request.getName())) {
			throw new InvalidParameterException("Plan name is null or blank!");
		}
		
		try {
			final Optional<ChoreographerPlan> planOptional = choreographerPlanRepository.findByName(request.getName().trim().toLowerCase());
			planOptional.ifPresent(choreographerActionPlan -> {
				throw new InvalidParameterException("Plan with specified name already exists.");
			});

			final ChoreographerPlanRequestDTO normalizedRequest = planValidator.validateAndNormalizePlan(request);

			ChoreographerPlan plan = new ChoreographerPlan(normalizedRequest.getName());
			plan = choreographerPlanRepository.save(plan);

			final List<ChoreographerActionRequestDTO> actionRequests = normalizedRequest.getActions();

			for (final ChoreographerActionRequestDTO action : actionRequests) {
				createAction(plan, action.getName(), action.getSteps(), action.getFirstStepNames());
			}

			for (final ChoreographerActionRequestDTO action : actionRequests) {
				addNextActionToAction(plan, action.getName(), action.getNextActionName());
			}

			final Optional<ChoreographerAction> actionOpt = choreographerActionRepository.findByNameAndPlan(normalizedRequest.getFirstActionName(), plan);
			final ChoreographerAction firstAction = actionOpt.get();
			firstAction.setFirstAction(true);
			plan.setFirstAction(firstAction);
			choreographerActionRepository.saveAndFlush(firstAction);

			return choreographerPlanRepository.saveAndFlush(plan);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerStep getStepById(final long id) {
		logger.debug("getStepById started...");

		try {
			final Optional<ChoreographerStep> stepOpt = choreographerStepRepository.findById(id);
			if (stepOpt.isPresent()) {
				return stepOpt.get();
			} else {
				throw new InvalidParameterException("Step with id of '" + id + "' doesn't exist!");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerStep> collectStepsFromPlan(final long planId) {
		logger.debug("collectStepsPlan started...");

		try {
			final Optional<ChoreographerPlan> planOpt = choreographerPlanRepository.findById(planId);
			if (planOpt.isEmpty()) {
				throw new InvalidParameterException("Plan with id of '" + planId + "' doesn't exist!");
			}
			final List<ChoreographerAction> actions = choreographerActionRepository.findByPlan(planOpt.get());			
			return actions.isEmpty() ? List.of() : choreographerStepRepository.findByActionIn(actions);
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ChoreographerStep> getFirstSteps(final long actionId) {
		logger.debug("getFirstSteps started...");
		
		try {
			final Optional<ChoreographerAction> actionOpt = choreographerActionRepository.findById(actionId);
			if (actionOpt.isEmpty()) {
				throw new InvalidParameterException("Action with id of " + actionId + " doesn't exists");
			}
			
			return choreographerStepRepository.findByActionAndFirstStep(actionOpt.get(), true);
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Map<ChoreographerAction,List<ChoreographerStep>> getPlanDetails(final ChoreographerPlan plan) {
		final List<ChoreographerAction> actions = choreographerActionRepository.findByPlan(plan);
		final Map<ChoreographerAction,List<ChoreographerStep>> result = new HashMap<>(actions.size());

		for (final ChoreographerAction action : actions) {
			final List<ChoreographerStep> steps = choreographerStepRepository.findByAction(action);
			result.put(action, steps);
		}

		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private ChoreographerAction createAction(final ChoreographerPlan plan, final String name, final List<ChoreographerStepRequestDTO> steps, final List<String> firstStepNames) {
		logger.debug("createAction started...");

		ChoreographerAction action = new ChoreographerAction();
		action.setPlan(plan);
		action.setName(name);

		action = choreographerActionRepository.save(action);

		for (final ChoreographerStepRequestDTO step : steps) {
			createStep(action, step);
		}

		for (final ChoreographerStepRequestDTO step : steps) {
			final List<String> nextStepNames = step.getNextStepNames();
			if (nextStepNames != null && !nextStepNames.isEmpty()) {
				addNextStepsToStep(action, step.getName(), nextStepNames);
			}
		}

		for (final String stepName : firstStepNames) {
			final Optional<ChoreographerStep> stepOpt = choreographerStepRepository.findByNameAndAction(stepName, action);
			final ChoreographerStep step = stepOpt.get();
			step.setFirstStep(true);
			choreographerStepRepository.saveAndFlush(step);
		}

		return choreographerActionRepository.saveAndFlush(action);
	}

	//-------------------------------------------------------------------------------------------------
	private ChoreographerStep createStep(final ChoreographerAction action, final ChoreographerStepRequestDTO stepRequest) {
		logger.debug("createStep started...");

		final ChoreographerServiceQueryFormDTO serviceRequirement = stepRequest.getServiceRequirement();
		Integer minVersion;
		Integer maxVersion;

		if (serviceRequirement.getVersionRequirement() != null) {
			minVersion = maxVersion = serviceRequirement.getVersionRequirement();
		} else {
			minVersion = serviceRequirement.getMinVersionRequirement();
			maxVersion = serviceRequirement.getMaxVersionRequirement();
		}

		final ChoreographerStep step = new ChoreographerStep(stepRequest.getName(), 
															 action,
															 serviceRequirement.getServiceDefinitionRequirement(),
															 minVersion,
															 maxVersion,
															 Utilities.toJson(serviceRequirement),
															 Utilities.map2Text(stepRequest.getStaticParameters()),
															 stepRequest.getQuantity());

		return choreographerStepRepository.saveAndFlush(step);
	}

	//-------------------------------------------------------------------------------------------------
	private ChoreographerStep addNextStepsToStep(final ChoreographerAction action, final String stepName, final List<String> nextStepNames) {
		logger.debug("addNextStepsToStep started...");

		final Optional<ChoreographerStep> stepOpt = choreographerStepRepository.findByNameAndAction(stepName, action);
		ChoreographerStep step = stepOpt.get();

		final List<ChoreographerStep> nextSteps = new ArrayList<>(nextStepNames.size());
		for (final String nextStepName : nextStepNames) {
			final Optional<ChoreographerStep> nextStepOpt = choreographerStepRepository.findByNameAndAction(nextStepName, action);
			nextSteps.add(nextStepOpt.get());
		}

		final List<ChoreographerStepNextStepConnection> nextStepConnections = new ArrayList<>(nextSteps.size());
		for (final ChoreographerStep nextStep : nextSteps) {
			final ChoreographerStepNextStepConnection stepNextStepConnection = new ChoreographerStepNextStepConnection(step, nextStep);
			step.getNextStepConnections().add(stepNextStepConnection);
			nextStep.getPreviousStepConnections().add(stepNextStepConnection);
			nextStepConnections.add(stepNextStepConnection);
		}
		choreographerStepNextStepConnectionRepository.saveAll(nextStepConnections);
		choreographerStepNextStepConnectionRepository.flush();
		choreographerStepRepository.saveAll(nextSteps);

		return choreographerStepRepository.saveAndFlush(step);
	}

	//-------------------------------------------------------------------------------------------------
	private void addNextActionToAction(final ChoreographerPlan plan, final String name, final String nextActionName) {
		logger.debug("addNextActionToAction started...");
		
		if (!Utilities.isEmpty(nextActionName)) {
			final Optional<ChoreographerAction> actionOpt = choreographerActionRepository.findByNameAndPlan(name, plan);
			final ChoreographerAction action = actionOpt.get();
			
			final Optional<ChoreographerAction> nextActionOpt = choreographerActionRepository.findByNameAndPlan(nextActionName, plan);
			action.setNextAction(nextActionOpt.get());
			
			choreographerActionRepository.saveAndFlush(action);
		}
	}
}