package eu.arrowhead.core.translator.services.translator.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.core.translator.services.translator.common.Translation.ContentType;
import eu.arrowhead.core.translator.services.translator.common.Translation.Protocol;

@JsonInclude(Include.NON_NULL)
public class InterfaceDTO implements Serializable {

  private static final long serialVersionUID = 3919207845124510215L;
  private String ip;
  private int port;
  private final Protocol protocol;
  private final ContentType contentType;

  @JsonCreator
  public InterfaceDTO(
      @JsonProperty("ip") String ip,
      @JsonProperty("port") int port,
      @JsonProperty("protocol") Protocol protocol,
      @JsonProperty("contentType") ContentType contentType) {
    this.ip = ip;
    this.port = port;
    this.protocol = protocol;
    this.contentType = contentType;
  }

  public InterfaceDTO(String ip, int port, Protocol protocol) {
    this.ip = ip;
    this.port = port;
    this.protocol = protocol;
    contentType = ContentType.ANY;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public ContentType getContentType() {
    return contentType;
  }

}
