package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NamedEntity extends ConfigurationEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instanceId", referencedColumnName = "id", nullable = false)
    protected GamsInstance instance;

    @Column(nullable = false, unique = true, length = 32)
    protected String name;

    public NamedEntity() { super(); }

    public NamedEntity(final GamsInstance instance, final String name) {
        this.instance = instance;
        this.name = name;
    }

    public GamsInstance getInstance() {
        return instance;
    }

    public void setInstance(final GamsInstance instance) {
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof NamedEntity)) { return false; }
        if (!super.equals(o)) { return false; }
        final NamedEntity that = (NamedEntity) o;
        return Objects.equals(instance, that.instance) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instance, name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("instance=" + instance)
                .add("name='" + name + "'")
                .toString();
    }
}
