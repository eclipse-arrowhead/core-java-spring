package eu.arrowhead.core.gams.dto;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.LoggingAction;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.core.gams.service.EventService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

public class LoggingActionWrapper extends AbstractActionWrapper {

    protected final LoggingAction sourceAction;
    private final Logger logger = LogManager.getLogger();

    public LoggingActionWrapper(final EventService eventService,
                                final Event sourceEvent,
                                final LoggingAction sourceAction) {
        super(eventService, sourceEvent);
        this.sourceAction = sourceAction;
    }

    public LoggingAction getSourceAction() {
        return sourceAction;
    }

    @Override
    protected void innerRun() {
        final Marker marker = sourceAction.createMarker();
        final Sensor sensor = sourceEvent.getSensor();
        final GamsInstance instance = sensor.getInstance();
        logger.log(Level.OFF, marker, "Instance: {}, Sensor: {}, Source: {}, Data: {}", instance.getName(), sensor.getName(),
                   sourceEvent.getSource(), sourceEvent.getData());
    }

    @Override
    protected void innerRunWithResult(final Sensor eventSensor) {
        innerRun();
    }
}
