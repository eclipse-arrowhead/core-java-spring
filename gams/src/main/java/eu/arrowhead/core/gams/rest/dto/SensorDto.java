package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.StringJoiner;

public class SensorDto implements Serializable {
    private String uid;
    private String address;
    private String name;
    private SensorType type;
    private Long retentionTime;
    private ChronoUnit timeUnit;

    public SensorDto() {
        super();
    }

    public SensorDto(final String uid, final String address, final String name, final SensorType type, final Long retentionTime,
                     final ChronoUnit timeUnit) {
        this.address = address;
        this.name = name;
        this.uid = uid;
        this.type = type;
        this.retentionTime = retentionTime;
        this.timeUnit = timeUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public Long getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(final Long retentionTime) {
        this.retentionTime = retentionTime;
    }

    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(final ChronoUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final SensorDto sensorDto = (SensorDto) o;
        return Objects.equals(uid, sensorDto.uid) &&
                Objects.equals(name, sensorDto.name) &&
                type == sensorDto.type &&
                Objects.equals(retentionTime, sensorDto.retentionTime) &&
                timeUnit == sensorDto.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, type, retentionTime, timeUnit);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SensorDto.class.getSimpleName() + "[", "]")
                .add("uid='" + uid + "'")
                .add("name='" + name + "'")
                .add("type=" + type)
                .add("retentionTime=" + retentionTime)
                .add("timeUnit=" + timeUnit)
                .toString();
    }
    
    private static final long serialVersionUID = 1L;    
}
