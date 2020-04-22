package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class MscvMipVerificationEntry implements Serializable {

    private Long id;
    private Long mipId;
    private Long weight;
    private Long verificationListId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getMipId() {
        return mipId;
    }

    public void setMipId(final Long mipId) {
        this.mipId = mipId;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(final Long weight) {
        this.weight = weight;
    }

    public Long getVerificationListId() {
        return verificationListId;
    }

    public void setVerificationListId(final Long verificationListId) {
        this.verificationListId = verificationListId;
    }

  @Override
  public String toString() {
    return new StringJoiner(", ", MscvMipVerificationEntry.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("mipId=" + mipId)
            .add("weight=" + weight)
            .add("verificationListId=" + verificationListId)
            .toString();
  }
}
