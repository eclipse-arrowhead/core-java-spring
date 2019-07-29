package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationInterCloudCheckResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1838972483889050448L;
	
	private CloudResponseDTO cloud;
	private String serviceDefinition;
	private List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckResponseDTO(final CloudResponseDTO cloud, final String serviceDefinition, final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.cloud = cloud;
		this.serviceDefinition = serviceDefinition;
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public String getServiceDefinition() { return serviceDefinition; }
	public List<IdIdListDTO> getAuthorizedProviderIdsWithInterfaceIds() { return authorizedProviderIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; } 
	public void setAuthorizedProviderIdsWithInterfaceIds(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}	
}