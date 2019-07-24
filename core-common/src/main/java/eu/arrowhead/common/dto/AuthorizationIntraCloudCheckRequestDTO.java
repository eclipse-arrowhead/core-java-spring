package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationIntraCloudCheckRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6704709511013220348L;
	
	private Long consumerId;
	private Long serviceDefinitionId;
	private List<IdIdListDTO> providerIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckRequestDTO(final Long consumerId, final Long serviceDefinitionId, final List<IdIdListDTO> providerIdsWithInterfaceIds) {
		this.consumerId = consumerId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getConsumerId() { return consumerId; }
	public Long getServiceDefinitionId() { return serviceDefinitionId; }
	public List<IdIdListDTO> getProviderIdsWithInterfaceIds() { return providerIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumerId(final Long consumerId) { this.consumerId = consumerId; }
	public void setServiceDefinitionId(final Long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setProviderIdsWithInterfaceIds(final List<IdIdListDTO> providerIdsWithInterfaceIds) { this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds; }
}