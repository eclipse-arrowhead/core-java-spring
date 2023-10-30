package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class PublishSensorDataRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String timestamp;
    private Object data;

    public PublishSensorDataRequest() { super(); }

    public PublishSensorDataRequest(final String timestamp, final Object data) {
        this.timestamp = timestamp;
        this.data = data;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final PublishSensorDataRequest that = (PublishSensorDataRequest) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, data);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PublishSensorDataRequest.class.getSimpleName() + "[", "]")
                .add("timestamp='" + timestamp + "'")
                .add("data=" + data)
                .toString();
    }
}
