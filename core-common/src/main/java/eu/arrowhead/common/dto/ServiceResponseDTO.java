package eu.arrowhead.common.dto;

import java.io.Serializable;

public class ServiceResponseDTO implements Serializable {

	private static final long serialVersionUID = -1087135064729428440L;
	
	//=================================================================================================
	// members
	
	private Long id;
	private String serviceDefinition;
	private String createdAt;
	private String updatedAt;
	
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceResponseDTO(final Long id, final String serviceDefinition, final String createdAt, final String updatedAt) {
		this.id = id;
		this.serviceDefinition = serviceDefinition;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public String getServiceDefinition() { return serviceDefinition; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}
