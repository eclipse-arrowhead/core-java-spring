package eu.arrowhead.common.dto.shared.mscv;


public class MscvVerificationDetail {

  private long id;
  private String result;
  private String details;
  private long verificationEntryId;
  private long executionId;
  private long scriptId;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }


  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }


  public long getVerificationEntryId() {
    return verificationEntryId;
  }

  public void setVerificationEntryId(long verificationEntryId) {
    this.verificationEntryId = verificationEntryId;
  }


  public long getExecutionId() {
    return executionId;
  }

  public void setExecutionId(long executionId) {
    this.executionId = executionId;
  }


  public long getScriptId() {
    return scriptId;
  }

  public void setScriptId(long scriptId) {
    this.scriptId = scriptId;
  }

}
