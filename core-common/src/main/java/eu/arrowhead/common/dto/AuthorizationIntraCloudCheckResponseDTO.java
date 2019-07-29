package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationIntraCloudCheckResponseDTO implements Serializable {

	private static final long serialVersionUID = 8163618094258979330L;
	
	//=================================================================================================
	// members
	
	//=================================================================================================
	// members
	
	private Long consumerId;
	private Long serviceDefinitionId;
	private List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO() {}
	
	public AuthorizationIntraCloudCheckResponseDTO(final Long consumerId, final Long serviceDefinitionId, final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.consumerId = consumerId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}
	//-------------------------------------------------------------------------------------------------
	
	public Long getConsumerId() {return consumerId;}
	public Long getServiceDefinitionId() {return serviceDefinitionId;}
	public List<IdIdListDTO> getAuthorizedProviderIdsWithInterfaceIds() {return authorizedProviderIdsWithInterfaceIds;}
	//-------------------------------------------------------------------------------------------------

	public void setConsumerId(final Long consumerId) {this.consumerId = consumerId;}
	public void setServiceDefinitionId(final Long serviceDefinitionId) {this.serviceDefinitionId = serviceDefinitionId;}
	public void setAuthorizedProviderIdsWithInterfaceIds(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;}	
}
