package eu.arrowhead.common.database.entity;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "gams_instance")
public class GamsInstance extends ConfigurationEntity {

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "name", "owner", "email"); //NOSONAR

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @Column(nullable = false, unique = false)
    private Long delay = 0L;

    @Column(nullable = false, unique = false)
    @Enumerated(EnumType.STRING)
    private ChronoUnit delayTimeUnit = ChronoUnit.SECONDS;

    @Column(nullable = true, unique = false, length = 32)
    private String owner;

    @Column(nullable = true, unique = false, length = 32)
    private String email;

    public GamsInstance() {}

    public GamsInstance(final String name) {
        this.name = name;
    }

    public GamsInstance(final String name, final UUID uid, final Long delay) {
        this.name = name;
        this.uid = uid;
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUidAsString() {
        return (Objects.nonNull(uid) ? uid.toString() : null);
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(final Long delay) {
        this.delay = delay;
    }

    public ChronoUnit getDelayTimeUnit() {
        return delayTimeUnit;
    }

    public void setDelayTimeUnit(final ChronoUnit delayTimeUnit) {
        this.delayTimeUnit = delayTimeUnit;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String shortToString() {
        return new StringJoiner(", ", GamsInstance.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GamsInstance.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("uid=" + uid)
                .add("delay=" + delay)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .toString();
    }
}