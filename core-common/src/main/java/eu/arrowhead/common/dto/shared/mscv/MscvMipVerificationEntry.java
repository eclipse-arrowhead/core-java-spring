package eu.arrowhead.common.dto.shared.mscv;


public class MscvMipVerificationEntry {

  private long id;
  private long mipId;
  private long weight;
  private long verificationListId;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getMipId() {
    return mipId;
  }

  public void setMipId(long mipId) {
    this.mipId = mipId;
  }


  public long getWeight() {
    return weight;
  }

  public void setWeight(long weight) {
    this.weight = weight;
  }


  public long getVerificationListId() {
    return verificationListId;
  }

  public void setVerificationListId(long verificationListId) {
    this.verificationListId = verificationListId;
  }

}
