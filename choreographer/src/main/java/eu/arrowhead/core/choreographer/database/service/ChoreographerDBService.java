package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.database.entity.*;
import eu.arrowhead.common.database.repository.*;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);

    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStep createChoreographerActionStepWithUsedService(final String stepName, final Set<String> usedServiceNames) {
        logger.debug("createChoreographerActionStep started...");

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

        /* List<ChoreographerActionStep> nextActionSteps = new ArrayList<>(nextActionStepNames.size());
        for (String actionStepName : nextActionStepNames) {
            Optional<ChoreographerActionStep> actionStepOpt = choreographerActionStepRepository.findByName(actionStepName);
            if (actionStepOpt.isPresent()) {
                nextActionSteps.add(actionStepOpt.get());
            } else {
                logger.debug("Action Step with name of " + actionStepName + " doesn't exist!");
            }
        }

        if (nextActionSteps.size() != nextActionStepNames.size()) {
            throw new InvalidParameterException("One or more of the Action Steps given doesn't exist! Create ALL Action Steps before usage.");
        }

        for(ChoreographerActionStep actionStep : nextActionSteps) {
            ChoreographerNextActionStep nextActionStep = choreographerNextActionStepRepository.save(new ChoreographerNextActionStep(stepEntry, actionStep));
            stepEntry.getNextActionSteps().add(nextActionStep);
        }
        choreographerNextActionStepRepository.flush();

        return choreographerActionStepRepository.saveAndFlush(stepEntry); */
    }

    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStep addNextStepToChoreographerActionStep(final String stepName, final Set<String> nextActionStepNames) {

        ChoreographerActionStep stepEntry = new ChoreographerActionStep();
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
                logger.debug("Action Step with name of " + nextActionStepName + " doesn't exist!");
            }
        }

        for(ChoreographerActionStep actionStep : nextActionSteps) {
            ChoreographerNextActionStep nextActionStep = choreographerNextActionStepRepository.save(new ChoreographerNextActionStep(stepEntry, actionStep));
            stepEntry.getNextActionSteps().add(nextActionStep);
        }
        choreographerNextActionStepRepository.flush();

        return choreographerActionStepRepository.saveAndFlush(stepEntry);
    }

    @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerAction createChoreographerAction(final String actionName, final String nextActionName, final List<ChoreographerActionStepRequestDTO> actionSteps) {
        logger.debug("createChoreographerAction started...");

        ChoreographerAction action = new ChoreographerAction();
        action.setActionName(actionName);

        /*Optional<ChoreographerAction> nextActionOpt = choreographerActionRepository.findByActionName(nextActionName);

        if(nextActionOpt.isPresent()) {
            action.setNextAction(nextActionOpt.get());
        } else {
            throw new InvalidParameterException("One or more of the Actions given doesn't exist! Create ALL Actions before usage.");
        }*/

        ChoreographerAction actionEntry = choreographerActionRepository.save(action);
        for (ChoreographerActionStepRequestDTO actionStep : actionSteps) {
            ChoreographerActionActionStepConnection connection = choreographerActionActionStepConnectionRepository
                    .save(new ChoreographerActionActionStepConnection(createChoreographerActionStepWithUsedService(actionStep.getActionStepName(), new HashSet<>(actionStep.getUsedServiceNames())),
                            actionEntry));
            addNextStepToChoreographerActionStep(actionStep.getActionStepName(), new HashSet<>(actionStep.getNextActionStepNames()));
            actionEntry.getActionActionStepConnections().add(connection);
        }

        choreographerActionActionStepConnectionRepository.flush();

        return choreographerActionRepository.saveAndFlush(actionEntry);
    }

    /* TODO IF THE SHIT FAILS I DID WHOLE DAY @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerActionPlan createChoreographerActionPlan(final String actionPlanName, final List<ChoreographerActionRequestDTO> actions) {
        logger.debug("createChoreographerActionPlan started...");

        ChoreographerActionPlan actionPlan = new ChoreographerActionPlan();

        actionPlan.setActionPlanName(actionPlanName);
        ChoreographerActionPlan choreographerActionPlanEntry = choreographerActionPlanRepository.save(actionPlan);

        for (ChoreographerActionRequestDTO actionRequest : actions) {
            ChoreographerAction choreographerAction = new ChoreographerAction();
            choreographerAction.setActionName(actionRequest.getActionName());
            choreographerActionRepository.saveAndFlush(choreographerAction);
        }

        for (ChoreographerActionRequestDTO actionRequest : actions) {
            Optional<ChoreographerAction> choreographerActionOptional = choreographerActionRepository.findByActionName(actionRequest.getActionName());
            if(choreographerActionOptional.isPresent()) {
                choreographerActionOptional.get().setNextAction(choreographerActionRepository.findByActionName(actionRequest.getNextActionName()));
            }
        }


        return choreographerActionPlanEntry;
    } */

    /*@Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStepResponseDTO createChoreographerActionStepResponse(final String stepName, final Set<String> usedServiceNames, final Set<String> nextActionSteps)  {
        logger.debug("createChoreographerActionStepResponse started...");

        return DTOConverter.convertChoreographerActionStepToChoreographerActionStepResponseDTO(createChoreographerActionStep(stepName, usedServiceNames, nextActionSteps));
    }*/

    /*
    public ChoreographerActionStep getChoreographerActionStepEntryById(final long id) {
        logger.debug("getChoreographerActionStepEntryById started...");

        try {
            Optional<ChoreographerActionStep> actionStepOpt = choreographerActionStepRepository.findById(id);
            if (actionStepOpt.isPresent()) {
                return actionStepOpt.get();
            } else {
                throw new InvalidParameterException("ChoreographerActionStep with ID of " + id + " doesn't exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public ChoreographerActionStepResponseDTO getChoreographerActionStepEntryByIdResponse(final long id) {
        logger.debug("getChoreographerActionStepEntryByIdResponse started...");

        ChoreographerActionStep choreographerActionStepEntry = getChoreographerActionStepEntryById(id);
        return DTOConverter.convertChoreographerActionStepToChoreographerActionStepResponseDTO(choreographerActionStepEntry);
    } */
}
