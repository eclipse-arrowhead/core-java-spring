/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.serviceregistry.database.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;

public class RegistryUtils {
	
	//=================================================================================================
	// members
	
	private static Logger logger = LogManager.getLogger(RegistryUtils.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static boolean pingService(final String address, final int port, final int timeout) {
		final InetSocketAddress providerHost = new InetSocketAddress(address, port);
		try (final Socket socket = new Socket()) {
			socket.connect(providerHost, timeout);
			return true;
		} catch (final IOException ex) {
			return false;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public static List<String> normalizeInterfaceNames(final List<String> interfaceNames) { 
		logger.debug("normalizeInterfaceNames started...");
		if (interfaceNames == null) {
			return List.of();
		}
		
		return interfaceNames.parallelStream().filter(Objects::nonNull).filter(e -> !e.isBlank()).map(e -> e.toUpperCase().trim()).collect(Collectors.toList());
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
			for (final Iterator<ServiceRegistryInterfaceConnection> it = srEntry.getInterfaceConnections().iterator(); it.hasNext();) {
				final ServiceRegistryInterfaceConnection conn = it.next();
				if (interfaceRequirements.contains(conn.getServiceInterface().getInterfaceName())) {
					remove = false;
				} else {
					it.remove();
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
		
		return securityTypes.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
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
	// This method may CHANGE the content of providedServices
	public static void filterOnVersion(final List<ServiceRegistry> providedServices, final int targetVersion) {
		logger.debug("filterOnVersion(List, int) started...");
		if (providedServices == null || providedServices.isEmpty()) {
			return;
		}
		
		providedServices.removeIf(sr -> sr.getVersion() == null || sr.getVersion().intValue() != targetVersion);
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providedServices
	public static void filterOnVersion(final List<ServiceRegistry> providedServices, final int minVersion, final int maxVersion) {
		logger.debug("filterOnVersion(List, int, int) started...");
		if (providedServices == null || providedServices.isEmpty()) {
			return;
		}
		
		providedServices.removeIf(sr -> sr.getVersion() == null || sr.getVersion().intValue() < minVersion || sr.getVersion().intValue() > maxVersion);
	}
	
	//-------------------------------------------------------------------------------------------------
	// throws IllegalStateException if two keys are identical after trim
	@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
	public static Map<String,String> normalizeMetadata(final Map<String,String> metadata) throws IllegalStateException { 
		logger.debug("normalizeMetadata started...");
		if (metadata == null) {
			return Map.of();
		}
		
		return metadata.entrySet().parallelStream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> e.getKey().trim(),
																	  		 								   e -> e.getValue().trim()));
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providedServices
	public static void filterOnMeta(final List<ServiceRegistry> providedServices, final Map<String,String> metadataRequirements) {
		logger.debug("filterOnMeta started...");
		if (providedServices == null || providedServices.isEmpty() || metadataRequirements == null || metadataRequirements.isEmpty()) {
			return;
		}
		
		final List<ServiceRegistry> toBeRemoved = new ArrayList<>();
		for (final ServiceRegistry srEntry : providedServices) {
			final Map<String,String> metadata = Utilities.text2Map(srEntry.getMetadata());
			if (metadata == null || !metadata.entrySet().containsAll(metadataRequirements.entrySet())) {
				toBeRemoved.add(srEntry);
			}
		}
		
		providedServices.removeAll(toBeRemoved);
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providedServices
	public static void filterOnPing(final List<ServiceRegistry> providedServices, final int timeout) {
		logger.debug("filterOnPing started...");
		if (providedServices == null || providedServices.isEmpty() || timeout <= 0) {
			return;
		}
		
		providedServices.removeIf(sr -> !pingService(sr.getSystem().getAddress(), sr.getSystem().getPort(), timeout));
	}
	
	//-------------------------------------------------------------------------------------------------
	public static List<AddressType> normalizeAddressTypes(final List<AddressType> addressTypes) { 
		logger.debug("normalizeAddressTypes started...");
		if (addressTypes == null) {
			return List.of();
		}
		
		return addressTypes.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of providedServices
	public static void filterOnProviderAddressType(final List<ServiceRegistry> providedServices, final List<AddressType> addressTypes) { 
		logger.debug("filterOnProviderAddressType started...");
		if (providedServices == null || providedServices.isEmpty() || addressTypes == null || addressTypes.isEmpty()) {
			return;
		}
		
		final List<ServiceRegistry> toBeRemoved = new ArrayList<>();
		for (final ServiceRegistry srEntry : providedServices) {
			if (srEntry.getSystem().getAddressType() == null || !addressTypes.contains(srEntry.getSystem().getAddressType())) {
				toBeRemoved.add(srEntry);
			}
		}
		
		providedServices.removeAll(toBeRemoved);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private RegistryUtils() {
		throw new UnsupportedOperationException();
	}
}