package eu.arrowhead.core.gams.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.AbstractAction;
import eu.arrowhead.common.database.entity.ActionPlan;
import eu.arrowhead.common.database.entity.CompositeAction;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.EventAction;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.HttpUrlApiCall;
import eu.arrowhead.common.database.entity.LoggingAction;
import eu.arrowhead.common.database.repository.ActionPlanRepository;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.dto.AbstractActionWrapper;
import eu.arrowhead.core.gams.dto.DependentActionWrapper;
import eu.arrowhead.core.gams.dto.EventActionWrapper;
import eu.arrowhead.core.gams.dto.HttpCallWrapper;
import eu.arrowhead.core.gams.dto.IndependentActionWrapper;
import eu.arrowhead.core.gams.dto.LoggingActionWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionAssemblyService {

    private final Logger logger = LogManager.getLogger();

    private final HttpService httpService;
    private final EventService eventService;
    private final ActionPlanRepository actionPlanRepository;
    private final KnowledgeService knowledgeService;
    private final DataValidation validation;

    @Autowired
    public ActionAssemblyService(final HttpService httpService, final EventService eventService,
                                 final ActionPlanRepository actionPlanRepository,
                                 final KnowledgeService knowledgeService) {
        super();
        this.httpService = httpService;
        this.eventService = eventService;
        this.actionPlanRepository = actionPlanRepository;
        this.knowledgeService = knowledgeService;
        this.validation = new DataValidation();
    }

    @Transactional
    public Runnable assembleActionPlan(final GamsInstance instance, final Event source) {
        validation.verify(instance);
        validation.verify(source);

            final Optional<ActionPlan> optionalActionPlan = actionPlanRepository.findByInstanceAndName(instance, source.getData());
            final ActionPlan actionPlan = optionalActionPlan
                    .orElseThrow(() -> new DataNotFoundException("No suitable action plan available"));

            logger.info(source.getMarker(), "Assembling action plan {} for event {}", actionPlan::shortToString, source::shortToString);

            return assembleRunnable(source, actionPlan.getAction());
    }

    protected AbstractActionWrapper assembleRunnable(final Event source,
                                                     final AbstractAction action) {

        final AbstractActionWrapper wrapper;
        logger.debug(source.getMarker(), "Creating Action {} for {}", action::shortToString, source::shortToString);


        switch (action.getActionType()) {
            case API_BODY_CALL:
            case API_URL_CALL:
                wrapper = new HttpCallWrapper(eventService, source, knowledgeService, (HttpUrlApiCall) action, httpService);
                break;
            case COMPOSITE:
                wrapper = assembleCompositeWrapper((CompositeAction) action, source);
                break;
            case LOGGING:
                wrapper = new LoggingActionWrapper(eventService, source, (LoggingAction) action);
                break;
            case EVENT:
                wrapper = new EventActionWrapper(eventService, source, knowledgeService, (EventAction) action);
                break;
            default:
                throw new UnsupportedOperationException("ActionType not supported: " + action.getActionType());
        }

        return wrapper;
    }

    private AbstractActionWrapper assembleCompositeWrapper(final CompositeAction action, final Event source) {

        final List<AbstractActionWrapper> wrappers = new ArrayList<>();
        final AbstractActionWrapper wrapper;

        for (AbstractAction abstractAction : action.getActions()) {
            wrappers.add(assembleRunnable(source, abstractAction));
        }

        switch (action.getCompositeType()) {
            case DEPENDENT:
                wrapper = new DependentActionWrapper(eventService, source, action, wrappers);
                break;
            case INDEPENDENT:
                wrapper = new IndependentActionWrapper(eventService, source, action, wrappers);
                break;
            default:
                throw new UnsupportedOperationException("CompositeActionType not supported: " + action.getCompositeType());
        }

        return wrapper;
    }
}
