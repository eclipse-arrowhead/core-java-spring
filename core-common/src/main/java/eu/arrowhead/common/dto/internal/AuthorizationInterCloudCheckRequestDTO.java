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

import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class AuthorizationInterCloudCheckRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -263209252842507399L;
	
	private CloudRequestDTO cloud;
	private String serviceDefinition;
	private List<IdIdListDTO> providerIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO(final CloudRequestDTO cloud, final String serviceDefinition, final List<IdIdListDTO> providerIdsWithInterfaceIds) {
		this.cloud = cloud;
		this.serviceDefinition = serviceDefinition;
		this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudRequestDTO getCloud() { return cloud; }
	public String getServiceDefinition() { return serviceDefinition; }
	public List<IdIdListDTO> getProviderIdsWithInterfaceIds() { return providerIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudRequestDTO cloud) { this.cloud = cloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setProviderIdsWithInterfaceIds(final List<IdIdListDTO> providerIdsWithInterfaceIds) { this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds; }
}