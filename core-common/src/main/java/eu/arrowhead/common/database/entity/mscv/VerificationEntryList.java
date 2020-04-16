package eu.arrowhead.common.database.entity.mscv;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

@Entity
@Table(name = "mscv_mip_verification_list")
public class VerificationEntryList {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column
    private String description;

    @Column
    private Long verificationInterval;

    @OneToMany(mappedBy = "verificationList", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<VerificationEntry> entries = new HashSet<>();

    public VerificationEntryList() {
        super();
    }

    public VerificationEntryList(final String name, final String description, final Long verificationInterval, final Set<VerificationEntry> entries) {
        this.name = name;
        this.description = description;
        this.verificationInterval = verificationInterval;
        this.entries = entries;
    }

    public VerificationEntryList(final Long id, final String name, final String description,
                                 final Long verificationInterval, final Set<VerificationEntry> entries) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.verificationInterval = verificationInterval;
        this.entries = entries;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getVerificationInterval() {
        return verificationInterval;
    }

    public void setVerificationInterval(final Long verificationInterval) {
        this.verificationInterval = verificationInterval;
    }

    public Set<VerificationEntry> getEntries() {
        return entries;
    }

    public void setEntries(final Set<VerificationEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationEntryList that = (VerificationEntryList) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        final var sj = new StringJoiner(", ", VerificationEntryList.class.getSimpleName() + "[", "]");
        sj.add("id=" + id)
          .add("name='" + name + "'")
          .add("description='" + description + "'");
        if (Objects.nonNull(entries)) { sj.add("entries=" + entries.size()); }
        sj.add("verificationInterval='" + verificationInterval + "'");
        return sj.toString();
    }
}
