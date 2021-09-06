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

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudWithRelaysAndPublicRelaysResponseDTO extends CloudResponseDTO {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -7909487804122446800L;

	private List<RelayResponseDTO> gatekeeperRelays;
	private List<RelayResponseDTO> gatewayRelays;
	private List<RelayResponseDTO> publicRelays;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysResponseDTO(final long id, final String operator, final String name, final boolean secure, final boolean neighbor, final boolean ownCloud, final String authenticationInfo,
									  final String createdAt, final String updatedAt, final List<RelayResponseDTO> gatekeeperRelays, final List<RelayResponseDTO> gatewayRelays, final List<RelayResponseDTO> publicRelays) {
		super(id, operator, name, secure, neighbor, ownCloud, authenticationInfo, createdAt, updatedAt);
		this.gatekeeperRelays = gatekeeperRelays;
		this.gatewayRelays = gatewayRelays;
		this.publicRelays = publicRelays;
	}

	//-------------------------------------------------------------------------------------------------
	public List<RelayResponseDTO> getGatekeeperRelays() { return gatekeeperRelays; }
	public List<RelayResponseDTO> getGatewayRelays() { return gatewayRelays; }
	public List<RelayResponseDTO> getPublicRelays() { return publicRelays; }

	//-------------------------------------------------------------------------------------------------
	public void setGatekeeperRelays(final List<RelayResponseDTO> gatekeeperRelays) { this.gatekeeperRelays = gatekeeperRelays; }
	public void setGatewayRelays(final List<RelayResponseDTO> gatewayRelays) { this.gatewayRelays = gatewayRelays; }
	public void setPublicRelays(final List<RelayResponseDTO> publicRelays) { this.publicRelays = publicRelays; }
	
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