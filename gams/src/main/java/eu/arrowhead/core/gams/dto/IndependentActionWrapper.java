package eu.arrowhead.core.gams.dto;

import java.util.List;

import eu.arrowhead.common.database.entity.AbstractAction;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.core.gams.service.EventService;

public class IndependentActionWrapper extends CompositeActionWrapper {

    public IndependentActionWrapper(final EventService eventService, final Event sourceEvent, final AbstractAction sourceAction,
                                    final List<AbstractActionWrapper> actions) {
        super(eventService, sourceEvent, sourceAction, actions);
    }

    @Override
    protected void innerRun() {
        for (AbstractActionWrapper runnable : actions) {
            try {
                runnable.run();
            } catch (final Exception ex) {
                eventService.createFailureEvent(this, new ActionExecutionException(runnable, ex));
            }
        }
    }
}
