package eu.arrowhead.common.dto.shared.mscv;


public class MscvVerificationExecution {

  private long id;
  private java.sql.Timestamp executionDate;
  private String result;
  private long verificationListId;
  private long verificationTargetId;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public java.sql.Timestamp getExecutionDate() {
    return executionDate;
  }

  public void setExecutionDate(java.sql.Timestamp executionDate) {
    this.executionDate = executionDate;
  }


  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }


  public long getVerificationListId() {
    return verificationListId;
  }

  public void setVerificationListId(long verificationListId) {
    this.verificationListId = verificationListId;
  }


  public long getVerificationTargetId() {
    return verificationTargetId;
  }

  public void setVerificationTargetId(long verificationTargetId) {
    this.verificationTargetId = verificationTargetId;
  }

}
