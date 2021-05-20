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

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerRunningStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepNextStepConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerWorklogRepository;
import eu.arrowhead.common.dto.internal.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStatusType;
import eu.arrowhead.common.dto.internal.ChoreographerStepRequestDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort.Direction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChoreographerDBService {
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
    private ChoreographerWorklogRepository choreographerWorklogRepository;

    @Autowired
    private ChoreographerRunningStepRepository choreographerRunningStepRepository;

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerStep createStep(final String name, final String serviceName, final String metadata, final String parameters, final int quantity, final long actionId) {
        logger.debug("createStep started...");

        try {
            if (Utilities.isEmpty(name)) {
                throw new InvalidParameterException("Step name is null or blank.");
            }

            if (Utilities.isEmpty(serviceName)) {
                throw new InvalidParameterException("Service name is null or blank.");
            }

            final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findByNameAndActionId(name, actionId);
            stepOptional.ifPresent(step -> {
                throw new InvalidParameterException("Action already has a step with the same name. Step names must be unique in each Action.");
            });

            final Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findById(actionId);
            if (actionOptional.isPresent()) {
                return choreographerStepRepository.saveAndFlush(new ChoreographerStep(name, serviceName, metadata, parameters, actionOptional.get(), quantity));
            } else {
                throw new InvalidParameterException("Action with given ID doesn't exist.");
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
    public ChoreographerStep addNextStepsToStep(final String stepName, final Set<String> nextStepNames, final long actionId) {
        logger.debug("addNextStepsToStep started...");

        if (Utilities.isEmpty(stepName)) {
        	throw new InvalidParameterException("Step name is empty or null.");
        }

        ChoreographerStep stepEntry;
        final List<ChoreographerStep> nextSteps = new ArrayList<>(nextStepNames.size());

        try {
        	final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findByNameAndActionId(stepName, actionId);
            if (stepOptional.isPresent()) {
                stepEntry = stepOptional.get();
            } else {
                throw new InvalidParameterException("The step doesn't exist!");
            }

            for(final String nextStepName : nextStepNames) {
                final Optional<ChoreographerStep> nextStepOptional = choreographerStepRepository.findByNameAndActionId(nextStepName, actionId);
                if (nextStepOptional.isPresent()) {
                    nextSteps.add(nextStepOptional.get());
                } else {
                    throw new InvalidParameterException("Step with name of " + nextStepName + " doesn't exist!");
                }
            }

            final List<ChoreographerStepNextStepConnection> nextStepConnections = new ArrayList<>();
            for(final ChoreographerStep nextStep : nextSteps) {
                final ChoreographerStepNextStepConnection stepNextStepConnection = new ChoreographerStepNextStepConnection(stepEntry, nextStep);
                stepEntry.getSteps().add(stepNextStepConnection);
                nextStepConnections.add(stepNextStepConnection);
            }
            choreographerStepNextStepConnectionRepository.saveAll(nextStepConnections);
            choreographerStepNextStepConnectionRepository.flush();

            return choreographerStepRepository.saveAndFlush(stepEntry);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerAction createAction(final String name, final List<String> firstStepNames, final long planId, final List<ChoreographerStepRequestDTO> steps) {
        logger.debug("createAction started...");

        try {
            if (Utilities.isEmpty(name)) {
                throw new InvalidParameterException("Action name is null or blank.");
            }

            final Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findByNameAndPlanId(name, planId);
            actionOptional.ifPresent(choreographerAction -> {
                throw new InvalidParameterException("Plan already has an action with the same name. Action names must be unique in each Plan.");
            });
            
            final ChoreographerAction action = new ChoreographerAction();

            action.setName(name);

            final Optional<ChoreographerPlan> planOptional = choreographerPlanRepository.findById(planId);
            if (planOptional.isPresent()) {
                action.setPlan(planOptional.get());
            } else {
                throw new InvalidParameterException("Plan with given ID doesn't exist.");
            }

            final ChoreographerAction actionEntry = choreographerActionRepository.save(action);

            if (steps != null && !steps.isEmpty()) {
            	for (final ChoreographerStepRequestDTO step : steps) {
                    actionEntry.getStepEntries().add(createStep(step.getName(), step.getServiceName(), step.getMetadata(), step.getParameters(), step.getQuantity(), actionEntry.getId()));
            	}

            	for (final ChoreographerStepRequestDTO step : steps) {
            		final List<String> nextStepNames = step.getNextStepNames();
            		if (nextStepNames != null && !nextStepNames.isEmpty()) {
            			addNextStepsToStep(step.getName(), new HashSet<>(nextStepNames), actionEntry.getId());
            		}
            	}
            }
            choreographerStepRepository.flush();

            if (firstStepNames != null && !firstStepNames.isEmpty()) {
                for (final String stepName : firstStepNames) {
                    final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findByNameAndActionId(stepName, actionEntry.getId());
                    if (stepOptional.isPresent()) {
                        ChoreographerStep step = stepOptional.get();
                        step.setActionFirstStep(actionEntry);
                        choreographerStepRepository.saveAndFlush(step);
                    } else {
                        throw new InvalidParameterException("The given first step(s) with the given name(s) must be included in the step list too.");
                    }
                }
            }

            return choreographerActionRepository.saveAndFlush(actionEntry);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerAction addNextActionToAction(final String name, final String nextActionName, final long planId) {
        logger.debug("addNextActionToAction started...");

        if (Utilities.isEmpty(name)) {
        	throw new InvalidParameterException("Action name or next Action name is null or blank.");
        }

        ChoreographerAction choreographerAction;

        try {
        	final Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findByNameAndPlanId(name, planId);
        	
            if (actionOptional.isPresent()) {
                choreographerAction = actionOptional.get();
            } else {
                throw new InvalidParameterException("Action with given Action Name of " + name + "doesn't exist!");
            }

            final Optional<ChoreographerAction> nextActionOpt = choreographerActionRepository.findByNameAndPlanId(nextActionName, planId);
            if (nextActionOpt.isPresent()) {
                choreographerAction.setNextAction(nextActionOpt.get());
            } else if (nextActionName != null) {
                throw new InvalidParameterException("Action with given Action Name of " + nextActionName + " doesn't exist!");
            }

            return choreographerActionRepository.saveAndFlush(choreographerAction);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerPlan createPlan(final String name, final String firstActionName, final List<ChoreographerActionRequestDTO> actions) {
        logger.debug("createPlan started...");

        try {
            if (Utilities.isEmpty(name)) {
                throw new InvalidParameterException("Plan name is null or blank!");
            }

            final Optional<ChoreographerPlan> planOptional = choreographerPlanRepository.findByName(name);
            planOptional.ifPresent(choreographerActionPlan -> {
                throw new InvalidParameterException("Plan with given name already exists! Plan names must be unique.");
            });

            final ChoreographerPlan plan = new ChoreographerPlan(name);
            final ChoreographerPlan planEntry = choreographerPlanRepository.save(plan);

            if (actions != null && !actions.isEmpty()) {
                for (final ChoreographerActionRequestDTO action : actions) {
                    planEntry.getActions().add(choreographerActionRepository.saveAndFlush(createAction(action.getName(), action.getFirstStepNames(), planEntry.getId(), action.getSteps())));
                }

                for (final ChoreographerActionRequestDTO action : actions) {
                    addNextActionToAction(action.getName(), action.getNextActionName(), planEntry.getId());
                }
            } else {
                throw new InvalidParameterException("Plan doesn't have any actions or the action field is blank.");
            }

            if (Utilities.isEmpty(firstActionName)) {
                throw new InvalidParameterException("A plan must have one first Action.");
            }

            final Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findByNameAndPlanId(firstActionName, planEntry.getId());
            if (actionOptional.isPresent()) {
                ChoreographerAction action = actionOptional.get();
                planEntry.setFirstAction(action);
            }

            return choreographerPlanRepository.saveAndFlush(planEntry);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }


    //-------------------------------------------------------------------------------------------------
	public Page<ChoreographerPlan> getPlanEntries(final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getPlanEntries started... ");

        final int validatedPage = page < 0 ? 0 : page;
        final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
        final Direction validatedDirection = direction == null ? Direction.ASC : direction;
        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!ChoreographerPlan.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            return choreographerPlanRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerPlanResponseDTO> getPlanEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getPlanEntriesResponse started...");

        final Page<ChoreographerPlan> planEntries = getPlanEntries(page, size, direction, sortField);

        final List<ChoreographerPlanResponseDTO> actionPlanResponseDTOS = new ArrayList<>();
        for (final ChoreographerPlan plan : planEntries) {
            actionPlanResponseDTOS.add(DTOConverter.convertPlanToPlanResponseDTO(plan));
        }

        return actionPlanResponseDTOS;
    }

    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlan getPlanById(final long id) {
        logger.debug("getPlanById started...");

        try {
            final Optional<ChoreographerPlan> actionPlanOpt = choreographerPlanRepository.findById(id);
            if (actionPlanOpt.isPresent()) {
                return actionPlanOpt.get();
            } else {
                throw new InvalidParameterException("Choreographer Action Plan with id of '" + id + "' doesn't exist!");
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
    public ChoreographerRunningStep registerRunningStep(final long stepId, final long sessionId, final ChoreographerStatusType status, final String message) {
        try {
            if (status == null) {
                throw new InvalidParameterException("Status is null or blank.");
            }

            if (Utilities.isEmpty(message)) {
                throw new InvalidParameterException("Message is null or blank.");
            }

            final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findById(stepId);
            final Optional<ChoreographerSession> sessionOptional = choreographerSessionRepository.findById(sessionId);

            if (stepOptional.isPresent() && sessionOptional.isPresent()) {
                return choreographerRunningStepRepository.saveAndFlush(new ChoreographerRunningStep(status, message, stepOptional.get(), sessionOptional.get()));
            } else {
                throw new InvalidParameterException("Step or Session with given id(s) not found!");
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
    public ChoreographerRunningStep setRunningStepStatus(final long runningStepId, final ChoreographerStatusType status, final String message) {
        try {
            if (status == null) {
                throw new InvalidParameterException("Status is null or blank.");
            }

            if (Utilities.isEmpty(message)) {
                throw new InvalidParameterException("Message is null or blank.");
            }

            final Optional<ChoreographerRunningStep> runningStepOptional = choreographerRunningStepRepository.findById(runningStepId);

            if (runningStepOptional.isPresent()) {
                ChoreographerRunningStep runningStepToChange = runningStepOptional.get();
                runningStepToChange.setStatus(status);
                runningStepToChange.setMessage(message);
                return choreographerRunningStepRepository.saveAndFlush(runningStepToChange);
            } else {
                throw new InvalidParameterException("Running step with given ID doesn't exist.");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlanResponseDTO getPlanByIdResponse(final long id) {
        logger.debug("getChoreographerActionPlanByIdResponse started...");

        return DTOConverter.convertPlanToPlanResponseDTO(getPlanById(id));
    }

    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public void removePlanEntryById(final long id) {
        logger.debug("removePlanEntryById started...");

        try {
            if (!choreographerPlanRepository.existsById(id)) {
                throw new InvalidParameterException("ActionPlan with id of '" + id + "' doesn't exist!");
            }
            choreographerPlanRepository.deleteById(id);
            choreographerPlanRepository.flush();
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession initiateSession(final long planId) {
        logger.debug("createSession started...");

        try {
            final Optional<ChoreographerPlan> planOptional = choreographerPlanRepository.findById(planId);
            if (planOptional.isPresent()) {
                ChoreographerSession sessionEntry = choreographerSessionRepository.saveAndFlush(new ChoreographerSession(planOptional.get(), ChoreographerStatusType.INITIATED));
                String worklogMessage = "Plan with ID of " + planId + " started running with session ID of " + sessionEntry.getId() + ".";
                createWorklog(worklogMessage, "");
                return sessionEntry;
            } else {
                throw new InvalidParameterException("Can't initiate session because the plan with the given ID doesn't exist.");
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
    public ChoreographerSession finalizeSession(final long sessionId) {
        logger.debug("finalizeSession started...");

        try {
            Optional<ChoreographerSession> sessionOptional = choreographerSessionRepository.findById(sessionId);
            if (sessionOptional.isPresent()) {
                ChoreographerSession session = sessionOptional.get();
                session.setStatus(ChoreographerStatusType.DONE);
                createWorklog("Session with ID of " + sessionId + " finished successfully.", "");
                return choreographerSessionRepository.saveAndFlush(session);
            } else {
                throw new InvalidParameterException("Session with given ID doesn't exist.");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerSession setSessionStatus(final long sessionId, final ChoreographerStatusType state) {
        logger.debug("changeSessionState started...");

        if (state == null) {
            throw new InvalidParameterException("State is null or blank.");
        }

        try {
            Optional<ChoreographerSession> sessionOptional = choreographerSessionRepository.findById(sessionId);
            if (sessionOptional.isPresent()) {
                ChoreographerSession session = sessionOptional.get();
                session.setStatus(state);
                createWorklog("New status of session with ID of " + sessionId + ": " + state, "");
                return choreographerSessionRepository.saveAndFlush(session);
            } else {
                throw new InvalidParameterException("Session with given ID doesn't exist.");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerWorklog createWorklog(final String message, final String exception) {
        logger.debug("createWorklog started...");

        try {
            if (Utilities.isEmpty(message)) {
                throw new InvalidParameterException("Message is null or blank.");
            }

            return choreographerWorklogRepository.saveAndFlush(new ChoreographerWorklog(message, exception));
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunningStep getRunningStepBySessionIdAndStepId(final long sessionId, final long stepId) {
        logger.debug("getRunningStepBySessionIdAndStepId started...");

        try {
            final Optional<ChoreographerRunningStep> runningStepOpt = choreographerRunningStepRepository.findByStepIdAndSessionId(stepId, sessionId);
            if (runningStepOpt.isPresent()) {
                return runningStepOpt.get();
            } else {
                throw new InvalidParameterException("Running step with session id of '" + sessionId + "' and step id of '" + stepId + "' doesn't exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerRunningStep getRunningStepById(final long id) {
        logger.debug("getRunningStepById started...");

        try {
            final Optional<ChoreographerRunningStep> runningStepOpt = choreographerRunningStepRepository.findById(id);
            if (runningStepOpt.isPresent()) {
                return runningStepOpt.get();
            } else {
                throw new InvalidParameterException("Running step with id of '" + id + "' doesn't exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
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
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
    public List<ChoreographerRunningStep> getAllRunningStepsBySessionId(final long sessionId) {
        logger.debug("getAllRunningStepsBySessionId started...");

        try {
            final List<ChoreographerRunningStep> runningSteps = choreographerRunningStepRepository.findAllBySessionId(sessionId);
            if (!runningSteps.isEmpty()) {
                return  runningSteps;
            } else {
                throw new InvalidParameterException("There are no running steps associated with id of '" + sessionId + "'.");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
}