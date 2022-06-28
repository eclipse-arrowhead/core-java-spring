package eu.arrowhead.core.gams;

import java.time.ZonedDateTime;

import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.TimeoutGuard;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import org.springframework.util.Assert;

public class DataValidation {
    public static final String NOT_NULL = " must not be null";
    public static final String NOT_EMPTY = " must not be empty";

    public void verify(final GamsInstance instance) {
        notNull(instance, "Gams Instance");
        notNull(instance.getName(), "Gams Instance Name");
        notNull(instance.getUid(), "Gams Instance UID");
    }

    public void verify(final Sensor sensor) {
        notNull(sensor, "Sensor");
        notNull(sensor.getName(), "Sensor Name");
        notNull(sensor.getUid(), "Sensor UID");
        notNull(sensor.getType(), "Sensor Type");
    }

    public void verify(final ZonedDateTime timestamp) {
        notNull(timestamp, "Timestamp");
    }

    public void verify(final Event event) {
        notNull(event, "Event");
        verify(event.getSensor());
        notNull(event.getCreatedAt(), "Event Creation Time");
        notNull(event.getValidFrom(), "Event Validity");
        notNull(event.getValidTill(), "Event Validity");
        notNull(event.getPhase(), "Event Type");
        notNull(event.getState(), "Event State");
    }

    public void verify(final AbstractSensorData<?> data) {
        notNull(data, "Data");
        verify(data.getSensor());
        notNull(data.getState(), "Data State");
        notNull(data.getData(), "Raw Data");
    }

    public void verify(final CreateInstanceRequest instanceRequest) {
        notNull(instanceRequest, "CreateInstanceRequest");
        notNull(instanceRequest.getName(), "Instance");
        notNull(instanceRequest.getDelayInSeconds(), "Instance delay");
    }

    public void verify(final TimeoutGuard analysis) {
        notNull(analysis, "TimeoutAnalysis");
        verify(analysis.getSensor());
        notNull(analysis.getTimeUnit(),"TimeUnit");
        notNull(analysis.getTimeValue(),"TimeValue");
        Assert.isTrue(analysis.getTimeValue() > 0,"TimeValue must be positive");
    }

    private <T> void notNull(final T object, final String objectMessage) {
        Assert.notNull(object, objectMessage + NOT_NULL);
        if (object instanceof String) {
            Assert.hasText((String) object, objectMessage + NOT_EMPTY);
        }
    }
}
