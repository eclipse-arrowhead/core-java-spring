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

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;

import java.util.List;
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.Vector;


@Service
public class ProxyService {

    //=================================================================================================
    // members
    //

    private final Logger logger = LogManager.getLogger(ProxyService.class);

    private List<ProxyElement> endpoints = new ArrayList<>();


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ProxyService() {
      endpoints = new ArrayList<>();
    }

    //-------------------------------------------------------------------------------------------------
    public List<String> getAllSystems() {
      logger.debug("getAllSystems...");

      List<String> res = new ArrayList<>();
      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement pe = pei.next();

        if (!systemExists(res, pe.getSystemName())) {
          res.add(pe.getSystemName());
        }
      }
      return res;
    }

    //-------------------------------------------------------------------------------------------------
    private boolean systemExists(final List <String> systems, final String systemName) {
      logger.debug("systemExists...");

      if(Utilities.isEmpty(systemName)){
        return false;
      }

      if (systems == null) {
        return false;
      }

      Iterator<String> sysi = systems.iterator();
      while (sysi.hasNext()) {
        String tmpSystemName = sysi.next();
        if (tmpSystemName.equals(systemName)) {
          return true;
        }
      }

      return false;
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<ProxyElement> getEndpointsFromSystem(final String systemName) {
      logger.debug("getEndpointsFromSystem...");

      if(Utilities.isEmpty(systemName)) {
        return null;
      }

      ArrayList<ProxyElement> res = new ArrayList<>();
      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement pe = pei.next();
        if (systemName.equals(pe.getSystemName())) {
          res.add(pe);
        }
      }

      return res;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean deleteAllEndpointsForSystem(final String systemName) {
      logger.debug("deleteAllEndpointsForSystem...");

      if(Utilities.isEmpty(systemName)) {
        return false;
      }

      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement pe = pei.next();
        if (systemName.equals(pe.getSystemName())) {
          pei.remove();
        }
      }

      return true;
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<String> getEndpointsNamesFromSystem(final String systemName) {
      logger.debug("getEndpointsNamesFromSystem started ...");

      if(Utilities.isEmpty(systemName)) {
        return null;
      }

      ArrayList<String> res = new ArrayList<>();
      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement pe = pei.next();
        if (systemName.equals(pe.getSystemName())) {
          res.add(pe.getServiceName());
        }
      }

      return res;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean addEndpointForService(final ProxyElement e) {
      logger.debug("addEndpointForService...");

      if (e == null) {
        return false;
      }

      for(ProxyElement tmp: endpoints) {
        if (tmp.getServiceName().equals(e.getServiceName())) {
          return false;
        }
      }
      endpoints.add(e);

      return true;
    }

    //-------------------------------------------------------------------------------------------------
    public ProxyElement getEndpointFromService(final String systemName, final String serviceName) {
      logger.debug("getEndpointFromService...");

      if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
        return null;
      }

      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement currpe = pei.next();
        if (serviceName.equals(currpe.getServiceName())) {
          return currpe;
        }
      }

      return null;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean updateEndpointFromService(final String systemName, final String serviceName, final Vector<SenML> message) {
      logger.debug("updateEndpointFromService...");

      if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
        return false;
      }

      if (message == null) {
        return false;
      }

      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement pe = pei.next();
        if (systemName.equals(pe.getSystemName()) && serviceName.equals(pe.getServiceName())) {
          pe.setMessage(message);
          return true;
        }
      }
      return false;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean deleteEndpointFromService(final String systemName, final String serviceName) {
      logger.debug("deleteEndpointFromService...");

      if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
        return false;
      }

      Iterator<ProxyElement> pei = endpoints.iterator();

      while (pei.hasNext()) {
        ProxyElement pe = pei.next();
        if (systemName.equals(pe.getSystemName()) && serviceName.equals(pe.getServiceName())) {
          pei.remove();
          return true;
        }
      }

      return false;
    }


    //=================================================================================================
    // assistant methods

}
