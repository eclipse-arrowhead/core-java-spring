package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;

public class MscvVerificationExecution implements Serializable {

    private Long id;
    private String executionDate;
    private String result;
    private Long verificationListId;
    private Long verificationTargetId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(final String executionDate) {
        this.executionDate = executionDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public Long getVerificationListId() {
        return verificationListId;
    }

    public void setVerificationListId(final Long verificationListId) {
        this.verificationListId = verificationListId;
    }

    public Long getVerificationTargetId() {
        return verificationTargetId;
    }

    public void setVerificationTargetId(final Long verificationTargetId) {
        this.verificationTargetId = verificationTargetId;
    }
}
