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

public class CloudRelayFormDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -2722459200530984365L;
	
	private CloudResponseDTO cloud;
	private RelayResponseDTO relay;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudRelayFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public CloudRelayFormDTO(final CloudResponseDTO cloud, final RelayResponseDTO relay) {
		this.cloud = cloud;
		this.relay = relay;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public RelayResponseDTO getRelay() { return relay; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setRelay(final RelayResponseDTO relay) { this.relay = relay; }
	
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