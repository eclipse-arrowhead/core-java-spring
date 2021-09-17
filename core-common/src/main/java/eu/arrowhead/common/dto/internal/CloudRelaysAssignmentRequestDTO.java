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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudRelaysAssignmentRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -999808999290231598L;
	
	private Long cloudId;
	private List<Long> gatekeeperRelayIds;
	private List<Long> gatewayRelayIds;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() { return cloudId; }
	public List<Long> getGatekeeperRelayIds() { return gatekeeperRelayIds; }
	public List<Long> getGatewayRelayIds() { return gatewayRelayIds; }
	
	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final Long cloudId) { this.cloudId = cloudId; }
	public void setGatekeeperRelayIds(final List<Long> gatekeeperRelayIds) { this.gatekeeperRelayIds = gatekeeperRelayIds; }
	public void setGatewayRelayIds(final List<Long> gatewayRelayIds) { this.gatewayRelayIds = gatewayRelayIds; }
	
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