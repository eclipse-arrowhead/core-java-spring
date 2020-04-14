package eu.arrowhead.common.database.entity.mscv;

import eu.arrowhead.common.dto.shared.mscv.VerificationRunDetailResult;

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
@Table(name = "mscv_verification_detail",
        uniqueConstraints = @UniqueConstraint(name = "u_detail_execution_mip", columnNames = {"executionId", "verificationEntryId"}))
public class VerificationExecutionDetail {

    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "executionId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_execution", value = ConstraintMode.CONSTRAINT))
    private VerificationExecution execution;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verificationEntryId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_verification_entry", value = ConstraintMode.CONSTRAINT))
    private VerificationEntry verificationEntry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "scriptId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_script", value = ConstraintMode.CONSTRAINT))
    private Script script;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private VerificationRunDetailResult result;

    @Column(length = 255)
    private String details;

    public VerificationExecutionDetail() {
        super();
    }

    public VerificationExecutionDetail(final VerificationExecution execution,
                                       final VerificationEntry verificationEntry,
                                       final Script script,
                                       final VerificationRunDetailResult result,
                                       final String details) {
        this.execution = execution;
        this.verificationEntry = verificationEntry;
        this.script = script;
        this.result = result;
        this.details = details;
    }

    public VerificationExecutionDetail(final Long id, final VerificationExecution execution,
                                       final VerificationEntry verificationEntry,
                                       final Script script,
                                       final VerificationRunDetailResult result,
                                       final String details) {
        this.id = id;
        this.execution = execution;
        this.verificationEntry = verificationEntry;
        this.script = script;
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

    public VerificationEntry getVerificationEntry() {
        return verificationEntry;
    }

    public void setVerificationEntry(final VerificationEntry verificationEntry) {
        this.verificationEntry = verificationEntry;
    }

    public VerificationRunDetailResult getResult() {
        return result;
    }

    public void setResult(final VerificationRunDetailResult result) {
        this.result = result;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(final Script script) {
        this.script = script;
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
                Objects.equals(verificationEntry, that.verificationEntry) &&
                Objects.equals(script, that.script) &&
                result == that.result &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, execution, verificationEntry, script, result, details);
    }

    @Override
    public String toString() {
        final var sj = new StringJoiner(", ", VerificationExecutionDetail.class.getSimpleName() + "[", "]");

        sj.add("id=" + id);
        if (Objects.nonNull(execution)) { sj.add("execution=" + execution.getExecutionDate()); }
        sj.add("verificationEntry=" + verificationEntry)
          .add("script=" + script)
          .add("result=" + result)
          .add("details=" + details);

        return sj.toString();
    }
}
