package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.*;
import eu.arrowhead.common.database.repository.*;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.choreographer.*;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort.Direction;

import java.util.*;

@Service
public class ChoreographerDBService {

    @Autowired
    private ServiceDefinitionRepository serviceDefinitionRepository;

    @Autowired
    private ChoreographerActionPlanRepository choreographerActionPlanRepository;

    @Autowired
    private ChoreographerActionRepository choreographerActionRepository;

    @Autowired
    private ChoreographerActionStepRepository choreographerActionStepRepository;

    @Autowired
    private ChoreographerActionStepServiceDefinitionConnectionRepository choreographerActionStepServiceDefinitionConnectionRepository;

    @Autowired
    private ChoreographerNextActionStepRepository choreographerNextActionStepRepository;

    @Autowired
    private ChoreographerActionActionStepConnectionRepository choreographerActionActionStepConnectionRepository;

    @Autowired
    private ChoreographerActionPlanActionConnectionRepository choreographerActionPlanActionConnectionRepository;

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);

    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStep createChoreographerActionStepWithUsedService(final String stepName, final Set<String> usedServiceNames) {
        logger.debug("createChoreographerActionStep started...");

        Optional<ChoreographerActionStep> choreographerActionStepOpt = choreographerActionStepRepository.findByName(stepName);

        choreographerActionStepOpt.ifPresent(choreographerActionStep -> {
            throw new InvalidParameterException("One or more ActionSteps with the given names already exist! ActionStep NAMES must be UNIQUE!");
        });

        List<ServiceDefinition> usedServices = new ArrayList<>(usedServiceNames.size());
        for (String name : usedServiceNames) {
            Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findByServiceDefinition(name);
            if (serviceOpt.isPresent()) {
                usedServices.add(serviceOpt.get());
            } else {
                logger.debug("Service Definition with name of " + name + " doesn't exist!");
            }
        }

        if (usedServices.size() != usedServiceNames.size()) {
            throw new InvalidParameterException("One or more of the Services given doesn't exist! Create ALL Services before usage.");
        }

        ChoreographerActionStep step = new ChoreographerActionStep(stepName);
        ChoreographerActionStep stepEntry = choreographerActionStepRepository.save(step);
        for (ServiceDefinition serviceDefinition : usedServices) {
            ChoreographerActionStepServiceDefinitionConnection connection =
                    choreographerActionStepServiceDefinitionConnectionRepository.save(new ChoreographerActionStepServiceDefinitionConnection(stepEntry, serviceDefinition));
            stepEntry.getActionStepServiceDefinitionConnections().add(connection);
        }
        choreographerActionStepServiceDefinitionConnectionRepository.flush();

        return choreographerActionStepRepository.saveAndFlush(stepEntry);
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStep addNextStepToChoreographerActionStep(final String stepName, final Set<String> nextActionStepNames) {
        logger.debug("addNextStepToChoreographerActionStep started...");

        ChoreographerActionStep stepEntry;
        Optional<ChoreographerActionStep> choreographerActionStepOpt = choreographerActionStepRepository.findByName(stepName);
        if (choreographerActionStepOpt.isPresent()) {
            stepEntry = choreographerActionStepOpt.get();
        } else {
            throw new InvalidParameterException("The Choreographer Action Step doesn't exist!");
        }

        List<ChoreographerActionStep> nextActionSteps = new ArrayList<>(nextActionStepNames.size());
        for(String nextActionStepName : nextActionStepNames) {
            Optional<ChoreographerActionStep> actionStepOpt = choreographerActionStepRepository.findByName(nextActionStepName);
            if (actionStepOpt.isPresent()) {
                nextActionSteps.add(actionStepOpt.get());
            } else {
                throw new InvalidParameterException("Action Step with name of " + nextActionStepName + " doesn't exist!");
            }
        }

        for(ChoreographerActionStep actionStep : nextActionSteps) {
            ChoreographerNextActionStep nextActionStep = choreographerNextActionStepRepository.save(new ChoreographerNextActionStep(stepEntry, actionStep));
            stepEntry.getActionSteps().add(nextActionStep);
        }
        choreographerNextActionStepRepository.flush();

        return choreographerActionStepRepository.saveAndFlush(stepEntry);
    }

    @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerAction createChoreographerAction(final String actionName, final List<ChoreographerActionStepRequestDTO> actionSteps) {
        logger.debug("createChoreographerAction started...");

        Optional<ChoreographerAction> choreographerActionOpt = choreographerActionRepository.findByActionName(actionName);

        choreographerActionOpt.ifPresent(choreographerAction -> {
            throw new InvalidParameterException("One or more Actions with the given names already exist! Action NAMES must be UNIQUE!");
        });

        ChoreographerAction action = new ChoreographerAction();
        action.setActionName(actionName);

        ChoreographerAction actionEntry = choreographerActionRepository.save(action);
        for (ChoreographerActionStepRequestDTO actionStep : actionSteps) {
            ChoreographerActionActionStepConnection connection = choreographerActionActionStepConnectionRepository
                    .save(new ChoreographerActionActionStepConnection(createChoreographerActionStepWithUsedService(actionStep.getActionStepName(), new HashSet<>(actionStep.getUsedServiceNames())),
                            actionEntry));
            actionEntry.getActionActionStepConnections().add(connection);
        }

        for (ChoreographerActionStepRequestDTO actionStep : actionSteps) {
            List<String> nextActionStepNames = actionStep.getNextActionStepNames();
            if(nextActionStepNames != null && !nextActionStepNames.isEmpty()) {
                addNextStepToChoreographerActionStep(actionStep.getActionStepName(), new HashSet<>(nextActionStepNames));
            }
        }

        choreographerActionActionStepConnectionRepository.flush();

        return choreographerActionRepository.saveAndFlush(actionEntry);
    }

    public ChoreographerAction addNextActionToChoreographerAction(final String actionName, final String nextActionName) {
        logger.debug("addNextActionToChoreographerAction started...");

        Optional<ChoreographerAction> choreographerActionOpt = choreographerActionRepository.findByActionName(actionName);

        ChoreographerAction choreographerAction;
        if(choreographerActionOpt.isPresent()) {
            choreographerAction = choreographerActionOpt.get();
        } else {
            throw new InvalidParameterException("Action with given Action Name of " + actionName + "doesn't exist!");
        }

        Optional<ChoreographerAction> nextActionOpt = choreographerActionRepository.findByActionName(nextActionName);
        if(nextActionOpt.isPresent()) {
            choreographerAction.setNextAction(nextActionOpt.get());
        } else {
            throw new InvalidParameterException("Action with given Action Name of " + nextActionName + "doesn't exist!");
        }

        return choreographerActionRepository.saveAndFlush(choreographerAction);
    }

    @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerActionPlan createChoreographerActionPlan(final String actionPlanName, List<ChoreographerActionRequestDTO> actions) {
        logger.debug("createChoreographerActionPlan started...");

        Optional<ChoreographerActionPlan> choreographerActionPlanOpt = choreographerActionPlanRepository.findByActionPlanName(actionPlanName);

        choreographerActionPlanOpt.ifPresent(choreographerActionPlan -> {
            throw new InvalidParameterException("ActionPlan with given name already exists! ActionPlan NAMES must be UNIQUE!");
        });

        ChoreographerActionPlan actionPlan = new ChoreographerActionPlan(actionPlanName);

        ChoreographerActionPlan actionPlanEntry = choreographerActionPlanRepository.save(actionPlan);

        for(ChoreographerActionRequestDTO action : actions) {
                ChoreographerActionPlanActionConnection connection = choreographerActionPlanActionConnectionRepository
                        .save(new ChoreographerActionPlanActionConnection(actionPlanEntry, createChoreographerAction(action.getActionName(), action.getActionSteps())));
                actionPlanEntry.getActionPlanActionConnections().add(connection);
        }

        for (ChoreographerActionRequestDTO action : actions) {
            addNextActionToChoreographerAction(action.getActionName(), action.getNextActionName());
        }

        choreographerActionPlanActionConnectionRepository.flush();

        return choreographerActionPlanRepository.saveAndFlush(actionPlanEntry);
    }

    public ChoreographerActionPlan createChoreographerActionPlanWithExistingActions(final String actionPlanName, List<ChoreographerExistingActionRequestDTO> actions) {
        logger.debug("createChoreographerActionPlanWithExistingActions started...");

        Optional<ChoreographerActionPlan> choreographerActionPlanOpt = choreographerActionPlanRepository.findByActionPlanName(actionPlanName);

        choreographerActionPlanOpt.ifPresent(choreographerActionPlan -> {
            throw new InvalidParameterException("ActionPlan with given name already exists! ActionPlan NAMES must be UNIQUE!");
        });

        ChoreographerActionPlan actionPlan = new ChoreographerActionPlan(actionPlanName);

        ChoreographerActionPlan actionPlanEntry = choreographerActionPlanRepository.save(actionPlan);

        List<ChoreographerAction> choreographerActions = new ArrayList<>(actions.size());
        for(ChoreographerExistingActionRequestDTO action : actions) {
            Optional<ChoreographerAction> nextActionOptional = choreographerActionRepository.findByActionName(action.getNextActionName());
            if(nextActionOptional.isPresent()) {
                Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findByActionNameAndNextAction(action.getActionName(), nextActionOptional.get());
                if(actionOptional.isPresent()) {
                    choreographerActions.add(actionOptional.get());
                } else {
                    throw new InvalidParameterException("One or more given Actions are not present in the database! Please create them first!");
                }
            } else {
                throw new InvalidParameterException("The NextAction you defined for an Action doesn't match with the Action's initial NextAction!");
            }
        }

        for (ChoreographerAction action : choreographerActions) {
            ChoreographerActionPlanActionConnection connection = choreographerActionPlanActionConnectionRepository
                    .save(new ChoreographerActionPlanActionConnection(actionPlanEntry, action));
            actionPlanEntry.getActionPlanActionConnections().add(connection);
        }

        return choreographerActionPlanRepository.saveAndFlush(actionPlanEntry);
    }

    public Page<ChoreographerActionPlan> getChoreographerActionPlanEntries(final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getChoreographerActionPlanEntries started... ");

        int validatedPage = page < 0 ? 0 : page;
        int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
        Direction validatedDirection = direction == null ? Direction.ASC : direction;
        String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if(!ChoreographerActionPlan.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            return choreographerActionPlanRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public List<ChoreographerActionPlanResponseDTO> getChoreographerActionPlanEntriesResponse (final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getChoreographerActionPlanEntriesResponse started...");

        Page<ChoreographerActionPlan> choreographerActionPlanEntries = getChoreographerActionPlanEntries(page, size, direction, sortField);

        List<ChoreographerActionPlanResponseDTO> actionPlanResponseDTOS = new ArrayList<>();
        for(ChoreographerActionPlan actionPlan : choreographerActionPlanEntries) {
            actionPlanResponseDTOS.add(DTOConverter.convertChoreographerActionPlanToChoreographerActionPlanResponseDTO(actionPlan));
        }

        return actionPlanResponseDTOS;
    }

    public ChoreographerActionPlan getChoreographerActionPlanById(final long id) {
        logger.debug("getChoreographerActionPlanById started...");

        try {
            Optional<ChoreographerActionPlan> actionPlanOpt = choreographerActionPlanRepository.findById(id);
            if (actionPlanOpt.isPresent()) {
                return actionPlanOpt.get();
            } else {
                throw new InvalidParameterException("Choreographer Action Plan with id of '" + id + "' doesn't exist!");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public ChoreographerActionPlanResponseDTO getChoreographerActionPlanByIdResponse (final long id) {
        logger.debug("getChoreographerActionPlanByIdResponse started...");

        return DTOConverter.convertChoreographerActionPlanToChoreographerActionPlanResponseDTO(getChoreographerActionPlanById(id));
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeActionPlanEntryById(final long id) {
        logger.debug("removeActionPlanEntryById started...");

        try {
            if(!choreographerActionPlanRepository.existsById(id)) {
                throw new InvalidParameterException("ActionPlan with id of '" + id + "' doesn't exist!");
            }
            choreographerActionPlanRepository.deleteById(id);
            choreographerActionPlanRepository.flush();
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }
}
