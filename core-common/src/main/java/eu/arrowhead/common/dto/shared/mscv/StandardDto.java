package eu.arrowhead.common.dto.shared.mscv;


import java.io.Serializable;
import java.util.StringJoiner;

public class StandardDto implements Serializable {

  private static final long serialVersionUID = 1L;
  private String identification;
  private String name;
  private String description;
  private String referenceUri;

  public StandardDto() {
    super();
  }

  public StandardDto(final String identification, final String name, final String referenceUri) {
    this.identification = identification;
    this.name = name;
    this.referenceUri = referenceUri;
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

  @Override
  public String toString() {
    return new StringJoiner(", ", StandardDto.class.getSimpleName() + "[", "]")
            .add("identification='" + identification + "'")
            .add("name='" + name + "'")
            .add("description='" + description + "'")
            .add("referenceUri='" + referenceUri + "'")
            .toString();
  }
}
