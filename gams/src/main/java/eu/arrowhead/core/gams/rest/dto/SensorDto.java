package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class SensorDto implements Serializable {
    private String uid;
    private String name;
    private SensorType type;

    public SensorDto() {
        super();
    }

    public SensorDto(final String uid, final String name, final SensorType type) {
        this.name = name;
        this.uid = uid;
        this.type = type;
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

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorDto sensorDto = (SensorDto) o;
        return Objects.equals(name, sensorDto.name) &&
                Objects.equals(uid, sensorDto.uid) &&
                type == sensorDto.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uid, type);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SensorDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("uid='" + uid + "'")
                .add("type=" + type)
                .toString();
    }
}
