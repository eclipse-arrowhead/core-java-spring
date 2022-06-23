package eu.arrowhead.core.gams.dto;

import java.util.List;

import eu.arrowhead.common.database.entity.AbstractAction;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.core.gams.service.EventService;

public class DependentActionWrapper extends CompositeActionWrapper {


    public DependentActionWrapper(final EventService eventService, final Event sourceEvent, final AbstractAction sourceAction,
                                  final List<AbstractActionWrapper> actions) {
        super(eventService, sourceEvent, sourceAction, actions);
    }

    @Override
    protected void innerRun() {
        for (AbstractActionWrapper runnable : actions) {
            try {
                runnable.run();
            } catch(final Exception ex) {
                throw new ActionExecutionException(runnable, ex);
            }
        }
    }
}
