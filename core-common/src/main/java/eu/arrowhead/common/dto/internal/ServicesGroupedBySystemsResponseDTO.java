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

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

@JsonInclude(Include.NON_NULL)
public class ServicesGroupedBySystemsResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6701946599623393658L;
	
	private long systemId;
	private String systemName;
	private String address;
	private int port;
	private List<ServiceRegistryResponseDTO> services;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGroupedBySystemsResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGroupedBySystemsResponseDTO(final long systemId, final String systemName, final String address, final int port, final List<ServiceRegistryResponseDTO> services) {
		this.systemId = systemId;
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.services = services;
	}

	//-------------------------------------------------------------------------------------------------
	public long getSystemId() {return systemId;}
	public String getSystemName() {return systemName;}
	public String getAddress() {return address;}
	public int getPort() {return port;}
	public List<ServiceRegistryResponseDTO> getServices() {return services;}

	//-------------------------------------------------------------------------------------------------
	public void setSystemId(final long id) {this.systemId = id;}
	public void setSystemName(final String systemName) {this.systemName = systemName;}
	public void setAddress(final String address) {this.address = address;}
	public void setPort(final int port) {this.port = port;}
	public void setServices(final List<ServiceRegistryResponseDTO> services) {this.services = services;}	
}