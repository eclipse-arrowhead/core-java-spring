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

public class QoSBestRelayRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8357593897933589292L;
	
	private CloudResponseDTO cloud;
	private String attribute;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSBestRelayRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSBestRelayRequestDTO(final CloudResponseDTO cloud, final String attribute) {
		this.cloud = cloud;
		this.attribute = attribute;
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public String getAttribute() { return attribute; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setAttribute(final String attribute) { this.attribute = attribute; }
	
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
