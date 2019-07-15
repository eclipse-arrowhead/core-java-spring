package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

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
	private String attribute;	
	private ZonedDateTime createdAt;	
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO(final long id, final ServiceDefinitionResponseDTO serviceDefinition, final SystemResponseDTO consumerSystem,
			final SystemResponseDTO providerSystem, final CloudResponseDTO providerCloud, final Integer priority, final String attribute, final ZonedDateTime createdAt,
			final ZonedDateTime updatedAt) {
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
	public String getAttribute() { return attribute; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(final SystemResponseDTO consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final SystemResponseDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final CloudResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setPriority(final Integer priority) { this.priority = priority; }
	public void setAttribute(final String attribute) { this.attribute = attribute; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

}
