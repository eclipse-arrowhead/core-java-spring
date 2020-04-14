package eu.arrowhead.common.dto.shared.mscv;


public class MscvStandard {

  private long id;
  private String identification;
  private String name;
  private String description;
  private String referenceUri;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getIdentification() {
    return identification;
  }

  public void setIdentification(String identification) {
    this.identification = identification;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public String getReferenceUri() {
    return referenceUri;
  }

  public void setReferenceUri(String referenceUri) {
    this.referenceUri = referenceUri;
  }

}
