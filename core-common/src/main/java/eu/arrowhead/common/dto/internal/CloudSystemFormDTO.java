/********************************************************************************
 * Copyright (c) 2020 AITIA
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class CloudSystemFormDTO implements Serializable {

	private static final long serialVersionUID = -1391881322265049372L;
	
	private CloudResponseDTO cloud;
	private SystemResponseDTO system;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudSystemFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudSystemFormDTO(final CloudResponseDTO cloud, final SystemResponseDTO system) {
		this.cloud = cloud;
		this.system = system;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public SystemResponseDTO getSystem() { return system; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setSystem(final SystemResponseDTO system) { this.system = system; }
	
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