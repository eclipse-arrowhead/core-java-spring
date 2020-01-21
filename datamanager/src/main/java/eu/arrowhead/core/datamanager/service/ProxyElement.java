package eu.arrowhead.core.datamanager.service;

import java.util.Vector;
import eu.arrowhead.common.dto.shared.SenML;

public class ProxyElement {

  public String systemName = null;     // i.e. tempSys-1._Audon-Thermometer−01._http._tcp._arrowhead.eu
  public String serviceName = null;           // i.e "_tempService1._tempSys-1. http._tcp._arrowhead.eu:8000"
  public String serviceType = null;    // _Tempreatre._http._tcp. etc...
  public Vector<SenML> msg = null;


  /**
   * @fn public ProxyElement(String systemName, String serviceName)
   * @brief creates a new ProxyElement 
   */
  public ProxyElement(String systemName, String serviceName) {
    this.systemName = new String(systemName);
    this.serviceName = new String(serviceName);
    this.msg = null;
  }


  /**
   * @fn public ProxyElement(String systemName, String serviceName, Vector<SenMLMessage> senml)
   * @brief creates a new ProxyElement from a SenML message
   */
  public ProxyElement(String systemName, String serviceName, Vector<SenML> senml) {
    this.systemName = new String(systemName);
    this.serviceName = new String(serviceName);
    this.msg = senml;
  }


}

