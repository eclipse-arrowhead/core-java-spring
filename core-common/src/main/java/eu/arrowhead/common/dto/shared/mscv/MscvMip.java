package eu.arrowhead.common.dto.shared.mscv;


public class MscvMip {

  private long id;
  private long extId;
  private String name;
  private String description;
  private long standardId;
  private long categoryId;
  private long domainId;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getExtId() {
    return extId;
  }

  public void setExtId(long extId) {
    this.extId = extId;
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


  public long getStandardId() {
    return standardId;
  }

  public void setStandardId(long standardId) {
    this.standardId = standardId;
  }


  public long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(long categoryId) {
    this.categoryId = categoryId;
  }


  public long getDomainId() {
    return domainId;
  }

  public void setDomainId(long domainId) {
    this.domainId = domainId;
  }

}
