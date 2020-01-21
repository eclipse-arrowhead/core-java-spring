package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepNextStepConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.dto.internal.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerNextActionStepResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerStep createStep(final String name, final String serviceName, final String metadata, final String parameters, final long actionId) {
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
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        try {
            final Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findById(actionId);
            if (actionOptional.isPresent()) {
                return choreographerStepRepository.saveAndFlush(new ChoreographerStep(name, serviceName, metadata, parameters, actionOptional.get()));
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
        try {
        	final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findByNameAndActionId(stepName, actionId);
            if (stepOptional.isPresent()) {
                stepEntry = stepOptional.get();
            } else {
                throw new InvalidParameterException("The step doesn't exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        final List<ChoreographerStep> nextSteps = new ArrayList<>(nextStepNames.size());
        try {
            for(final String nextStepName : nextStepNames) {
                final Optional<ChoreographerStep> stepOptional = choreographerStepRepository.findByName(nextStepName);
                if (stepOptional.isPresent()) {
                    nextSteps.add(stepOptional.get());
                } else {
                    throw new InvalidParameterException("Step with name of " + nextStepName + " doesn't exist!");
                }
            }
            
            for(final ChoreographerStep nextStep : nextSteps) {
            	final ChoreographerStepNextStepConnection stepNextStepConnection =
                        choreographerStepNextStepConnectionRepository.save(new ChoreographerStepNextStepConnection(stepEntry, nextStep));
            	stepEntry.getSteps().add(stepNextStepConnection);
            }
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
    public ChoreographerAction createChoreographerAction(final String name, final List<String> firstStepNames, final long planId, final List<ChoreographerStepRequestDTO> steps) {
        logger.debug("createChoreographerAction started...");

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
                    actionEntry.getStepEntries().add(createStep(step.getName(), step.getServiceName(), step.getMetadata(), step.getParameters(), actionEntry.getId()));
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
        	final Optional<ChoreographerAction> choreographerActionOpt = choreographerActionRepository.findByNameAndPlanId(name, planId);
        	
            if (choreographerActionOpt.isPresent()) {
                choreographerAction = choreographerActionOpt.get();
            } else {
                throw new InvalidParameterException("Action with given Action Name of " + name + "doesn't exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        try {
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
                throw new InvalidParameterException("Plan with given name already exists! Plan NAMES must be UNIQUE!");
            });
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        try {
        	final ChoreographerPlan actionPlan = new ChoreographerPlan(name);
        	final ChoreographerPlan actionPlanEntry = choreographerPlanRepository.save(actionPlan);
        	
            if (actions != null && !actions.isEmpty()) {
                for (final ChoreographerActionRequestDTO action : actions) {
                    actionPlanEntry.getActions().add(choreographerActionRepository.saveAndFlush(createChoreographerAction(action.getName(), action.getFirstStepNames(), actionPlanEntry.getId(), action.getSteps())));
                }

                for (final ChoreographerActionRequestDTO action : actions) {
                    addNextActionToAction(action.getName(), action.getNextActionName(), actionPlanEntry.getId());
                }
            } else {
                throw new InvalidParameterException("Plan doesn't have any actions or the action field is blank.");
            }

            return choreographerPlanRepository.saveAndFlush(actionPlanEntry);
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    /*
    //-------------------------------------------------------------------------------------------------
	public Page<ChoreographerActionPlan> getChoreographerActionPlanEntries(final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getChoreographerActionPlanEntries started... ");

        final int validatedPage = page < 0 ? 0 : page;
        final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
        final Direction validatedDirection = direction == null ? Direction.ASC : direction;
        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!ChoreographerActionPlan.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            return choreographerActionPlanRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerActionPlanResponseDTO> getChoreographerActionPlanEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getChoreographerActionPlanEntriesResponse started...");

        final Page<ChoreographerActionPlan> choreographerActionPlanEntries = getChoreographerActionPlanEntries(page, size, direction, sortField);

        final List<ChoreographerActionPlanResponseDTO> actionPlanResponseDTOS = new ArrayList<>();
        for (final ChoreographerActionPlan actionPlan : choreographerActionPlanEntries) {
            actionPlanResponseDTOS.add(DTOConverter.convertChoreographerActionPlanToChoreographerActionPlanResponseDTO(actionPlan));
        }

        return actionPlanResponseDTOS;
    }

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlan getChoreographerActionPlanById(final long id) {
        logger.debug("getChoreographerActionPlanById started...");

        try {
            final Optional<ChoreographerActionPlan> actionPlanOpt = choreographerActionPlanRepository.findById(id);
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
	public ChoreographerActionPlanResponseDTO getChoreographerActionPlanByIdResponse(final long id) {
        logger.debug("getChoreographerActionPlanByIdResponse started...");

        return DTOConverter.convertChoreographerActionPlanToChoreographerActionPlanResponseDTO(getChoreographerActionPlanById(id));
    }

    //-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
    public void removeActionPlanEntryById(final long id) {
        logger.debug("removeActionPlanEntryById started...");

        try {
            if (!choreographerActionPlanRepository.existsById(id)) {
                throw new InvalidParameterException("ActionPlan with id of '" + id + "' doesn't exist!");
            }
            choreographerActionPlanRepository.deleteById(id);
            choreographerActionPlanRepository.flush();
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
 */
}