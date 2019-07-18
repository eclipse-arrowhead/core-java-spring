package eu.arrowhead.common.dto;

import java.io.Serializable;

public class AuthorizationInterCloudCheckRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -263209252842507399L;
	private Long cloudId;
	private Long serviceDefinitionId;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO() {}
	
	public AuthorizationInterCloudCheckRequestDTO(final Long cloudId, final Long serviceDefinitionId) {
		this.cloudId = cloudId;
		this.serviceDefinitionId = serviceDefinitionId;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() {return cloudId;}
	public Long getServiceDefinitionId() {return serviceDefinitionId;}

	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final Long cloudId) {this.cloudId = cloudId;}
	public void setServiceDefinitionId(final Long serviceDefinitionId) {this.serviceDefinitionId = serviceDefinitionId;}
}