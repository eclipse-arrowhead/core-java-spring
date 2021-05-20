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

package eu.arrowhead.core.qos.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class QoSVerificationParameters {
	
	//=================================================================================================
	// members
	
	private final SystemResponseDTO providerSystem;
	private final CloudResponseDTO providerCloud;
	private final boolean gatewayIsMandatory;
	private final Map<String,String> metadata;
	private final Map<String,String> qosRequirements;
	private final Map<String,String> commands;
	private final List<OrchestratorWarnings> warnings;
	
	private QoSIntraPingMeasurementResponseDTO localReferencePingMeasurement;
	private QoSMeasurementAttributesFormDTO providerTargetCloudMeasurement;
	private final Set<RelayResponseDTO> verifiedRelays = new HashSet<>(); //to be filled during the pre-verification process
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSVerificationParameters(final SystemResponseDTO providerSystem, final CloudResponseDTO providerCloud, final boolean gatewayIsMandatory, final Map<String, String> metadata,
									 final Map<String, String> qosRequirements, final Map<String, String> commands, final List<OrchestratorWarnings> warnings) {
		Assert.notNull(providerSystem, "providerSystem is null");
		
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.gatewayIsMandatory = gatewayIsMandatory;
		this.metadata = metadata;
		this.qosRequirements = qosRequirements;
		this.commands = commands;
		this.warnings = warnings;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getProviderSystem() { return providerSystem; }
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public boolean isGatewayIsMandatory() { return gatewayIsMandatory; }
	public Map<String, String> getMetadata() { return metadata; }
	public Map<String, String> getQosRequirements() { return qosRequirements; }
	public Map<String, String> getCommands() { return commands; }
	public List<OrchestratorWarnings> getWarnings() { return warnings; }	
	public QoSIntraPingMeasurementResponseDTO getLocalReferencePingMeasurement() { return localReferencePingMeasurement; }
	public QoSMeasurementAttributesFormDTO getProviderTargetCloudMeasurement() { return providerTargetCloudMeasurement; }
	public Set<RelayResponseDTO> getVerifiedRelays() { return verifiedRelays; }

	//-------------------------------------------------------------------------------------------------
	public void setLocalReferencePingMeasurement(final QoSIntraPingMeasurementResponseDTO localReferencePingMeasurement) { this.localReferencePingMeasurement = localReferencePingMeasurement; }
	public void setProviderTargetCloudMeasurement(final QoSMeasurementAttributesFormDTO providerTargetCloudMeasurement) { this.providerTargetCloudMeasurement = providerTargetCloudMeasurement; }

	//-------------------------------------------------------------------------------------------------
	public boolean isInterCloud() {
		return providerCloud != null;
	}	
	
	//-------------------------------------------------------------------------------------------------
	public void validateParameters() {
		Assert.notNull(providerSystem, "Provider is null");
		Assert.notNull(metadata, "Metadata is null");
		Assert.notNull(qosRequirements, "QoS requirements is null");
		Assert.notNull(commands, "Commands is null");
		Assert.notNull(warnings, "Warnings is null");
		if (gatewayIsMandatory) {
			Assert.notNull(providerCloud, "Provider cloud is null while gateway is mandatory");
			Assert.notNull(localReferencePingMeasurement, "Gateway is manadtory while localReferencePingMeasurement is null");
			Assert.notNull(providerTargetCloudMeasurement, "Gateway is manadtory while providerTargetCloudMeasurement is null");			
		}
	}
}
