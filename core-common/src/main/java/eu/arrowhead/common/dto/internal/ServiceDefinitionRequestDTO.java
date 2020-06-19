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

public class ServiceDefinitionRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1966787184376371095L;
	
	private String serviceDefinition;
			
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO(final String serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
}