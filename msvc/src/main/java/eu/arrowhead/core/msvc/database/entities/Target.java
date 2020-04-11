package eu.arrowhead.core.msvc.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "msvc_target")
public abstract class Target {

    @Id
    private Long id;

    @Column(nullable = false, length = 32)
    private String name;

    public Target() {
        super();
    }

    public Target(final String name) {
        this.name = name;
    }

    public Target(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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
        if (o == null || getClass() != o.getClass()) { return false; }
        final Target that = (Target) o;
        return id.equals(that.id) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "[", "]");

        sj.add("id=" + id)
          .add("name='" + name + "'");

        appendToString(sj);

        return sj.toString();
    }

    protected abstract void appendToString(final StringJoiner sj);
}
