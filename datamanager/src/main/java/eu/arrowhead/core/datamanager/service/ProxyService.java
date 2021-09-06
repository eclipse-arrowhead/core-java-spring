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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@Service
public class ProxyService {

    //=================================================================================================
    // members
	
	private static final String INVALID_FORMAT_ERROR_MESSAGE = " has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING;
 
    private final Logger logger = LogManager.getLogger(ProxyService.class);

    private List<ProxyElement> endpoints = new ArrayList<>();
    
    @Autowired
    private CommonNamePartVerifier cnVerifier;
    
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ProxyService() {
    	endpoints = new ArrayList<>();
    }

    //-------------------------------------------------------------------------------------------------
    public List<String> getAllSystems() {
    	logger.debug("getAllSystems...");
	
	    final List<String> res = new ArrayList<>();
	    final Iterator<ProxyElement> pei = endpoints.iterator();
	
	    while (pei.hasNext()) {
	        final ProxyElement pe = pei.next();
	
	        if (!systemExists(res, pe.getSystemName())) {
	        	res.add(pe.getSystemName());
	        }
	    }
	    return res;
    }

    //-------------------------------------------------------------------------------------------------
    private boolean systemExists(final List <String> systems, final String systemName) {
    	logger.debug("systemExists...");

    	if (Utilities.isEmpty(systemName)) {
    		return false;
    	}
    	
    	if (systems == null) {
    		return false;
    	}

    	final Iterator<String> sysi = systems.iterator();
    	while (sysi.hasNext()) {
    		final String tmpSystemName = sysi.next();
    		if (tmpSystemName.equals(systemName)) {
    			return true;
    		}
    	}

    	return false;
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<ProxyElement> getEndpointsFromSystem(final String systemName) {
    	logger.debug("getEndpointsFromSystem...");

    	if (Utilities.isEmpty(systemName)) {
    		return null;
    	}
      
    	final String _systemName = systemName.toLowerCase().trim();

    	final ArrayList<ProxyElement> res = new ArrayList<>();
    	final Iterator<ProxyElement> pei = endpoints.iterator();

    	while (pei.hasNext()) {
    		final ProxyElement pe = pei.next();
    		if (_systemName.equals(pe.getSystemName())) {
    			res.add(pe);
    		}
    	}

    	return res;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean deleteAllEndpointsForSystem(final String systemName) {
    	logger.debug("deleteAllEndpointsForSystem...");

    	if (Utilities.isEmpty(systemName)) {
    		return false;
    	}
      
    	final String _systemName = systemName.toLowerCase().trim();

    	final Iterator<ProxyElement> pei = endpoints.iterator();

    	while (pei.hasNext()) {
    		final ProxyElement pe = pei.next();
    		if (_systemName.equals(pe.getSystemName())) {
    			pei.remove();
    		}
    	}

    	return true;
    }

    //-------------------------------------------------------------------------------------------------
    public ArrayList<String> getEndpointsNamesFromSystem(final String systemName) {
    	logger.debug("getEndpointsNamesFromSystem started ...");

    	if (Utilities.isEmpty(systemName)) {
    		return null;
    	}
      
    	final String _systemName = systemName.toLowerCase().trim();

    	final ArrayList<String> res = new ArrayList<>();
    	final Iterator<ProxyElement> pei = endpoints.iterator();

    	while (pei.hasNext()) {
    		final ProxyElement pe = pei.next();
    		if (_systemName.equals(pe.getSystemName())) {
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
      
    	if (!validateAndNormalizeProxyElement(e)) {
    		return false;
    	}

    	for (final ProxyElement tmp: endpoints) {
    		if (tmp.getServiceName().equals(e.getServiceName())) {
    			return false;
    		}
    	}
    	endpoints.add(e);

    	return true;
    }

    //-------------------------------------------------------------------------------------------------
	private boolean validateAndNormalizeProxyElement(final ProxyElement e) {
		try {
			Assert.isTrue(!Utilities.isEmpty(e.getSystemName()), "System name is blank");
			Assert.isTrue(cnVerifier.isValid(e.getSystemName()), "System name" + INVALID_FORMAT_ERROR_MESSAGE);
			Assert.isTrue(!Utilities.isEmpty(e.getServiceName()), "Service name is blank");
			
			if (useStrictServiceDefinitionVerifier) {
				Assert.isTrue(cnVerifier.isValid(e.getServiceName()), "Service name" + INVALID_FORMAT_ERROR_MESSAGE);
			}
			
			e.setSystemName(e.getSystemName().toLowerCase().trim());
			e.setServiceName(e.getServiceName().toLowerCase().trim());
			
			return true;
		} catch (final IllegalArgumentException ex) {
			logger.debug("Invalid input: " + ex.getMessage());
			return false;
		}
	}

	//-------------------------------------------------------------------------------------------------
    public ProxyElement getEndpointFromService(final String systemName, final String serviceName) {
    	logger.debug("getEndpointFromService...");

    	if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
    		return null;
    	}
      
    	final String _serviceName = serviceName.toLowerCase().trim();

    	final Iterator<ProxyElement> pei = endpoints.iterator();

    	while (pei.hasNext()) {
    		final ProxyElement currpe = pei.next();
    		if (_serviceName.equals(currpe.getServiceName())) {
    			return currpe;
    		}
    	}

    	return null;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean updateEndpointFromService(final String systemName, final String serviceName, final Vector<SenML> message) {
    	logger.debug("updateEndpointFromService...");

    	if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
    		return false;
    	}

    	if (message == null) {
    		return false;
    	}
      
    	final String _systemName = systemName.toLowerCase().trim();
    	final String _serviceName = serviceName.toLowerCase().trim();

    	final Iterator<ProxyElement> pei = endpoints.iterator();

    	while (pei.hasNext()) {
    		final ProxyElement pe = pei.next();
    		if (_systemName.equals(pe.getSystemName()) && _serviceName.equals(pe.getServiceName())) {
    			pe.setMessage(message);
    			return true;
    		}
    	}
    	return false;
    }

    //-------------------------------------------------------------------------------------------------
    public boolean deleteEndpointFromService(final String systemName, final String serviceName) {
    	logger.debug("deleteEndpointFromService...");

    	if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
    		return false;
    	}
      
    	final String _systemName = systemName.toLowerCase().trim();
    	final String _serviceName = serviceName.toLowerCase().trim();

    	final Iterator<ProxyElement> pei = endpoints.iterator();

    	while (pei.hasNext()) {
    		final ProxyElement pe = pei.next();
    		if (_systemName.equals(pe.getSystemName()) && _serviceName.equals(pe.getServiceName())) {
    			pei.remove();
    			return true;
    		}
    	}

    	return false;
    }
}