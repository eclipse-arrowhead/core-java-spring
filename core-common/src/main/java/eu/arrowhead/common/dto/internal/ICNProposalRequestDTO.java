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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ICNProposalRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 1400876207612030929L;
	
	private ServiceQueryFormDTO requestedService;
	private CloudRequestDTO requesterCloud;
	private SystemRequestDTO requesterSystem;
	private List<SystemRequestDTO> preferredSystems = new ArrayList<>();
	private List<RelayRequestDTO> knownGatewayRelays = new ArrayList<>();
	private List<RelayRequestDTO> preferredGatewayRelays = new ArrayList<>();
	private OrchestrationFlags negotiationFlags = new OrchestrationFlags();
	private Map<String,String> commands = new HashMap<>();
	private boolean gatewayIsPresent = false;
	private String consumerGatewayPublicKey;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalRequestDTO(final ServiceQueryFormDTO requestedService, final CloudRequestDTO requesterCloud, final SystemRequestDTO requesterSystem, 
							     final List<SystemRequestDTO> preferredSystems, final List<RelayRequestDTO> preferredGatewayRelays, final List<RelayRequestDTO> knownGatewayRelays,
							     final OrchestrationFlags negotiationFlags, final boolean gatewayIsPresent, final Map<String,String> commands) {
		Assert.notNull(requestedService, "Requested service is null.");
		Assert.notNull(requesterCloud, "Requester cloud is null.");
		Assert.notNull(requesterSystem, "Requester system is null.");
		
		this.requestedService = requestedService;
		this.requesterCloud = requesterCloud;
		this.requesterSystem = requesterSystem;
		this.gatewayIsPresent = gatewayIsPresent;
		
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
		
		if (preferredGatewayRelays != null) {
			this.preferredGatewayRelays = preferredGatewayRelays;
		}
		
		if (knownGatewayRelays != null) {
			this.knownGatewayRelays = knownGatewayRelays;
		}
		
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}
		
		if (commands != null) {
			this.commands = commands;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public List<SystemRequestDTO> getPreferredSystems() { return preferredSystems; }
	public List<RelayRequestDTO> getPreferredGatewayRelays() { return preferredGatewayRelays; }
	public List<RelayRequestDTO> getKnownGatewayRelays() { return knownGatewayRelays; }
	public OrchestrationFlags getNegotiationFlags() { return negotiationFlags; }
	public Map<String, String> getCommands() { return commands; }
	public boolean getGatewayIsPresent() { return gatewayIsPresent; }
	public String getConsumerGatewayPublicKey() { return consumerGatewayPublicKey; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setGatewayIsPresent(final boolean gatewayIsPresent) { this.gatewayIsPresent = gatewayIsPresent; }
	public void setConsumerGatewayPublicKey(final String consumerGatewayPublicKey) { this.consumerGatewayPublicKey = consumerGatewayPublicKey; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredSystems(final List<SystemRequestDTO> preferredSystems) {
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredGatewayRelays(final List<RelayRequestDTO> preferredGatewayRelays) {
		if (preferredGatewayRelays != null) {
			this.preferredGatewayRelays = preferredGatewayRelays;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setKnownGatewayRelays(final List<RelayRequestDTO> knownGatewayRelays) {
		if (knownGatewayRelays != null) {
			this.knownGatewayRelays = knownGatewayRelays;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setNegotiationFlags(final OrchestrationFlags negotiationFlags) {
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setCommands(final Map<String,String> commands) {
		if (commands != null) {
			this.commands = commands;
		}
	}
	
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