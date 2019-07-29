package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationInterCloudCheckResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1838972483889050448L;
	
	private long cloudId;
	private long serviceDefinitionId;
	private List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckResponseDTO(final long cloudId, final long serviceDefinitionId, final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.cloudId = cloudId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public long getCloudId() { return cloudId; }
	public long getServiceDefinitionId() { return serviceDefinitionId; }
	public List<IdIdListDTO> getAuthorizedProviderIdsWithInterfaceIds() { return authorizedProviderIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final long cloudId) { this.cloudId = cloudId; }
	public void setServiceDefinitionId(final long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; } 
	public void setAuthorizedProviderIdsWithInterfaceIds(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}	
}