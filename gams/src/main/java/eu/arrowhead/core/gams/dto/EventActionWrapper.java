package eu.arrowhead.core.gams.dto;

import java.time.ZonedDateTime;

import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.EventAction;
import eu.arrowhead.common.database.entity.StringSensorData;
import eu.arrowhead.core.gams.service.EventService;

public class EventActionWrapper extends AbstractActionWrapper {

    private final EventAction action;

    public EventActionWrapper(final EventService eventService, final Event source, final EventAction action) {
        super(eventService, source);
        this.action = action;
    }

    @Override
    protected void innerRun() {
        final AbstractSensorData data = new StringSensorData(action.getTarget(), ZonedDateTime.now(), sourceEvent.getData());
        switch (action.getPhase()) {
            case MONITOR:
                eventService.createMonitorEvent(action.getTarget(), data);
                break;
            case ANALYZE:
                eventService.createAnalyseEvent(action.getTarget(), data);
                break;
            case PLAN:
                eventService.createPlanEvent(action.getTarget(), data);
                break;
            case EXECUTE:
                eventService.createExecuteEvent(action.getTarget(), data);
                break;
        }
    }
}
