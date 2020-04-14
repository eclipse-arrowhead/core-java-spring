package eu.arrowhead.common.dto.shared.mscv;


public class MscvSshTarget {

  private long id;
  private String address;
  private long port;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }


  public long getPort() {
    return port;
  }

  public void setPort(long port) {
    this.port = port;
  }

}
