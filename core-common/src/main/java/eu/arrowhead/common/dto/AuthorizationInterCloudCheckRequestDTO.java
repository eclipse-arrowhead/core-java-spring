package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class AuthorizationInterCloudCheckRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -263209252842507399L;
	
	private Long cloudId;
	private Long serviceDefinitionId;
	private List<IdIdListDTO> providerIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudCheckRequestDTO(final Long cloudId, final Long serviceDefinitionId, final List<IdIdListDTO> providerIdsWithInterfaceIds) {
		this.cloudId = cloudId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() { return cloudId; }
	public Long getServiceDefinitionId() { return serviceDefinitionId; }
	public List<IdIdListDTO> getProviderIdsWithInterfaceIds() { return providerIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final Long cloudId) { this.cloudId = cloudId; }
	public void setServiceDefinitionId(final Long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setProviderIdsWithInterfaceIds(final List<IdIdListDTO> providerIdsWithInterfaceIds) { this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds; }
}