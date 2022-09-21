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

import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

public class ICNProposalResponseDTO extends OrchestrationResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -206034951431082007L;

	private boolean useGateway = false;
	
	// these members only filled when useGateway is true
	private RelayResponseDTO relay;
	private GatewayProviderConnectionResponseDTO connectionInfo;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO() {
		super();
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO(final List<OrchestrationResultDTO> response) {
		super(response);
		
		this.useGateway = false;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO(final OrchestrationResultDTO result, final RelayResponseDTO relay, final GatewayProviderConnectionResponseDTO connectionInfo) {
		super(List.of(result));
		this.useGateway = true;
		this.relay = relay;
		this.connectionInfo = connectionInfo;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean isUseGateway() { return useGateway; }
	public RelayResponseDTO getRelay() { return relay; }
	public GatewayProviderConnectionResponseDTO getConnectionInfo() { return connectionInfo; }

	//-------------------------------------------------------------------------------------------------
	public void setUseGateway(final boolean useGateway) { this.useGateway = useGateway; }
	public void setRelay(final RelayResponseDTO relay) { this.relay = relay; }
	public void setConnectionInfo(final GatewayProviderConnectionResponseDTO connectionInfo) { this.connectionInfo = connectionInfo; }
	
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