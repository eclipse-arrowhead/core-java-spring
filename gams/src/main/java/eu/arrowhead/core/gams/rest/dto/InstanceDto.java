package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class InstanceDto implements Serializable {

    private final String name;
    private final String uid;
    private final String creationTime;

    public InstanceDto(final String name, final String uid, final String creationTime) {
        this.name = name;
        this.uid = uid;
        this.creationTime = creationTime;
    }

    public String getName() {return this.name;}

    public String getUid() {return this.uid;}

    public String getCreationTime() {return this.creationTime;}

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final InstanceDto that = (InstanceDto) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uid, creationTime);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InstanceDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("uid='" + uid + "'")
                .add("creationTime='" + creationTime + "'")
                .toString();
    }
}
