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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

@JsonInclude(Include.NON_NULL)
public class AutoCompleteDataResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2436219204484930542L;
	
	private List<IdValueDTO> serviceList;
	private List<SystemResponseDTO> systemList;
	private List<IdValueDTO> interfaceList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AutoCompleteDataResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AutoCompleteDataResponseDTO(final List<IdValueDTO> serviceList, final List<SystemResponseDTO> systemList, final List<IdValueDTO> interfaceList) {
		this.serviceList = serviceList;
		this.systemList = systemList;
		this.interfaceList = interfaceList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<IdValueDTO> getServiceList() { return serviceList; }
	public List<SystemResponseDTO> getSystemList() { return systemList; }
	public List<IdValueDTO> getInterfaceList() { return interfaceList; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceList(final List<IdValueDTO> serviceList) { this.serviceList = serviceList; }
	public void setSystemList(final List<SystemResponseDTO> systemList) { this.systemList = systemList; }
	public void setInterfaceList(final List<IdValueDTO> interfaceList) { this.interfaceList = interfaceList; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}