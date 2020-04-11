package eu.arrowhead.core.msvc.database.entities;

import eu.arrowhead.core.msvc.database.VerificationRunDetailResult;

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
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "msvc_verification_detail",
        uniqueConstraints = @UniqueConstraint(name = "u_detail_execution_mip", columnNames = {"executionId", "mipId"}))
public class VerificationExecutionDetail {

    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "executionId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_execution", value = ConstraintMode.CONSTRAINT))
    private VerificationExecution execution;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mipId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_mip", value = ConstraintMode.CONSTRAINT))
    private Mip mip;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private VerificationRunDetailResult result;

    @Column
    private String details;

    public VerificationExecutionDetail() {
        super();
    }

    public VerificationExecutionDetail(final VerificationExecution execution, final Mip mip,
                                       final VerificationRunDetailResult result, final String details) {
        this.execution = execution;
        this.mip = mip;
        this.result = result;
        this.details = details;
    }

    public VerificationExecutionDetail(final Long id, final VerificationExecution execution, final Mip mip,
                                       final VerificationRunDetailResult result, final String details) {
        this.id = id;
        this.execution = execution;
        this.mip = mip;
        this.result = result;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public VerificationExecution getExecution() {
        return execution;
    }

    public void setExecution(final VerificationExecution execution) {
        this.execution = execution;
    }

    public Mip getMip() {
        return mip;
    }

    public void setMip(final Mip mip) {
        this.mip = mip;
    }

    public VerificationRunDetailResult getResult() {
        return result;
    }

    public void setResult(final VerificationRunDetailResult result) {
        this.result = result;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationExecutionDetail that = (VerificationExecutionDetail) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(execution, that.execution) &&
                Objects.equals(mip, that.mip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, execution, mip);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VerificationExecutionDetail.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("verificationRun=" + execution)
                .add("indicatorPoint=" + mip)
                .add("result=" + result)
                .toString();
    }
}
