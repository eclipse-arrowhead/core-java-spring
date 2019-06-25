package eu.arrowhead.common.dto;

import java.io.Serializable;

public class InterCloudAuthorizationResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 840542120891817637L;
	
	private long id;
	private CloudResponseDTO cloud; 
	private ServiceDefinitionResponseDTO serviceDefinition;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudAuthorizationResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public InterCloudAuthorizationResponseDTO(long id, CloudResponseDTO cloud,
			ServiceDefinitionResponseDTO serviceDefinition, String createdAt, String updatedAt) {
		this.id = id;
		this.cloud = cloud;
		this.serviceDefinition = serviceDefinition;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;}
	public CloudResponseDTO getCloud() { return cloud; }
	public ServiceDefinitionResponseDTO getServiceDefinition() { return serviceDefinition;	}
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(long id) { this.id = id; }
	public void setCloud(CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setServiceDefinition(ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }	

}
