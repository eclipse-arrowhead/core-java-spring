package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerActionActionStepConnection;
import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import eu.arrowhead.common.database.entity.ChoreographerActionPlanActionConnection;
import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.database.entity.ChoreographerActionStepServiceDefinitionConnection;
import eu.arrowhead.common.database.entity.ChoreographerNextActionStep;
import eu.arrowhead.common.database.entity.ChoreographerWorkspace;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.*;

import eu.arrowhead.common.dto.internal.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerActionStepRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerWorkspaceResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerActionPlanResponseDTO;
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

    @Autowired
    private ChoreographerWorkspaceRepository choreographerWorkspaceRepository;

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);

    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStep createChoreographerActionStepWithUsedService(final String stepName, final Set<String> usedServiceNames) {
        logger.debug("createChoreographerActionStep started...");

        try {
            if(Utilities.isEmpty(stepName)) {
                throw new InvalidParameterException("ActionStep name is null or blank.");
            }

            if(usedServiceNames == null || usedServiceNames.isEmpty()) {
                throw new InvalidParameterException("UsedService name is null or blank.");
            }

            Optional<ChoreographerActionStep> choreographerActionStepOpt = choreographerActionStepRepository.findByName(stepName);

            choreographerActionStepOpt.ifPresent(choreographerActionStep -> {
                throw new InvalidParameterException("One or more ActionSteps with the given names already exist! ActionStep NAMES must be UNIQUE!");
            });
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        List<ServiceDefinition> usedServices = new ArrayList<>(usedServiceNames.size());
        try {
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
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        ChoreographerActionStep stepEntry = choreographerActionStepRepository.save(new ChoreographerActionStep(stepName));
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

        try {
            if(Utilities.isEmpty(stepName)) {
                throw new InvalidParameterException("Step name is empty or null.");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        ChoreographerActionStep stepEntry;
        Optional<ChoreographerActionStep> choreographerActionStepOpt = choreographerActionStepRepository.findByName(stepName);
        try {
            if (choreographerActionStepOpt.isPresent()) {
                stepEntry = choreographerActionStepOpt.get();
            } else {
                throw new InvalidParameterException("The Choreographer Action Step doesn't exist!");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        List<ChoreographerActionStep> nextActionSteps = new ArrayList<>(nextActionStepNames.size());
        try {
            for(String nextActionStepName : nextActionStepNames) {
                Optional<ChoreographerActionStep> actionStepOpt = choreographerActionStepRepository.findByName(nextActionStepName);
                if (actionStepOpt.isPresent()) {
                    nextActionSteps.add(actionStepOpt.get());
                } else {
                    throw new InvalidParameterException("Action Step with name of " + nextActionStepName + " doesn't exist!");
                }
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
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

        try {
            if(Utilities.isEmpty(actionName)) {
                throw new InvalidParameterException("Action name is null or blank.");
            }

            Optional<ChoreographerAction> choreographerActionOpt = choreographerActionRepository.findByActionName(actionName);

            choreographerActionOpt.ifPresent(choreographerAction -> {
                throw new InvalidParameterException("One or more Actions with the given names already exist! Action NAMES must be UNIQUE!");
            });
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        ChoreographerAction action = new ChoreographerAction();
        action.setActionName(actionName);

        ChoreographerAction actionEntry = choreographerActionRepository.save(action);
        if(actionSteps != null & !actionSteps.isEmpty()) {
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
        }

        choreographerActionActionStepConnectionRepository.flush();

        return choreographerActionRepository.saveAndFlush(actionEntry);
    }

    @Transactional
    public ChoreographerAction addNextActionToChoreographerAction(final String actionName, final String nextActionName) {
        logger.debug("addNextActionToChoreographerAction started...");

        try {
            if (Utilities.isEmpty(actionName)) {
                throw new InvalidParameterException("Action name or next Action name is null or blank.");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        Optional<ChoreographerAction> choreographerActionOpt = choreographerActionRepository.findByActionName(actionName);

        ChoreographerAction choreographerAction;

        try {
            if(choreographerActionOpt.isPresent()) {
                choreographerAction = choreographerActionOpt.get();
            } else {
                throw new InvalidParameterException("Action with given Action Name of " + actionName + "doesn't exist!");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        Optional<ChoreographerAction> nextActionOpt = choreographerActionRepository.findByActionName(nextActionName);
        try {
            if(nextActionOpt.isPresent()) {
                choreographerAction.setNextAction(nextActionOpt.get());
            } else if (nextActionName != null) {
                throw new InvalidParameterException("Action with given Action Name of " + nextActionName + " doesn't exist!");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        return choreographerActionRepository.saveAndFlush(choreographerAction);
    }

    @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerActionPlan createChoreographerActionPlan(final String actionPlanName, final List<ChoreographerActionRequestDTO> actions) {
        logger.debug("createChoreographerActionPlan started...");

        try {
            if(Utilities.isEmpty(actionPlanName)) {
                throw new InvalidParameterException("ActionPlan name is null or blank!");
            }

            Optional<ChoreographerActionPlan> choreographerActionPlanOpt = choreographerActionPlanRepository.findByActionPlanName(actionPlanName);

            choreographerActionPlanOpt.ifPresent(choreographerActionPlan -> {
                throw new InvalidParameterException("ActionPlan with given name already exists! ActionPlan NAMES must be UNIQUE!");
            });
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        ChoreographerActionPlan actionPlan = new ChoreographerActionPlan(actionPlanName);

        ChoreographerActionPlan actionPlanEntry = choreographerActionPlanRepository.save(actionPlan);

        try {
            if(actions != null && !actions.isEmpty()) {
                for(ChoreographerActionRequestDTO action : actions) {
                    ChoreographerActionPlanActionConnection connection = choreographerActionPlanActionConnectionRepository
                            .save(new ChoreographerActionPlanActionConnection(actionPlanEntry, createChoreographerAction(action.getActionName(), action.getActionSteps())));
                    actionPlanEntry.getActionPlanActionConnections().add(connection);
                }

                for (ChoreographerActionRequestDTO action : actions) {
                    addNextActionToChoreographerAction(action.getActionName(), action.getNextActionName());
                }

            } else {
                throw new InvalidParameterException("ActionPlan doesn't have any actions or the action field is blank.");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        choreographerActionPlanActionConnectionRepository.flush();

        return choreographerActionPlanRepository.saveAndFlush(actionPlanEntry);
    }

    public ChoreographerActionPlan createChoreographerActionPlanWithExistingActions(final String actionPlanName, List<ChoreographerActionRequestDTO> actions) {
        logger.debug("createChoreographerActionPlanWithExistingActions started...");

        Optional<ChoreographerActionPlan> choreographerActionPlanOpt = choreographerActionPlanRepository.findByActionPlanName(actionPlanName);

        try {
            choreographerActionPlanOpt.ifPresent(choreographerActionPlan -> {
                throw new InvalidParameterException("ActionPlan with given name already exists! ActionPlan NAMES must be UNIQUE!");
            });
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        ChoreographerActionPlan actionPlan = new ChoreographerActionPlan(actionPlanName);

        ChoreographerActionPlan actionPlanEntry = choreographerActionPlanRepository.save(actionPlan);

        List<ChoreographerAction> choreographerActions = new ArrayList<>(actions.size());
        try {
            for(ChoreographerActionRequestDTO action : actions) {
                String nextActionName = action.getNextActionName();
                if(nextActionName != null) {
                    Optional<ChoreographerAction> nextActionOptional = choreographerActionRepository.findByActionName(nextActionName);
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
                } else {
                    Optional<ChoreographerAction> actionOptional = choreographerActionRepository.findByActionName(action.getActionName());
                    actionOptional.ifPresent(choreographerActions::add);
                }
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
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
        String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!ChoreographerActionPlan.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        try {
            return choreographerActionPlanRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
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
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
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
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerWorkspace createChoreographerWorkspace(final String name, final double x, final double y, final double z, final double r) {
        logger.debug("createChoreographerWorkspace started...");

        try {
            if(Utilities.isEmpty(name)) {
                throw new InvalidParameterException("Name of workspace is null or blank.");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }

        return choreographerWorkspaceRepository.saveAndFlush(new ChoreographerWorkspace(name, x, y, z, r));
    }

    public Page<ChoreographerWorkspace> getChoreographerWorkspaceEntries(final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getChoreographerWorkspaceEntries started... ");

        int validatedPage = page < 0 ? 0 : page;
        int validatedSize = size <= 0 ? Integer.MAX_VALUE : size;
        Direction validatedDirection = direction == null ? Direction.ASC : direction;
        String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        try {
            if (!ChoreographerWorkspace.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
                throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
            }


            return choreographerWorkspaceRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public List<ChoreographerWorkspaceResponseDTO> getChoreographerWorkspaceEntriesResponse (final int page, final int size, final Direction direction, final String sortField) {
        logger.debug("getChoreographerWorkspaceEntriesResponse started...");

        Page<ChoreographerWorkspace> choreographerWorkspaceEntries = getChoreographerWorkspaceEntries(page, size, direction, sortField);

        List<ChoreographerWorkspaceResponseDTO> choreographerWorkspaceResponseDTOS = new ArrayList<>();
        for(ChoreographerWorkspace workspace : choreographerWorkspaceEntries) {
            choreographerWorkspaceResponseDTOS.add(DTOConverter.convertChoreographerWorkspaceToChoreographerWorkspaceResponseDTO(workspace));
        }

        return choreographerWorkspaceResponseDTOS;
    }

    public ChoreographerWorkspace getChoreographerWorkspace(final long id) {
        logger.debug("getChoreographerWorkspace started...");

        try {
            Optional<ChoreographerWorkspace> workspaceOpt = choreographerWorkspaceRepository.findById(id);
            if (workspaceOpt.isPresent()) {
                return workspaceOpt.get();
            } else {
                throw new InvalidParameterException("Workspace with id of '" + id + "' doesn't exist!");
            }
        } catch (InvalidParameterException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public void removeWorkspaceEntryById(final long id) {
        logger.debug("removeWorkspaceEntryById started...");

        try {
            if(!choreographerWorkspaceRepository.existsById(id)) {
                throw new InvalidParameterException("Workspace with id of '" + id + "' doesn't exist!");
            }
            choreographerWorkspaceRepository.deleteById(id);
            choreographerWorkspaceRepository.flush();
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public ChoreographerWorkspaceResponseDTO getChoreographerWorkspaceByIdResponse (final long id) {
        logger.debug("getChoreographerWorkspaceByIdResponse started...");

        return DTOConverter.convertChoreographerWorkspaceToChoreographerWorkspaceResponseDTO(getChoreographerWorkspace(id));
    }
}
