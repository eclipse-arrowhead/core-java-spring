package eu.arrowhead.core.gams.dto;

import java.util.StringJoiner;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.core.gams.service.EventService;

public abstract class AbstractActionWrapper implements Runnable {

    protected final EventService eventService;
    protected final Event sourceEvent;

    public AbstractActionWrapper(final EventService eventService, final Event sourceEvent) {
        this.eventService = eventService;
        this.sourceEvent = sourceEvent;
    }

    public Event getSourceEvent() {
        return sourceEvent;
    }


    @Override
    public void run() {
        try {
            innerRun();
        } catch (final Exception ex) {
            eventService.createFailureEvent(this, ex);
        }
    }

    protected abstract void innerRun();

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("sourceEvent=" + sourceEvent.shortToString())
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("sourceEvent=" + sourceEvent)
                .toString();
    }
}
