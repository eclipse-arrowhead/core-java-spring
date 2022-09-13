package eu.arrowhead.common.database.entity.mscv;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/*
Measurable Indicator Points are extracted from existing standards and guidelines to address requirements.
The MIPs are executed through target operating system specific Scripts.
The results of all executions determine the compliance of a device/system/service to a specific standard.

See also: "Security Safety and Organizational Standard Compliance in Cyber Physical Systems" from Ani Bicaku et. al.
 */
@Entity
@Table(name = "mscv_mip",
        uniqueConstraints = @UniqueConstraint(name = "u_mip_category_id",
                columnNames = {"extId", "categoryId"}))
public class Mip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 11)
    private Integer extId;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "standardId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_standard", value = ConstraintMode.CONSTRAINT))
    private Standard standard;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domainId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_domain", value = ConstraintMode.CONSTRAINT))
    private MipDomain domain;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_category", value = ConstraintMode.CONSTRAINT))
    private MipCategory category;

    public Mip() {
        super();
    }

    public Mip(final String name,
               final Integer extId,
               final String description,
               final MipDomain domain,
               final MipCategory category,
               final Standard standard) {
        this.name = name;
        this.extId = extId;
        this.description = description;
        this.domain = domain;
        this.category = category;
        this.standard = standard;
    }

    public String getIdentifier() {
        if (Objects.nonNull(category)) { return category.getAbbreviation() + "-" + extId; } else { return name; }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getExtId() {
        return extId;
    }

    public void setExtId(final Integer extId) {
        this.extId = extId;
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

    public MipDomain getDomain() {
        return domain;
    }

    public void setDomain(final MipDomain domain) {
        this.domain = domain;
    }

    public MipCategory getCategory() {
        return category;
    }

    public void setCategory(final MipCategory category) {
        this.category = category;
    }

    public Standard getStandard() {
        return standard;
    }

    public void setStandard(final Standard standard) {
        this.standard = standard;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Mip that = (Mip) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(extId, that.extId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(category, that.category) &&
                Objects.equals(standard, that.standard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, domain, category, standard);
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", Mip.class.getSimpleName() + "[", "]");
        sj.add("id=" + id);
        sj.add("extId=" + extId);
        sj.add("name=" + name);
        if (Objects.nonNull(domain)) { sj.add("domain=" + domain.getName()); }
        if (Objects.nonNull(category)) { sj.add("category=" + category.getAbbreviation()); }
        if (Objects.nonNull(standard)) { sj.add("standard=" + standard.getName()); }
        return sj.toString();
    }
}
