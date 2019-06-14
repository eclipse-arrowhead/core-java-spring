package eu.arrowhead.core.serviceregistry.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.dto.ServiceSecurityType;

public class RegistryUtils {
	
	//=================================================================================================
	// members
	
	private static Logger logger = LogManager.getLogger(RegistryUtils.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static List<String> normalizeInterfaceNames(final List<String> interfaceNames) { 
		logger.debug("normalizeInterfaceNames started...");
		if (interfaceNames == null) {
			return List.of();
		}
		
		return interfaceNames.stream().filter(Objects::nonNull).filter(e -> !e.isBlank()).map(e -> e.toUpperCase().trim()).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providedServices
	public static void filterOnInterfaces(final List<ServiceRegistry> providedServices, final List<String> interfaceRequirements) {
		logger.debug("filterOnInterfaces started...");
		if (providedServices == null || providedServices.isEmpty() || interfaceRequirements == null || interfaceRequirements.isEmpty()) {
			return;
		}
		
		final List<ServiceRegistry> toBeRemoved = new ArrayList<>();
		for (final ServiceRegistry srEntry : providedServices) {
			boolean remove = true;
			for (final ServiceRegistryInterfaceConnection conn : srEntry.getInterfaceConnections()) {
				if (interfaceRequirements.contains(conn.getServiceInterface().getInterfaceName())) {
					remove = false;
					break;
				}
			}
			
			if (remove) {
				toBeRemoved.add(srEntry);
			}
		}
		
		providedServices.removeAll(toBeRemoved);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static List<ServiceSecurityType> normalizeSecurityTypes(final List<ServiceSecurityType> securityTypes) {
		logger.debug("normalizeSecurityTypes started...");
		if (securityTypes == null) {
			return List.of();
		}
		
		return securityTypes.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providedServices
	public static void filterOnSecurityType(final List<ServiceRegistry> providedServices, final List<ServiceSecurityType> securityTypes) {
		logger.debug("filterOnSecurityType started...");
		if (providedServices == null || providedServices.isEmpty() || securityTypes == null || securityTypes.isEmpty()) {
			return;
		}
		
		final List<ServiceRegistry> toBeRemoved = new ArrayList<>();
		for (final ServiceRegistry srEntry : providedServices) {
			if (!securityTypes.contains(srEntry.getSecure())) {
				toBeRemoved.add(srEntry);
			}
		}
		
		providedServices.removeAll(toBeRemoved);
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providerServices
	public static void filterOnVersion(final List<ServiceRegistry> providedServices, final int targetVersion) {
		logger.debug("filterOnVersion started...");
		providedServices.removeIf(sr -> sr.getVersion() == null || sr.getVersion().intValue() != targetVersion);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private RegistryUtils() {
		throw new UnsupportedOperationException();
	}
}