package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;

public class IntraCloudAuthorizationCheckResponseDTO implements Serializable {

	private static final long serialVersionUID = 8163618094258979330L;
	
	//=================================================================================================
	// members
	
	private Long consumerId;
	private Long serviceDefinitionId;
	private Map<Long, Boolean> providerIdAuthorizationState;
	

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationCheckResponseDTO() {}

	public IntraCloudAuthorizationCheckResponseDTO(final Long consumerId, final Long serviceDefinitionId, final Map<Long, Boolean> providerIdAuthorizationState) {
		this.consumerId = consumerId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.providerIdAuthorizationState = providerIdAuthorizationState;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getConsumerId() {return consumerId;}
	public Long getServiceDefinitionId() {return serviceDefinitionId;}
	public Map<Long, Boolean> getProviderIdAuthorizationState() {return providerIdAuthorizationState;}

	//-------------------------------------------------------------------------------------------------
	public void setConsumerId(final Long consumerId) {this.consumerId = consumerId;}
	public void setServiceDefinitionId(final Long serviceDefinitionId) {this.serviceDefinitionId = serviceDefinitionId;} 
	public void setProviderIdAuthorizationState(final Map<Long, Boolean> providerIdAuthorizationState) {this.providerIdAuthorizationState = providerIdAuthorizationState;}	
}
