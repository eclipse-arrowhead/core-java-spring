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
  private DataManagerDriver dataManagerDriver;

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
  public ArrayList<String> getServicesFromSystem(String systemName) {
    logger.debug("getServicesFromSystem started ...");

    if (systemName == null) {
      return null;
    }
    if (systemName.equals("")) {
      return null;
    }

    return dataManagerDBService.getServicesFromSystem(systemName);
  }

  //-------------------------------------------------------------------------------------------------
  public boolean createEndpoint(String systemName, String serviceName) {
    logger.debug("createEndpoint started ...");

    if (systemName == null || serviceName == null) {
      return false;
    }

    return dataManagerDBService.createEndpoint(systemName, serviceName);
  }

  //-------------------------------------------------------------------------------------------------
  public boolean addServiceForSystem(String systemName, String serviceName, String serviceType){
    logger.debug("addServiceForSystem started ...");

    return dataManagerDBService.addServiceForSystem(systemName, serviceName, serviceType);
  }

  //-------------------------------------------------------------------------------------------------
  public boolean updateEndpoint(String systemName, String serviceName, Vector<SenML> msg) {
    logger.debug("updateEndpoint started ...");

    if (systemName == null || serviceName == null) {
      return false;
    }
    if (msg == null) {
      return false;
    }
    return dataManagerDBService.updateEndpoint(systemName, serviceName, msg);
  }
  
  //-------------------------------------------------------------------------------------------------
  public Vector<SenML> fetchEndpoint(String systemName, String serviceName, double from, double to, int count) {
    logger.debug("fetchEndpoint started ...");

    if (systemName == null || serviceName == null) {
      logger.debug("systemName or serviceName is null");
      return null;
    }

    return dataManagerDBService.fetchMessagesFromEndpoint(systemName, serviceName, from, to, count);
  }

  //-------------------------------------------------------------------------------------------------
  public Vector<SenML> fetchEndpoint(String systemName, String serviceName, double from, double to, Vector<Integer> counts, Vector<String> signals) {
    logger.debug("fetchEndpoint started ...");

    if (systemName == null || serviceName == null) {
      logger.debug("systemName or serviceName is null");
      return null;
    }

    return dataManagerDBService.fetchSignalsFromEndpoint(systemName, serviceName, from, to, counts, signals);
  }

  
  //=================================================================================================
  // assistant methods
  
  


}
