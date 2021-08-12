package eu.arrowhead.common.database.entity.mscv;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "mscv_standard")
public class Standard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String identification;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, length = 100)
    private String referenceUri;

    @OneToMany(mappedBy = "standard", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Mip> mips = new HashSet<>();

    public Standard() { super(); }

    public Standard(final String identification, final String name, final String referenceUri) {
        this.identification = identification;
        this.name = name;
        this.referenceUri = referenceUri;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(final String identification) {
        this.identification = identification;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getReferenceUri() {
        return referenceUri;
    }

    public void setReferenceUri(final String referenceUri) {
        this.referenceUri = referenceUri;
    }

    public Set<Mip> getMips() {
        return mips;
    }

    public void setMips(final Set<Mip> mips) {
        this.mips = mips;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Standard standard = (Standard) o;
        return Objects.equals(id, standard.id) &&
                Objects.equals(identification, standard.identification) &&
                Objects.equals(referenceUri, standard.referenceUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, referenceUri);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Standard.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("identification='" + identification + "'")
                .add("name='" + name + "'")
                .add("referenceUri='" + referenceUri + "'")
                .toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
