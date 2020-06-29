/********************************************************************************
 * Copyright (c) 2020 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/
package eu.arrowhead.core.datamanager.service;

import java.util.Vector;
import eu.arrowhead.common.dto.shared.SenML;

public class ProxyElement {

  private String systemName = null;
  private String serviceName = null;
  private Vector<SenML> message = null;


  public ProxyElement(final String systemName, final String serviceName) {
    this.systemName = systemName;
    this.serviceName = serviceName;
    this.message = null;
  }


  public ProxyElement(final String systemName, final String serviceName, final Vector<SenML> message) {
    this.systemName = systemName;
    this.serviceName = serviceName;
    this.message = message;
  }

  public String getSystemName() {return systemName; };
  public void setSystemName(final String systemName) {this.systemName = systemName; };
 
  public String getServiceName() {return serviceName; };
  public void setServiceName(final String serviceName) {this.serviceName = serviceName; };
 
  public Vector<SenML> getMessage() {return message; };
  public void setMessage(final Vector<SenML> message) {this.message = message; };
 
}

