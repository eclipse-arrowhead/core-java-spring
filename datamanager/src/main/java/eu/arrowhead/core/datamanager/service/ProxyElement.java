package eu.arrowhead.core.datamanager.service;

import java.util.Vector;
import eu.arrowhead.common.dto.shared.SenML;

public class ProxyElement {

  public String systemName = null;
  public String serviceName = null;
  public String serviceType = null;
  public Vector<SenML> msg = null;


  public ProxyElement(String systemName, String serviceName) {
    this.systemName = new String(systemName);
    this.serviceName = new String(serviceName);
    this.msg = null;
  }


  public ProxyElement(String systemName, String serviceName, Vector<SenML> senml) {
    this.systemName = new String(systemName);
    this.serviceName = new String(serviceName);
    this.msg = senml;
  }


}

