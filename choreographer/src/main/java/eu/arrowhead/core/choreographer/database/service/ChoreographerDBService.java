package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.database.entity.*;
import eu.arrowhead.common.database.repository.*;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);

    @Transactional(rollbackFor = ArrowheadException.class)
    public ChoreographerActionStep createChoreographerActionStepWithServiceDefinition(final String stepName, final Set<String> usedServiceNames) {
        logger.debug("createChoreographerActionStepWithServiceDefinition started");

        List<ServiceDefinition> usedServices = new ArrayList<>(usedServiceNames.size());
        for (String name : usedServiceNames) {
            Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findByServiceDefinition(name);
            if (serviceOpt.isPresent()) {
                usedServices.add(serviceOpt.get());
            } else {
                logger.debug("Service Definition with name of" + name + "doesn't exist!");
            }
        }

        if (usedServices.size() != usedServiceNames.size()) {
            throw new InvalidParameterException("One or more of the services given doesn't exist! Create ALL services before usage.");
        }

        ChoreographerActionStep step = new ChoreographerActionStep(stepName);
        ChoreographerActionStep stepEntry = choreographerActionStepRepository.save(step);
        for (ServiceDefinition serviceDefinition : usedServices) {
            ChoreographerActionStepServiceDefinitionConnection connection =
                    choreographerActionStepServiceDefinitionConnectionRepository.save(new ChoreographerActionStepServiceDefinitionConnection(stepEntry, serviceDefinition));
            stepEntry.getActionStepServiceDefinitionConnections().add(connection);
        }
        choreographerActionStepServiceDefinitionConnectionRepository.flush();

        ChoreographerActionStep savedStepEntry = choreographerActionStepRepository.save(stepEntry);
        choreographerActionStepRepository.flush();
        return savedStepEntry;
    }

    public ChoreographerActionStepResponseDTO createChoreographerActionStepWithServiceDefinitionResponse(final String stepName, final Set<String> usedServiceNames)  {
        logger.debug("createChoreographerActionStepWithServiceDefinitionResponse started...");

        return DTOConverter.convertChoreographerActionStepToChoreographerActionStepResponseDTO(createChoreographerActionStepWithServiceDefinition(stepName, usedServiceNames));
    }

    /* public ChoreographerActionPlan getChoreographerActionPlanById(final long id) {
        logger.debug("getChoreographerActionPlanById started...");

        try {
            final Optional<ChoreographerActionPlan> actionPlan = choreographerActionPlanRepository.findById(id);
            if (actionPlan.isPresent()) {
                return actionPlan.get();
            } else {
                throw new InvalidParameterException("Action Plan with id of '" + id + "' does not exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public ChoreographerActionPlanResponseDTO getChoreographerActionPlanByIdResponse(final long id) {
        logger.debug("getChoreographerActionPlanByIdResponse started...");

        final ChoreographerActionPlan choreographerActionPlan = getChoreographerActionPlanById(id);
        return DTOConverter.convertChoreographerActionPlanToChoreographerActionPlanResponseDTO(choreographerActionPlan);
    } */

}
