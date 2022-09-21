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

import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ICNRequestFormDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 932072920257234750L;
	
	private ServiceQueryFormDTO requestedService;
	private Long targetCloudId;
	private SystemRequestDTO requesterSystem;
	private List<SystemRequestDTO> preferredSystems = new ArrayList<>();
	private List<RelayRequestDTO> preferredGatewayRelays = new ArrayList<>();
	private OrchestrationFlags negotiationFlags = new OrchestrationFlags();
	private Map<String,String> commands = new HashMap<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNRequestFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ICNRequestFormDTO(final ServiceQueryFormDTO requestedService, final Long targetCloudId, final SystemRequestDTO requesterSystem, final List<SystemRequestDTO> preferredSystems,
						     final List<RelayRequestDTO> preferredGatewayRelays, final OrchestrationFlags negotiationFlags,  final Map<String,String> commands) {
		Assert.notNull(requestedService, "Requested service is null.");
		Assert.notNull(targetCloudId, "Target cloud id is null.");
		Assert.notNull(requesterSystem, "Requester system is null.");
		
		this.requestedService = requestedService;
		this.targetCloudId = targetCloudId;
		this.requesterSystem = requesterSystem;
		
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
		
		if (preferredGatewayRelays != null) {
			this.preferredGatewayRelays = preferredGatewayRelays;
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
	public Long getTargetCloudId() { return targetCloudId; }
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public List<SystemRequestDTO> getPreferredSystems() { return preferredSystems; }
	public List<RelayRequestDTO> getPreferredGatewayRelays() { return preferredGatewayRelays; }
	public OrchestrationFlags getNegotiationFlags() { return negotiationFlags; }	
	public Map<String, String> getCommands() { return commands; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setTargetCloudId(final Long targetCloudId) { this.targetCloudId = targetCloudId; }
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	
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