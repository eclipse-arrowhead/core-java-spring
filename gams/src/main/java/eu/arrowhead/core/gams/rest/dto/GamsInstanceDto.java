package eu.arrowhead.core.gams.rest.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class GamsInstanceDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String uid;
    private final String creationTime;
    private final Long delayEvents;
    private final String owner;

    public GamsInstanceDto(final String name, final String uid, final String creationTime, final Long delayEvents, final String owner) {
        this.name = name;
        this.uid = uid;
        this.creationTime = creationTime;
        this.delayEvents = delayEvents;
        this.owner = owner;
    }

    public String getName() {return this.name;}

    public String getUid() {return this.uid;}

    public String getCreationTime() {return this.creationTime;}

    public String getOwner() {
        return owner;
    }
    public Long getDelayEvents() {
        return delayEvents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final GamsInstanceDto that = (GamsInstanceDto) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(delayEvents, that.delayEvents) &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, delayEvents, uid, owner, creationTime);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GamsInstanceDto.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("uid='" + uid + "'")
                .add("owner='" + owner + "'")
                .add("delay='" + delayEvents + "'")
                .add("creationTime='" + creationTime + "'")
                .toString();
    }

}
