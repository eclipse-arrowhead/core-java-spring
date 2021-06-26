package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.ActionType;
import eu.arrowhead.core.gams.dto.EventType;
import eu.arrowhead.core.gams.dto.GamsPhase;

@Entity
@Table(name = "gams_event_action")
public class EventAction extends AbstractAction {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "targetSensorId", referencedColumnName = "id", nullable = false)
    private Sensor target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private GamsPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private EventType eventType;

    public EventAction() {
        super();
    }

    public EventAction(final GamsInstance instance, final String name) {
        super(instance, name, ActionType.EVENT);
    }

    public Sensor getTarget() {
        return target;
    }

    public void setTarget(final Sensor target) {
        this.target = target;
    }

    public GamsPhase getPhase() {
        return phase;
    }

    public void setPhase(final GamsPhase phase) {
        this.phase = phase;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof EventAction)) { return false; }
        if (!super.equals(o)) { return false; }
        final EventAction that = (EventAction) o;
        return Objects.equals(target, that.target) &&
                phase == that.phase &&
                eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), target, phase, eventType);
    }

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("target=" + target.getName())
                .add("phase=" + phase)
                .add("eventType=" + eventType)
                .add("actionType=" + actionType)
                .add("instance=" + instance.getName())
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .add("name='" + name + "'")
                .add("target=" + target)
                .add("phase=" + phase)
                .add("type=" + eventType)
                .add("actionType=" + actionType)
                .add("instance=" + instance)
                .toString();
    }
}
