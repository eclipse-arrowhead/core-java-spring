package eu.arrowhead.common.database.entity.mscv;

import eu.arrowhead.common.dto.shared.mscv.OS;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "mscv_target")
public abstract class Target {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private OS os;

    public Target() {
        super();
    }

    public Target(final String name, final OS os) {
        this.name = name;
        this.os = os;
    }

    public Target(final Long id, final String name, final OS os) {
        this.id = id;
        this.name = name;
        this.os = os;
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

    public OS getOs() {
        return os;
    }

    public void setOs(final OS os) {
        this.os = os;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Target target = (Target) o;
        return Objects.equals(id, target.id) &&
                Objects.equals(name, target.name) &&
                os == target.os;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, os);
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "[", "]");

        sj.add("id=" + id)
          .add("name='" + name + "'")
          .add("os='" + os + "'");

        appendToString(sj);

        return sj.toString();
    }

    protected abstract void appendToString(final StringJoiner sj);
}
