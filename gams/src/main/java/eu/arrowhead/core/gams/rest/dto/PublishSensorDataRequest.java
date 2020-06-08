package eu.arrowhead.core.gams.rest.dto;

import java.util.StringJoiner;

public class PublishSensorDataRequest extends SensorDataDto {

    public PublishSensorDataRequest() { super(); }

    public PublishSensorDataRequest(final String timestamp, final Object data) {
        super(timestamp, data);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PublishSensorDataRequest.class.getSimpleName() + "[", "]")
                .add("timestamp='" + getTimestamp() + "'")
                .add("data=" + getData())
                .toString();
    }
}
