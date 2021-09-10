package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @Column(nullable = false, updatable = false)
    protected ZonedDateTime createdAt;

    public AbstractEntity() {
        super();
    }

    public AbstractEntity(final ZonedDateTime createdAt) {
        this.createdAt = createdAt.withNano(0);
    }

    @PrePersist
    public void onCreate() {
        if(Objects.isNull(createdAt)) {
            this.createdAt = ZonedDateTime.now().withNano(0);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final ZonedDateTime createdAt) {
        this.createdAt = createdAt.withNano(0);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof AbstractEntity)) { return false; }
        final AbstractEntity that = (AbstractEntity) o;
        return id == that.id &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .toString();
    }

}
