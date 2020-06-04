package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class CreateSensorRequest implements Serializable {

    private String name;
    private SensorType type;

    public CreateSensorRequest() {
        super();
    }

    public CreateSensorRequest(final String name, final SensorType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public SensorType getType() {
        return type;
    }

    public void setType(final SensorType type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final CreateSensorRequest that = (CreateSensorRequest) o;
        return Objects.equals(name, that.name) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateSensorRequest.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("type=" + type)
                .toString();
    }
}
