package eu.arrowhead.core.choreographer.database.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.ChoreographerActionPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerActionStepRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final Logger logger = LogManager.getLogger(ChoreographerDBService.class);

    public ChoreographerActionStep getChoreographerActionStepById(final long id) {
        logger.debug("getChoreographerActionStepById started...");

        try {
            final Optional<ChoreographerActionStep> actionStep = choreographerActionStepRepository.findById(id);
            if(actionStep.isPresent()) {
                return actionStep.get();
            } else {
                throw new InvalidParameterException("Action step with id of '\" + id + \"' does not exist!");
            }
        } catch (final InvalidParameterException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
            throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
        }
    }

    public Set<ServiceDefinition> getAllUsedServicesByActionStepId(final long id) {

        final Optional<ChoreographerActionStep> actionStep = choreographerActionStepRepository.findById(id);
        return actionStep.get().getUsedServices();
    }

    public ChoreographerActionStepResponseDTO getChoreographerActionStepByIdResponse(final long id) {
        logger.debug("getChoreographerActionPlanByIdResponse started...");

        final ChoreographerActionStep choreographerActionPlan = getChoreographerActionStepById(id);
        return DTOConverter.convertChoreographerActionStepToChoreographerActionStepResponseDTO(ChoreographerActionStep);
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
