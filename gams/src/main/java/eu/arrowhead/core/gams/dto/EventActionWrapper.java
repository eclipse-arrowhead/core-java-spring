package eu.arrowhead.core.gams.dto;

import java.time.ZonedDateTime;

import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.EventAction;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.StringSensorData;
import eu.arrowhead.core.gams.service.EventService;
import eu.arrowhead.core.gams.service.KnowledgeService;

public class EventActionWrapper extends AbstractActionWrapper {

    private final KnowledgeService knowledgeService;
    private final EventAction action;

    public EventActionWrapper(final EventService eventService, final Event source, final KnowledgeService knowledgeService,
                              final EventAction action) {
        super(eventService, source);
        this.knowledgeService = knowledgeService;
        this.action = action;
    }

    private void createEvent(final AbstractSensorData<?> data) {
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

    @Override
    protected void innerRun() {
        final AbstractSensorData data = new StringSensorData(action.getTarget(), ZonedDateTime.now(), sourceEvent.getData());
        knowledgeService.storeSensorData(data);
        createEvent(data);
    }

    @Override
    protected void innerRunWithResult(final Sensor eventSensor) {
        final AbstractSensorData data = new StringSensorData(eventSensor, ZonedDateTime.now(), sourceEvent.getData());
        knowledgeService.storeSensorData(data);
        createEvent(data);
    }
}
