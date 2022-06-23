package eu.arrowhead.core.gams.dto;

import java.util.List;

import eu.arrowhead.common.database.entity.AbstractAction;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.service.EventService;

public abstract class CompositeActionWrapper extends AbstractActionWrapper {

    protected final AbstractAction sourceAction;
    protected final List<AbstractActionWrapper> actions;

    public CompositeActionWrapper(final EventService eventService, final Event sourceEvent,
                                  final AbstractAction sourceAction, final List<AbstractActionWrapper> actions) {
        super(eventService, sourceEvent);
        this.sourceAction = sourceAction;
        this.actions = actions;
    }

    public AbstractAction getSourceAction() {
        return sourceAction;
    }

    @Override
    protected void innerRunWithResult(final Sensor eventSensor) {
        innerRun();
    }
}
