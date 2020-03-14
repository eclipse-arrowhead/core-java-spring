package eu.arrowhead.core.qos.manager.impl;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class QoSVerificationParameters {
	
	//=================================================================================================
	// members
	
	private final SystemResponseDTO providerSystem;
	private final CloudResponseDTO providerCloud;
	private final Map<String,String> metadata;
	private final Map<String,String> qosRequirements;
	private final Map<String,String> commands;
	private final List<OrchestratorWarnings> warnings;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSVerificationParameters(final SystemResponseDTO providerSystem, final CloudResponseDTO providerCloud, final Map<String, String> metadata, final Map<String, String> qosRequirements,
									 final Map<String, String> commands, final List<OrchestratorWarnings> warnings) {
		Assert.notNull(providerSystem, "providerSystem is null");
		
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.metadata = metadata;
		this.qosRequirements = qosRequirements;
		this.commands = commands;
		this.warnings = warnings;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getProviderSystem() { return providerSystem; }
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public Map<String, String> getMetadata() { return metadata; }
	public Map<String, String> getQosRequirements() { return qosRequirements; }
	public Map<String, String> getCommands() { return commands; }
	public List<OrchestratorWarnings> getWarnings() { return warnings; }
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInterCloud() {
		return providerCloud != null;
	}	
}
