package eu.arrowhead.core.translator.services.translator.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.core.translator.services.translator.common.Translation.Protocol;

@JsonInclude(Include.NON_NULL)
public class InterfaceDTO implements Serializable {

  private static final long serialVersionUID = 3919207845124510215L;
  private String ip;
  private int port;
  private final Protocol protocol;

  public InterfaceDTO(String ip, int port, Protocol protocol) {
    this.ip = ip;
    this.port = port;
    this.protocol = protocol;
  }

  public String getIp(){
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
  
}
