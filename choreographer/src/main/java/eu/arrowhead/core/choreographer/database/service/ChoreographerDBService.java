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
import org.springframework.web.servlet.tags.form.OptionsTag;

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

    @Autowired
    private ChoreographerActionPlanActionConnectionRepository choreographerActionPlanActionConnectionRepository;

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
            } /*else {
                throw new InvalidParameterException("Action Step with name of " + nextActionStepName + " doesn't exist!");
            }*/
        }

        for(ChoreographerActionStep actionStep : nextActionSteps) {
            ChoreographerNextActionStep nextActionStep = choreographerNextActionStepRepository.save(new ChoreographerNextActionStep(stepEntry, actionStep));
            stepEntry.getNextActionSteps().add(nextActionStep);
        }
        choreographerNextActionStepRepository.flush();

        return choreographerActionStepRepository.saveAndFlush(stepEntry);
    }

    @Transactional (rollbackFor = ArrowheadException.class)
    public ChoreographerAction createChoreographerAction(final String actionName, final List<ChoreographerActionStepRequestDTO> actionSteps) {
        logger.debug("createChoreographerAction started...");

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
            addNextStepToChoreographerActionStep(actionStep.getActionStepName(), new HashSet<>(actionStep.getNextActionStepNames()));
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

        logger.debug(choreographerAction.getActionName());

        ChoreographerAction nextAction = new ChoreographerAction();
        Optional<ChoreographerAction> nextActionOpt = choreographerActionRepository.findByActionName(nextActionName);
        if(nextActionOpt.isPresent()) {
            nextAction = nextActionOpt.get();
            choreographerAction.setNextAction(nextAction);
        } /*else {
            throw new InvalidParameterException("Action with given Action Name of " + nextActionName + "doesn't exist!");
        }*/
        
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
}
