package eu.arrowhead.common.database.entity.mscv;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.dto.shared.mscv.Layer;
import org.springframework.util.Assert;

@Entity
@Table(name = "mscv_mip_verification_list",
        uniqueConstraints = @UniqueConstraint(name = "u_verification_list_name_layer", columnNames = {"name", "layer"}))
public class VerificationEntryList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private Long verificationInterval;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private Layer layer;

    @OneToMany(mappedBy = "verificationList", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<VerificationEntry> entries = new HashSet<>();

    public VerificationEntryList() {
        super();
    }

    public VerificationEntryList(final String name, final String description,
                                 final Long verificationInterval,
                                 final VerificationEntry... entries) {
        this.name = name;
        this.description = description;
        this.verificationInterval = verificationInterval;
        this.entries = new HashSet<>(Arrays.asList(entries));
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

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(final Layer layer) {
        this.layer = layer;
    }

    public void addEntry(final VerificationEntry entry) {
        Assert.notNull(entry,"Verification entry must not be null");
        if(Objects.isNull(entry.getVerificationList())) {
            entry.setVerificationList(this);
        }
        entries.add(entry);
    }

    public void removeEntry(final VerificationEntry entry) {
        Assert.notNull(entry,"Verification entry must not be null");
        entries.remove(entry);
        entry.setVerificationList(null);
    }

    public Set<VerificationEntry> getEntries() {
        return Collections.unmodifiableSet(entries);
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
          .add("description='" + description + "'")
          .add("verificationInterval='" + verificationInterval + "'")
          .add("layer='" + layer + "'");
        if (Objects.nonNull(entries)) { sj.add("entries=" + entries.size()); }
        return sj.toString();
    }
}
