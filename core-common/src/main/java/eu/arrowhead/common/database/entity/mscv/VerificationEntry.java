package eu.arrowhead.common.database.entity.mscv;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "mscv_mip_verification_entry",
        uniqueConstraints = @UniqueConstraint(name = "u_entry_mip_list", columnNames = {"mipId", "verificationListId"}))
public class VerificationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Short weight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mipId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_entry_mip", value = ConstraintMode.CONSTRAINT))
    private Mip mip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verificationListId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_entry_verification_list", value = ConstraintMode.CONSTRAINT))
    private VerificationEntryList verificationList;


    public VerificationEntry() {
        super();
    }

    public VerificationEntry(final Mip mip, final Short weight, final VerificationEntryList entryList) {
        this.mip = mip;
        this.weight = weight;
    }

    public VerificationEntry(final Long id, final Mip mip, final Short weight, final VerificationEntryList entryList) {
        this.id = id;
        this.mip = mip;
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Mip getMip() {
        return mip;
    }

    public void setMip(final Mip mip) {
        this.mip = mip;
    }

    public Short getWeight() {
        return weight;
    }

    public void setWeight(final Short weight) {
        this.weight = weight;
    }

    public VerificationEntryList getVerificationList() {
        return verificationList;
    }

    public void setVerificationList(final VerificationEntryList verificationList) {
        this.verificationList = verificationList;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationEntry that = (VerificationEntry) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(mip, that.mip) &&
                Objects.equals(weight, that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mip, weight);
    }

    @Override
    public String toString() {
        final var sj = new StringJoiner(", ", VerificationEntry.class.getSimpleName() + "[", "]");

        sj.add("id=" + id);
        if (Objects.nonNull(mip)) { sj.add("mip=" + mip.getName()); }
        sj.add("weight=" + weight);

        return sj.toString();
    }
}
