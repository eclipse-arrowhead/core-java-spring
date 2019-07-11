package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrchestratorFormRequestDTO implements Serializable {


	//=================================================================================================
	// members

	private static final long serialVersionUID = -3595427649312358521L;

	private static final List<String> flagKeys = List.of("triggerInterCloud", "externalServiceRequest", "enableInterCloud",
			"metadataSearch", "pingProviders", "overrideStore", "matchmaking",
			"onlyPreferred", "enableQoS");
	
	private SystemRequestDTO requesterSystem;
	private CloudRequestDTO requesterCloud;
	private String requestedServiceDefinition;
	private List<String> interfaces;
	private Map<String, Boolean> orchestrationFlags = new HashMap<>();
	private List<PreferredProviderDataDTO> preferredProviders = new ArrayList<>();
	private Map<String, String> commands = new HashMap<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public OrchestratorFormRequestDTO() {
		for (String key : flagKeys) {
		  if (!orchestrationFlags.containsKey(key)) {
		    orchestrationFlags.put(key, false);
		  }
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public String getRequestedServiceDefinition() { return requestedServiceDefinition; }
	public List<String> getInterfaces() { return interfaces; }
	public Map<String, Boolean> getOrchestrationFlags() { return orchestrationFlags; }
	public List<PreferredProviderDataDTO> getPreferredProviders() { return preferredProviders; }
	public Map<String, String> getCommands() { return commands; }
	
	//-------------------------------------------------------------------------------------------------	
	public void setRequesterSystem(SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setRequesterCloud(CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequestedServiceDefinition(String requestedServiceDefinition) { this.requestedServiceDefinition = requestedServiceDefinition; }
	public void setInterfaces(List<String> interfaces) { this.interfaces = interfaces; }
	public void setOrchestrationFlags(Map<String, Boolean> orchestrationFlags) { this.orchestrationFlags = orchestrationFlags; }
	public void setPreferredProviders(List<PreferredProviderDataDTO> preferredProviders) { this.preferredProviders = preferredProviders; }
	public void setCommands(Map<String, String> commands) { this.commands = commands; }
}
