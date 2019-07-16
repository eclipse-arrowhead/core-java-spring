package eu.arrowhead.common.dto;

import java.io.Serializable;

public class AuthorizationInterCloudResponseDTO implements Serializable {

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
	public AuthorizationInterCloudResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public AuthorizationInterCloudResponseDTO(final long id, final CloudResponseDTO cloud,
			final ServiceDefinitionResponseDTO serviceDefinition, final String createdAt, final String updatedAt) {
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
	public void setId(final long id) { this.id = id; }
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }	

}
