package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;

public class OrchestratorStoreResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -6099079027561380554L;

	private long id;
	private ServiceDefinitionResponseDTO serviceDefinition;	
	private SystemResponseDTO consumerSystem;	
	private SystemResponseDTO providerSystem; 	
	private CloudResponseDTO providerCloud;	
	private Integer priority;	
	private Map<String,String> attribute;	
	private String createdAt;	
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO(final long id, final ServiceDefinitionResponseDTO serviceDefinition, final SystemResponseDTO consumerSystem,
			final SystemResponseDTO providerSystem, final CloudResponseDTO providerCloud, final Integer priority, final Map<String,String> attribute, final String createdAt,
			final String updatedAt) {
		this.id = id;
		this.serviceDefinition = serviceDefinition;
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.priority = priority;
		this.attribute = attribute;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ServiceDefinitionResponseDTO getServiceDefinition() { return serviceDefinition; }
	public SystemResponseDTO getConsumerSystem() { return consumerSystem; }
	public SystemResponseDTO getProviderSystem() { return providerSystem; }
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public Integer getPriority() { return priority; }
	public Map<String,String> getAttribute() { return attribute; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(final SystemResponseDTO consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final SystemResponseDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final CloudResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setPriority(final Integer priority) { this.priority = priority; }
	public void setAttribute(final Map<String,String> attribute) { this.attribute = attribute; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

}
