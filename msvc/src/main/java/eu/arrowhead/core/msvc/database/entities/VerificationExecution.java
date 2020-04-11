package eu.arrowhead.core.msvc.database.entities;

import eu.arrowhead.core.msvc.database.VerificationRunResult;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "msvc_verification_execution",
        uniqueConstraints = @UniqueConstraint(name = "u_verification_list_target_date",
                columnNames = {"verificationTargetId", "verificationListId", "executionDate"}))
public class VerificationExecution {

    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verificationTargetId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_target", value = ConstraintMode.CONSTRAINT))
    private Target target;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verificationListId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_verification_list", value = ConstraintMode.CONSTRAINT))
    private VerificationEntryList verificationList;

    @Column(nullable = false)
    private ZonedDateTime executionDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationRunResult result;

    public VerificationExecution() {
        super();
    }

    public VerificationExecution(final Target target,
                                 final VerificationEntryList verificationList,
                                 final ZonedDateTime executionDate,
                                 final VerificationRunResult result) {
        this.target = target;
        this.verificationList = verificationList;
        this.executionDate = executionDate;
        this.result = result;
    }

    public VerificationExecution(final Long id,
                                 final Target target,
                                 final VerificationEntryList verificationList,
                                 final ZonedDateTime executionDate,
                                 final VerificationRunResult result) {
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

    public VerificationRunResult getResult() {
        return result;
    }

    public void setResult(final VerificationRunResult result) {
        this.result = result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationExecution that = (VerificationExecution) o;
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
        final var sj = new StringJoiner(", ", VerificationExecution.class.getSimpleName() + "[", "]");
        sj.add("id=" + id);
        if (Objects.nonNull(target)) { sj.add("target=" + target.getName()); }
        if (Objects.nonNull(verificationList)) { sj.add("verificationList=" + verificationList.getName()); }
        sj.add("executionDate='" + executionDate + "'");
        sj.add("result=" + result);
        return sj.toString();
    }
}
