package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.StringJoiner;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "request")
public class CreateSensorRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = true, value = "Name of the sensor", dataType = "string", example = "MyTemperatureSensor")
    private String name;
    @ApiModelProperty(required = false, value = "IPv4 address of the sensor", dataType = "string")
    private String address;
    @ApiModelProperty(required = true, value = "SensorType", dataType = "SensorType", example = "FLOATING_POINT_NUMBER")
    private SensorType type;
    @ApiModelProperty(required = true, value = "Validity length of sensor data", dataType = "long", example = "24")
    private Long retentionTime;
    @ApiModelProperty(required = true, value = "Validity length time unit of sensor data", dataType = "ChronoUnit", example = "HOURS")
    private ChronoUnit timeUnit;

    public CreateSensorRequest() {
        super();
    }

    public CreateSensorRequest(final String name, final String address, final SensorType type, final Long retentionTime, final ChronoUnit timeUnit) {
        this.name = name;
        this.address = address;
        this.type = type;
        this.retentionTime = retentionTime;
        this.timeUnit = timeUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public void setType(final SensorType type) {
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
        final CreateSensorRequest that = (CreateSensorRequest) o;
        return Objects.equals(name, that.name) &&
                type == that.type &&
                retentionTime.equals(that.retentionTime) &&
                timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, retentionTime, timeUnit);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateSensorRequest.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("type=" + type)
                .add("retentionTime=" + retentionTime)
                .add("timeUnit=" + timeUnit)
                .toString();
    }
}
