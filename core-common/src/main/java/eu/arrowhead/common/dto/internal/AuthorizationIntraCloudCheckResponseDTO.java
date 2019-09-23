package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class AuthorizationIntraCloudCheckResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7220577872110448998L;
	
	private SystemResponseDTO consumer;
	private long serviceDefinitionId;
	private List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO(final SystemResponseDTO consumer, final long serviceDefinitionId, final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.consumer = consumer;
		this.serviceDefinitionId = serviceDefinitionId;
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getConsumer() { return consumer; }
	public Long getServiceDefinitionId() { return serviceDefinitionId; }
	public List<IdIdListDTO> getAuthorizedProviderIdsWithInterfaceIds() { return authorizedProviderIdsWithInterfaceIds; }
	
	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemResponseDTO consumer) { this.consumer = consumer; }
	public void setServiceDefinitionId(final Long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setAuthorizedProviderIdsWithInterfaceIds(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) { 
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}	
}