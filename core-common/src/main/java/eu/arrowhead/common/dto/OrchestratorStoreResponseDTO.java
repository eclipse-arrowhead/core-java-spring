package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;

public class OrchestratorStoreResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -6099079027561380554L;

	private long id;
	private ServiceDefinition serviceDefinition;	
	private System consumerSystem;	
	private System providerSystem; 	
	private Cloud providerCloud;	
	private Integer priority;	
	private String attribute;	
	private ZonedDateTime createdAt;	
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO(long id, ServiceDefinition serviceDefinition, System consumerSystem,
			System providerSystem, Cloud providerCloud, Integer priority, String attribute, ZonedDateTime createdAt,
			ZonedDateTime updatedAt) {
		super();
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
	public ServiceDefinition getServiceDefinition() { return serviceDefinition; }
	public System getConsumerSystem() { return consumerSystem; }
	public System getProviderSystem() { return providerSystem; }
	public Cloud getProviderCloud() { return providerCloud; }
	public Integer getPriority() { return priority; }
	public String getAttribute() { return attribute; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(long id) { this.id = id; }
	public void setServiceDefinition(ServiceDefinition serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(System consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(System providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(Cloud providerCloud) { this.providerCloud = providerCloud; }
	public void setPriority(Integer priority) { this.priority = priority; }
	public void setAttribute(String attribute) { this.attribute = attribute; }
	public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

}
