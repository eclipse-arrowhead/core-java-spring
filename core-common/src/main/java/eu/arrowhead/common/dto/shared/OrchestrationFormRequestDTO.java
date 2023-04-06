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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.exception.BadPayloadException;

public class OrchestrationFormRequestDTO implements Serializable {
	
	public static final String QOS_REQUIREMENT_MAXIMUM_RESPONSE_TIME_THRESHOLD = "qosMaxRespTimeThreshold"; // in milliseconds, it means the maximum value of the maximum response time
	public static final String QOS_REQUIREMENT_AVERAGE_RESPONSE_TIME_THRESHOLD = "qosAvgRespTime"; // in milliseconds, it means the maximum value of the average (mean) response time 
	public static final String QOS_REQUIREMENT_JITTER_THRESHOLD = "qosJitterThreshold"; // in milliseconds, it mean the maximum acceptable jitter
	public static final String QOS_REQUIREMENT_MAXIMUM_RECENT_PACKET_LOSS = "qosMaxRecentPacketLoss"; // in percent, for example 20% is 20
 	public static final String QOS_REQUIREMENT_MAXIMUM_PACKET_LOSS = "qosMaxPacketLost"; // in percent
	
	public static final String QOS_COMMAND_EXCLUSIVITY = "qosExclusivity"; // in seconds

	//=================================================================================================
	// members

	private static final long serialVersionUID = 5875552294338921238L;
	
	private SystemRequestDTO requesterSystem;
	private CloudRequestDTO requesterCloud;
	private ServiceQueryFormDTO requestedService;
	private OrchestrationFlags orchestrationFlags = new OrchestrationFlags();
	private List<PreferredProviderDataDTO> preferredProviders = new ArrayList<>();
	private Map<String,String> commands = new HashMap<>();
	private Map<String,String> qosRequirements = new HashMap<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public OrchestrationFormRequestDTO() {}

	//-------------------------------------------------------------------------------------------------	
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public OrchestrationFlags getOrchestrationFlags() { return orchestrationFlags; }
	public List<PreferredProviderDataDTO> getPreferredProviders() { return preferredProviders; }
	public Map<String,String> getCommands() { return commands; }
	public Map<String,String> getQosRequirements() { return qosRequirements; }

	//-------------------------------------------------------------------------------------------------	
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setOrchestrationFlags(final Map<String,Boolean> orchestrationFlags) { this.orchestrationFlags = new OrchestrationFlags(orchestrationFlags); }
	
	//-------------------------------------------------------------------------------------------------
	public void setCommands(final Map<String,String> commands) { 
		if (commands != null) {
			this.commands = commands;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredProviders(final List<PreferredProviderDataDTO> preferredProviders) {
		if (preferredProviders != null) {
			this.preferredProviders = preferredProviders;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setQosRequirements(final Map<String,String> qosRequirements) {
		if (qosRequirements != null) {
			this.qosRequirements = qosRequirements;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationFormRequestDTO validateCrossParameterConstraints() {
        if (requestedService == null && orchestrationFlags.get(Flag.OVERRIDE_STORE)) {
            throw new BadPayloadException("Requested service can not be null when \"" + Flag.OVERRIDE_STORE + "\" is TRUE");
        }
        
        if (requestedService == null && orchestrationFlags.get(Flag.TRIGGER_INTER_CLOUD)) {
            throw new BadPayloadException("Requested service can not be null when \"" + Flag.TRIGGER_INTER_CLOUD + "\" is TRUE");
        }

        if (orchestrationFlags.get(Flag.ONLY_PREFERRED)) {
        	for (final Iterator<PreferredProviderDataDTO> it = preferredProviders.iterator(); it.hasNext();) {
				final PreferredProviderDataDTO provider = it.next();
				if (orchestrationFlags.get(Flag.ENABLE_INTER_CLOUD) && !provider.isValid()) {
					it.remove();
				} else if (!provider.isLocal()) {
					it.remove();
				}
			}
        	
            if (preferredProviders.isEmpty()) {
                throw new BadPayloadException("There is no valid preferred provider, but \"" + Flag.ONLY_PREFERRED + "\" is set to true");
            }
        }
        
        if (orchestrationFlags.get(Flag.ENABLE_QOS) && commands.containsKey(QOS_COMMAND_EXCLUSIVITY) && !orchestrationFlags.get(Flag.MATCHMAKING)) {
        	throw new BadPayloadException("Exclusive service orchestration is only possible when \"" + Flag.MATCHMAKING + "\" is set to true");
        }

        return this;
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
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationFormRequestDTO(final Builder builder) {
		this.requesterSystem = builder.requesterSystem;
		this.requesterCloud = builder.requesterCloud;
		this.requestedService = builder.requestedService;
		this.orchestrationFlags = builder.orchestrationFlags;
		this.preferredProviders = builder.preferredProviders;
		this.setCommands(builder.commands);
		this.setQosRequirements(builder.qosRequirements);
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	public static class Builder {
		
		//=================================================================================================
		// members
		
		private final SystemRequestDTO requesterSystem;
		private CloudRequestDTO requesterCloud;
		private ServiceQueryFormDTO requestedService;
		private final OrchestrationFlags orchestrationFlags = new OrchestrationFlags();
		private List<PreferredProviderDataDTO> preferredProviders = new ArrayList<>();
		private Map<String,String> commands = new HashMap<>();
		private Map<String,String> qosRequirements = new HashMap<>();

		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public Builder(final SystemRequestDTO requesterSystem) {
			Assert.notNull(requesterSystem, "requesterSystem is null.");
			this.requesterSystem = requesterSystem;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder requesterCloud(final CloudRequestDTO requesterCloud) {
			this.requesterCloud = requesterCloud;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder requestedService(final ServiceQueryFormDTO requestedService) {
			this.requestedService = requestedService;
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public Builder flag(final Flag flag, final boolean value) {
			this.orchestrationFlags.put(flag, value);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder flags(final OrchestrationFlags flags) {
			this.orchestrationFlags.putAll(flags);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder preferredProviders(final PreferredProviderDataDTO... preferredProviders) {
			this.preferredProviders = preferredProviders == null || preferredProviders.length == 0 ? List.of() : Arrays.asList(preferredProviders);
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder commands(final Map<String,String> commands) {
			this.commands = commands;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder command(final String key, final String value) {
			Assert.isTrue(key != null && !key.isBlank(), "Key is null or blank");
			
			if (this.commands == null) {
				this.commands = new HashMap<>();
			}
			this.commands.put(key, value);
			
			return this;

		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder qosRequirements(final Map<String,String> qosRequirements) {
			this.qosRequirements = qosRequirements;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder qosRequirement(final String key, final String value) {
			Assert.isTrue(key != null && !key.isBlank(), "Key is null or blank");
			
			if (this.qosRequirements == null) {
				this.qosRequirements = new HashMap<>();
			}
			this.qosRequirements.put(key, value);
			
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public OrchestrationFormRequestDTO build() {
			return new OrchestrationFormRequestDTO(this).validateCrossParameterConstraints();
		}
	}
}