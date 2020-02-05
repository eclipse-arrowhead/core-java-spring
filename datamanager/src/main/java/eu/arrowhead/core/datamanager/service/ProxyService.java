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
    private List<ProxyElement> endpoints = new ArrayList<>();

    static {
    }

    public ProxyService() {
	endpoints = new ArrayList<>();
    }

    public List<String> getAllSystems() {
	List<String> res = new ArrayList<>();
	Iterator<ProxyElement> epi = endpoints.iterator();

	while (epi.hasNext()) {
	    ProxyElement pe = epi.next();


	    if (!systemExists(res, pe.systemName))
	    	res.add(pe.systemName);
	}
	return res;
    }

    private boolean systemExists(List <String> systems, String systemName) {
	    Iterator<String> sysi = systems.iterator();
	    while (sysi.hasNext()) {
		    String tmpsys = sysi.next();
		    if (tmpsys.equals(systemName))
			return true;
	    }


	    return false;
    }


    public ArrayList<ProxyElement> getEndpoints(String systemName) {
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

    public ArrayList<String> getEndpointsNames(String systemName) {
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


    public boolean addEndpoint(ProxyElement e) {
	for(ProxyElement tmp: endpoints) {
	    if (tmp.serviceName.equals(e.serviceName))
		return false;
	}

	endpoints.add(e);
	return true;
    }


    public ProxyElement getEndpoint(String serviceName) {
	Iterator<ProxyElement> epi = endpoints.iterator();

	while (epi.hasNext()) {
	    ProxyElement curpe = epi.next();
	    if (serviceName.equals(curpe.serviceName)) {
		return curpe;
	    }
	}

	return null;
    }


    public boolean updateEndpoint(String systemName, String serviceName, Vector<SenML> msg) {
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

    public boolean deleteEndpoint(String serviceName) {
	Iterator<ProxyElement> epi = endpoints.iterator();

	while (epi.hasNext()) {
	    ProxyElement pe = epi.next();
	    if (serviceName.equals(pe.serviceName)) {
		    epi.remove();
		    return true;
	    }
	}
	return false;
    }
}
