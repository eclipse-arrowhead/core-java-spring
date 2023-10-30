package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class SensorDataDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uid;
    private String address;
    private String timestamp;
    private Object data;

    public SensorDataDto() { super(); }

    public SensorDataDto(final String uid, final String address, final String timestamp, final Object data) {
        this.uid = uid;
        this.address = address;
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
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
        final SensorDataDto that = (SensorDataDto) o;
        return Objects.equals(uid, that.uid) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, timestamp, data);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SensorDataDto.class.getSimpleName() + "[", "]")
                .add("uid='" + uid + "'")
                .add("timestamp='" + timestamp + "'")
                .add("data=" + data)
                .toString();
    }
}
