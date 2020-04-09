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

import java.util.ArrayList;
import java.util.Iterator;
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
      logger.debug("getAllSystems started ...");

      List<String> res = new ArrayList<>();
      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement pe = epi.next();

	if (!systemExists(res, pe.systemName)) {
	  res.add(pe.systemName);
	}
      }
      return res;
    }

    //-------------------------------------------------------------------------------------------------
    private boolean systemExists(List <String> systems, String systemName) {

      if (systems == null || systemName == null) {
	return false;
      }

      Iterator<String> sysi = systems.iterator();
      while (sysi.hasNext()) {
	String tmpsys = sysi.next();
	if (tmpsys.equals(systemName)) {
	  return true;
	}
      }


      return false;
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<ProxyElement> getEndpointsFromSystem(String systemName) {
      logger.debug("getEndpointsFromSystem started ...");

      if (systemName == null) {
	return null;
      }

      ArrayList<ProxyElement> res = new ArrayList<>();
      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement pe = epi.next();
	if (systemName.equals(pe.systemName)) {
	  res.add(pe);
	}
      }

      return res;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean deleteAllEndpointsForSystem(String systemName) {
      logger.debug("deleteAllEndpointsForSystem started ...");

      if (systemName == null) {
	return false;
      }

      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement pe = epi.next();
	if (systemName.equals(pe.systemName)) {
	  epi.remove();
	}
      }

      return true;
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<String> getEndpointsNamesFromSystem(String systemName) {
      logger.debug("getEndpointsNamesFromSystem started ...");

      if (systemName == null) {
	return null;
      }

      ArrayList<String> res = new ArrayList<>();
      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement pe = epi.next();
	if (systemName.equals(pe.systemName)) {
	  res.add(pe.serviceName);
	}
      }

      return res;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean addEndpointForService(ProxyElement e) {
      logger.debug("addEndpointForService started ...");

      if (e == null) {
	return false;
      }

      for(ProxyElement tmp: endpoints) {
	if (tmp.serviceName.equals(e.serviceName)) {
	  return false;
	}
      }
      endpoints.add(e);

      return true;
    }

    //-------------------------------------------------------------------------------------------------
    public ProxyElement getEndpointFromService(String systemName, String serviceName) {
      logger.debug("getEndpointFromService started ...");

      if (systemName == null || serviceName == null) {
	return null;
      }

      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement currpe = epi.next();
	if (serviceName.equals(currpe.serviceName)) {
	  return currpe;
	}
      }

      return null;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean updateEndpointFromService(String systemName, String serviceName, Vector<SenML> msg) {
      logger.debug("updateEndpointFromService started ...");

      if (systemName == null || serviceName == null) {
	return false;
      }

      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement pe = epi.next();
	if (systemName.equals(pe.systemName) && serviceName.equals(pe.serviceName)) {
	  pe.msg = msg;
	  return true;
	}
      }
      return false;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean deleteEndpointFromService(String systemName, String serviceName) {
      logger.debug("deleteEndpointFromService started ...");

      if (systemName == null || serviceName == null) {
	return false;
      }

      Iterator<ProxyElement> epi = endpoints.iterator();

      while (epi.hasNext()) {
	ProxyElement pe = epi.next();
	if (systemName.equals(pe.systemName) && serviceName.equals(pe.serviceName)) {
	  epi.remove();
	  return true;
	}
      }

      return false;
    }


    //=================================================================================================
    // assistant methods

}
