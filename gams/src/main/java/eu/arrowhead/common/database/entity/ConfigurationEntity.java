package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public abstract class ConfigurationEntity extends AbstractEntity {

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    protected UUID uid;

    @Column(name = "updated_at", nullable = false, updatable = true)
    protected ZonedDateTime updatedAt;

    public ConfigurationEntity() { super(); }

    @PrePersist
    public void onCreate() {
        super.onCreate();
        if (Objects.isNull(uid)) { uid = UUID.randomUUID(); }
        if (Objects.isNull(updatedAt)) { this.updatedAt = this.createdAt; }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(final UUID uid) {
        this.uid = uid;
    }

    public String getUidString() {
        if (Objects.isNull(uid)) { uid = UUID.randomUUID(); }
        return uid.toString();
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ConfigurationEntity)) { return false; }
        final ConfigurationEntity that = (ConfigurationEntity) o;
        return id == that.id &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, createdAt);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("uid=" + uid)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .toString();
    }
}
