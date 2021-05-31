package eu.arrowhead.common.database.entity.mscv;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.dto.shared.mscv.SuccessIndicator;

@Entity
@Table(name = "mscv_verification_result",
        uniqueConstraints = @UniqueConstraint(name = "u_verification_list_target_date",
                columnNames = {"verification_target_id", "verification_list_id", "execution_date"}))
public class VerificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verification_target_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_target", value = ConstraintMode.CONSTRAINT))
    private Target target;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verification_list_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_verification_list", value = ConstraintMode.CONSTRAINT))
    private VerificationEntryList verificationList;

    @Column(name = "execution_date", nullable = false)
    private ZonedDateTime executionDate;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private SuccessIndicator result;

    public VerificationResult() {
        super();
    }

    public VerificationResult(final Target target,
                              final VerificationEntryList verificationList,
                              final ZonedDateTime executionDate,
                              final SuccessIndicator result) {
        this.target = target;
        this.verificationList = verificationList;
        this.executionDate = executionDate;
        this.result = result;
    }

    public VerificationResult(final Long id,
                              final Target target,
                              final VerificationEntryList verificationList,
                              final ZonedDateTime executionDate,
                              final SuccessIndicator result) {
        this.id = id;
        this.target = target;
        this.verificationList = verificationList;
        this.executionDate = executionDate;
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(final Target target) {
        this.target = target;
    }

    public VerificationEntryList getVerificationList() {
        return verificationList;
    }

    public void setVerificationList(final VerificationEntryList verificationList) {
        this.verificationList = verificationList;
    }

    public ZonedDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(final ZonedDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public SuccessIndicator getResult() {
        return result;
    }

    public void setResult(final SuccessIndicator result) {
        this.result = result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationResult that = (VerificationResult) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(target, that.target) &&
                Objects.equals(verificationList, that.verificationList) &&
                Objects.equals(executionDate, that.executionDate) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, target, verificationList, executionDate, result);
    }

    @Override
    public String toString() {
        final var sj = new StringJoiner(", ", VerificationResult.class.getSimpleName() + "[", "]");
        sj.add("id=" + id);
        if (Objects.nonNull(target)) { sj.add("target=" + target.getName()); }
        if (Objects.nonNull(verificationList)) { sj.add("verificationList=" + verificationList.getName()); }
        sj.add("executionDate='" + executionDate + "'");
        sj.add("result=" + result);
        return sj.toString();
    }
}
