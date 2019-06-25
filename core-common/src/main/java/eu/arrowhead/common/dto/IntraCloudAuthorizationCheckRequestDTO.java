package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class IntraCloudAuthorizationCheckRequestDTO implements Serializable {

	private static final long serialVersionUID = 6704709511013220348L;
	
	//=================================================================================================
	// members
	
	private Long consumerId;
	private Long serviceDefinitionId;
	private List<Long> providerIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationCheckRequestDTO() {}
	
	public IntraCloudAuthorizationCheckRequestDTO(final Long consumerId, final Long serviceDefinitionId, final List<Long> providerIds) {
		this.consumerId = consumerId;
		this.serviceDefinitionId = serviceDefinitionId;
		this.providerIds = providerIds;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getConsumerId() {return consumerId;}
	public Long getServiceDefinitionId() {return serviceDefinitionId;}
	public List<Long> getProviderIds() {return providerIds;}

	//-------------------------------------------------------------------------------------------------
	public void setConsumerId(final Long consumerId) {this.consumerId = consumerId;}
	public void setServiceDefinitionId(final Long serviceDefinitionId) {this.serviceDefinitionId = serviceDefinitionId;}
	public void setProviderIds(final List<Long> providerIds) {this.providerIds = providerIds;}		
}
