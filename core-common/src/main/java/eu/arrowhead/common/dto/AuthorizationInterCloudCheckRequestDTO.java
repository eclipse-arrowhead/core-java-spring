package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationInterCloudCheckRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -263209252842507399L;
	
	private CloudRequestDTO cloud;
	private String serviceDefinitionId;
	private List<IdIdListDTO> providerIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO(final CloudRequestDTO cloud, final String serviceDefinitionId, final List<IdIdListDTO> providerIdsWithInterfaceIds) {
		this.cloud = cloud;
		this.serviceDefinitionId = serviceDefinitionId;
		this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudRequestDTO getCloud() { return cloud; }
	public String getServiceDefinition() { return serviceDefinitionId; }
	public List<IdIdListDTO> getProviderIdsWithInterfaceIds() { return providerIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudRequestDTO cloud) { this.cloud = cloud; }
	public void setServiceDefinition(final String serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setProviderIdsWithInterfaceIds(final List<IdIdListDTO> providerIdsWithInterfaceIds) { this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds; }
}