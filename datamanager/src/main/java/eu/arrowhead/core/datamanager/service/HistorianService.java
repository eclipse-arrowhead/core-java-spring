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
public class HistorianService {

  //=================================================================================================
  // members
  //
 
  private final Logger logger = LogManager.getLogger(HistorianService.class);

  @Autowired
  private DataManagerDBService dataManagerDBService;

  //=================================================================================================
  // methods
  
  //-------------------------------------------------------------------------------------------------

  //-------------------------------------------------------------------------------------------------
  public HistorianService() {
  }


  //-------------------------------------------------------------------------------------------------
  public ArrayList<String> getSystems(){
    logger.debug("getSystems started ...");

    return dataManagerDBService.getAllHistorianSystems();
  }

  
  //-------------------------------------------------------------------------------------------------
  public ArrayList<String> getServicesFromSystem(final String systemName) {
    logger.debug("getServicesFromSystem started ...");

    if (Utilities.isEmpty(systemName)) {
      return null;
    }

    return dataManagerDBService.getServicesFromSystem(systemName);
  }

  //-------------------------------------------------------------------------------------------------
  public boolean createEndpoint(final String systemName, final String serviceName) {
    logger.debug("createEndpoint started ...");

    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
      return false;
    }

    return dataManagerDBService.createEndpoint(systemName, serviceName);
  }

  //-------------------------------------------------------------------------------------------------
  public boolean addServiceForSystem(final String systemName, final String serviceName, final String serviceType){
    logger.debug("addServiceForSystem started ...");

    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName) || Utilities.isEmpty(serviceType)) {
      return false;
    }

    return dataManagerDBService.addServiceForSystem(systemName, serviceName, serviceType);
  }

  //-------------------------------------------------------------------------------------------------
  public boolean updateEndpoint(final String systemName, final String serviceName, final Vector<SenML> msg) {
    logger.debug("updateEndpoint...");

    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
      return false;
    }

    if (msg == null) {
      return false;
    }

    if (msg.size() == 0) {
      return false;
    }

    return dataManagerDBService.updateEndpoint(systemName, serviceName, msg);
  }
  
  //-------------------------------------------------------------------------------------------------
  public Vector<SenML> fetchEndpoint(final String systemName, final String serviceName, double from, double to, final int count) {
    logger.debug("fetchEndpoint...");

    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
      return null;
    }

    return dataManagerDBService.fetchMessagesFromEndpoint(systemName, serviceName, from, to, count);
  }

  //-------------------------------------------------------------------------------------------------
  public Vector<SenML> fetchEndpoint(final String systemName, final String serviceName, double from, double to, final Vector<Integer> counts, final Vector<String> signals) {
    logger.debug("fetchEndpoint with signals...");

    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
      return null;
    }

    if (counts == null || signals == null) {
      return null;
    }

    if (signals.size() == 0) {
      return null;
    }

    if (counts.size() != signals.size()) {
      return null;
    }

    return dataManagerDBService.fetchSignalsFromEndpoint(systemName, serviceName, from, to, counts, signals);
  }

  
  //=================================================================================================
  // assistant methods
  
}
