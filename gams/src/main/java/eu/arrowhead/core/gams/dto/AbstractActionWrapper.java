package eu.arrowhead.core.gams.dto;

import java.util.StringJoiner;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.service.EventService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractActionWrapper implements Runnable {

    protected final EventService eventService;
    protected final Event sourceEvent;
    private final Logger logger = LogManager.getLogger();

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
            logger.info("Executing Action {}", this::shortToString);
            innerRun();
        } catch (final Exception ex) {
            logger.fatal("Unable to perform Action: {}", ex.getClass().getSimpleName(), ex);
            eventService.createFailureEvent(this, ex);
        }
    }

    public void runWithResult(final Sensor eventSensor) {
        try {
            logger.info("Executing Action {}", this::shortToString);
            innerRunWithResult(eventSensor);
        } catch (final Exception ex) {
            logger.fatal("Unable to perform Action: {}", ex.getClass().getSimpleName(), ex);
            eventService.createFailureEvent(this, ex);
        }
    }

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

    protected abstract void innerRun();
    protected abstract void innerRunWithResult(final Sensor eventSensor);
}
