package eu.arrowhead.core.datamanager.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.shared.SenML;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*; 
import java.util.Vector;

@Service
public class ProxyService {
    private static List<ProxyElement> endpoints = new ArrayList<>();

    static {
	endpoints = new ArrayList<>();

    }


    /**
     * @fn static List<ProxyElement> getAllEndpoints()
     * @brief Fetches all endpoints and returns a list
     *
     */
    public static List<String> getAllEndpoints() {
	List<String> res = new ArrayList<>();
	Iterator<ProxyElement> epi = endpoints.iterator();

	while (epi.hasNext()) {
	    ProxyElement pe = epi.next();
	    res.add(pe.systemName);
	}
	return res;
    }


    /**
     * @fn static List<ProxyElement> getEndpoints(String systemName)
     * @brief Fetches all service endpoints that belongs to a specific system
     *
     */
    public static ArrayList<ProxyElement> getEndpoints(String systemName) {
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


    /**
     * @fn static boolean addEndpoint(ProxyElement e)
     * @brief Adds a newly created Proxy endpoint
     *
     */
    public static boolean addEndpoint(ProxyElement e) {
	for(ProxyElement tmp: endpoints) {
	    if (tmp.serviceName.equals(e.serviceName)) // already exists
		return false;
	}

	endpoints.add(e);
	return true;
    }


    /**
     * @fn static ProxyElement getEndpoint(String serviceName)
     * @brief Searches for a Proxy endpoint
     *
     */
    public static ProxyElement getEndpoint(String serviceName) {
	Iterator<ProxyElement> epi = endpoints.iterator();

	while (epi.hasNext()) {
	    ProxyElement curpe = epi.next();
	    if (serviceName.equals(curpe.serviceName)) {
		return curpe;
	    }
	}

	return null;
    }


    /**
     * @fn static boolean updateEndpoint(String serviceName, Vector<SenML> msg)
     * @brief Updates a Proxy endpoint
     *
     */
    public static boolean updateEndpoint(String systemName, String serviceName, Vector<SenML> msg) {
	Iterator<ProxyElement> epi = endpoints.iterator();

	while (epi.hasNext()) {
	    ProxyElement pe = epi.next();
	    if (serviceName.equals(pe.serviceName)) {
		pe.msg = msg;
		return true;
	    }
	}
	return false;
    }

}
