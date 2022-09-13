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

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudWithRelaysResponseDTO extends CloudResponseDTO {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4289255963612036571L;
	
	private List<RelayResponseDTO> gatekeeperRelays; 
	private List<RelayResponseDTO> gatewayRelays;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysResponseDTO(final long id, final String operator, final String name, final boolean secure, final boolean neighbor, final boolean ownCloud, final String authenticationInfo,
									  final String createdAt, final String updatedAt, final List<RelayResponseDTO> gatekeeperRelays, final List<RelayResponseDTO> gatewayRelays) {
		super(id, operator, name, secure, neighbor, ownCloud, authenticationInfo, createdAt, updatedAt);
		this.gatekeeperRelays = gatekeeperRelays;
		this.gatewayRelays = gatewayRelays;
	}

	//-------------------------------------------------------------------------------------------------
	public List<RelayResponseDTO> getGatekeeperRelays() { return gatekeeperRelays; }
	public List<RelayResponseDTO> getGatewayRelays() { return gatewayRelays; }

	//-------------------------------------------------------------------------------------------------
	public void setGatekeeperRelays(final List<RelayResponseDTO> gatekeeperRelays) { this.gatekeeperRelays = gatekeeperRelays; }
	public void setGatewayRelays(final List<RelayResponseDTO> gatewayRelays) { this.gatewayRelays = gatewayRelays; }
	
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