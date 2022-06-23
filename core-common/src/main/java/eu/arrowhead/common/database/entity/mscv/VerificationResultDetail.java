package eu.arrowhead.common.database.entity.mscv;

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

import eu.arrowhead.common.dto.shared.mscv.DetailSuccessIndicator;

@Entity
@Table(name = "mscv_verification_result_detail",
        uniqueConstraints = @UniqueConstraint(name = "u_detail_execution_mip", columnNames = {"executionId", "verificationEntryId"}))
public class VerificationResultDetail {

    private final static int DETAILS_LENGTH = 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "executionId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_execution", value = ConstraintMode.CONSTRAINT))
    private VerificationResult execution;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verificationEntryId", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detail_verification_entry", value = ConstraintMode.CONSTRAINT))
    private VerificationEntry verificationEntry;

    @ManyToOne(optional = true)
    @JoinColumn(name = "scriptId", referencedColumnName = "id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_detail_script", value = ConstraintMode.CONSTRAINT))
    private Script script;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private DetailSuccessIndicator result;

    @Column(length = DETAILS_LENGTH)
    private String details;

    public VerificationResultDetail() {
        super();
    }

    public VerificationResultDetail(final VerificationResult execution,
                                    final VerificationEntry verificationEntry,
                                    final Script script,
                                    final DetailSuccessIndicator result,
                                    final String details) {
        this.execution = execution;
        this.verificationEntry = verificationEntry;
        this.script = script;
        this.result = result;
        setDetails(details);
    }

    public VerificationResultDetail(final Long id, final VerificationResult execution,
                                    final VerificationEntry verificationEntry,
                                    final Script script,
                                    final DetailSuccessIndicator result,
                                    final String details) {
        this.id = id;
        this.execution = execution;
        this.verificationEntry = verificationEntry;
        this.script = script;
        this.result = result;
        setDetails(details);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public VerificationResult getExecution() {
        return execution;
    }

    public void setExecution(final VerificationResult execution) {
        this.execution = execution;
    }

    public VerificationEntry getVerificationEntry() {
        return verificationEntry;
    }

    public void setVerificationEntry(final VerificationEntry verificationEntry) {
        this.verificationEntry = verificationEntry;
    }

    public DetailSuccessIndicator getResult() {
        return result;
    }

    public void setResult(final DetailSuccessIndicator result) {
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
        if (Objects.nonNull(details) && details.length() > DETAILS_LENGTH) {
            this.details = details.substring(0, DETAILS_LENGTH);
        } else {
            this.details = details;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final VerificationResultDetail that = (VerificationResultDetail) o;
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
        final var sj = new StringJoiner(", ", VerificationResultDetail.class.getSimpleName() + "[", "]");

        sj.add("id=" + id);
        if (Objects.nonNull(execution)) { sj.add("execution=" + execution.getExecutionDate()); }
        sj.add("verificationEntry=" + verificationEntry)
          .add("script=" + script)
          .add("result=" + result)
          .add("details=" + details);

        return sj.toString();
    }
}
