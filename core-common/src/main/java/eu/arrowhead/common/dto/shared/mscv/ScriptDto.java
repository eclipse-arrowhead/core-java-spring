package eu.arrowhead.common.dto.shared.mscv;


import java.util.StringJoiner;

public class ScriptDto {

  private String layer;
  private String os;
  private String path;
  private MipDto mip;

  public ScriptDto() {
    super();
  }

  public ScriptDto(final String layer, final String os, final String path, final MipDto mip) {
    this.layer = layer;
    this.os = os;
    this.path = path;
    this.mip = mip;
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


  public MipDto getMip() {
    return mip;
  }

  public void setMip(MipDto mipId) {
    this.mip = mip;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ScriptDto.class.getSimpleName() + "[", "]")
            .add("layer='" + layer + "'")
            .add("os='" + os + "'")
            .add("path='" + path + "'")
            .add("mip=" + mip)
            .toString();
  }
}
