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

import java.util.ArrayList;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;


@Service
public class HistorianService {
	
	//=================================================================================================
	// members
	 
	private final Logger logger = LogManager.getLogger(HistorianService.class);
	
	@Autowired
	private DataManagerDBService dataManagerDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ArrayList<String> getSystems(){
		logger.debug("getSystems");
	
		return dataManagerDBService.getAllHistorianSystems();
	}
	  
	//-------------------------------------------------------------------------------------------------
	public ArrayList<String> getServicesFromSystem(final String systemName) {
		if (Utilities.isEmpty(systemName)) {
			return null;
	    }
	    logger.debug("getServicesFromSystem for {}", systemName);
	
	    return dataManagerDBService.getServicesFromSystem(systemName);
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean createEndpoint(final String systemName, final String serviceName) {
	    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
	    	return false;
	    }
	    logger.debug("createEndpoint for {}/{}", systemName, serviceName);
	
	    return dataManagerDBService.createEndpoint(systemName, serviceName);
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean addServiceForSystem(final String systemName, final String serviceName, final String serviceType) {
	    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName) || Utilities.isEmpty(serviceType)) {
	    	return false;
	    }
	    logger.debug("addServiceForSystem for {}/{}", systemName, serviceName);
	
	    return dataManagerDBService.addServiceForSystem(systemName, serviceName, serviceType);
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean updateEndpoint(final String systemName, final String serviceName, final Vector<SenML> msg) {
	    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
	    	return false;
	    }
	    logger.debug("updateEndpoint for {}/{}", systemName, serviceName);
	
	    if (msg == null) {
	    	return false;
	    }
	
	    if (msg.size() == 0) {
	    	return false;
	    }
	
	    return dataManagerDBService.updateEndpoint(systemName, serviceName, msg);
	}
	  
	//-------------------------------------------------------------------------------------------------
	public Vector<SenML> fetchEndpoint(final String systemName, final String serviceName, final double from, final double to, final int count) {
	    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
	    	return null;
	    }
	    logger.debug("fetchEndpoint for {}/{}", systemName, serviceName);
	
	    return dataManagerDBService.fetchMessagesFromEndpoint(systemName, serviceName, from, to, count);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Vector<SenML> fetchEndpoint(final String systemName, final String serviceName, final double from, final double to, final Vector<Integer> counts, final Vector<String> signals) {
	    if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
	    	return null;
	    }
	    logger.debug("fetchEndpoint with signals for {}/{}", systemName, serviceName);
	
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
}