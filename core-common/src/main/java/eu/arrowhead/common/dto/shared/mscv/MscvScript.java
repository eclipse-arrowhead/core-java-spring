package eu.arrowhead.common.dto.shared.mscv;


public class MscvScript {

  private long id;
  private String layer;
  private String os;
  private String path;
  private long mipId;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getLayer() {
    return layer;
  }

  public void setLayer(String layer) {
    this.layer = layer;
  }


  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }


  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }


  public long getMipId() {
    return mipId;
  }

  public void setMipId(long mipId) {
    this.mipId = mipId;
  }

}
