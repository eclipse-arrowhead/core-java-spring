package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import eu.arrowhead.core.gams.dto.ProcessingState;

@MappedSuperclass
public abstract class ProcessableEntity extends AbstractEntity {

    protected static final Long DEFAULT_RETENTION_TIME = 1L;
    protected static final ChronoUnit DEFAULT_RETENTION_TIME_UNIT = ChronoUnit.HOURS;

    @Column(nullable = false, updatable = true, length = 16)
    @Enumerated(EnumType.STRING)
    protected ProcessingState state;

    @Column(nullable = false, updatable = false)
    protected ZonedDateTime validTill;

    public ProcessableEntity() {
        super();
    }

    public ProcessableEntity(final ZonedDateTime createdAt) {
        super(createdAt);
    }

    public ProcessableEntity(final ZonedDateTime createdAt, final ProcessingState state) {
        super(createdAt);
        this.state = state;
    }

    @PrePersist
    public void onCreate() {
        super.onCreate();
        if (Objects.isNull(state)) { this.state = ProcessingState.PERSISTED; }
        if (Objects.isNull(validTill)) { this.validTill = calculateValidityTime(createdAt, DEFAULT_RETENTION_TIME, DEFAULT_RETENTION_TIME_UNIT); }
    }

    public ProcessingState getState() {
        return state;
    }

    public void setState(final ProcessingState state) {
        this.state = state;
    }

    public ZonedDateTime getValidTill() {
        return validTill;
    }

    public void setValidTill(final ZonedDateTime validTill) {
        this.validTill = validTill;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ProcessableEntity)) { return false; }
        final ProcessableEntity that = (ProcessableEntity) o;
        return id == that.id &&
                Objects.equals(createdAt, that.createdAt) &&
                state == that.state &&
                Objects.equals(validTill, that.validTill);
    }

    //-------------------------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, state, validTill);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("state=" + state)
                .add("validTill=" + validTill)
                .toString();
    }

    protected ZonedDateTime calculateValidityTime(final ZonedDateTime startTime, final long delay, final ChronoUnit timeUnit) {
        return startTime.plus(delay, timeUnit);
    }
}
