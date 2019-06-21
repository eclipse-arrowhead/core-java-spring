package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class IntraCloudAuthorizationRequestDTO implements Serializable {

	private static final long serialVersionUID = 1322804880331971340L;
	
	//=================================================================================================
	// members
	
	private Long consumerId;
	private List<Long> providerIds;
	private List<Long> serviceDefinitionIds;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationRequestDTO() {}
	
	public IntraCloudAuthorizationRequestDTO(final Long consumerId, final List<Long> providerIds, final List<Long> serviceDefinitionIds) {
		this.consumerId = consumerId;
		this.providerIds = providerIds;
		this.serviceDefinitionIds = serviceDefinitionIds;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getConsumerId() {return consumerId;}
	public List<Long> getProviderIds() {return providerIds;}
	public List<Long> getServiceDefinitionIds() {return serviceDefinitionIds;}

	//-------------------------------------------------------------------------------------------------
	public void setConsumerId(final Long consumerId) {this.consumerId = consumerId;}
	public void setProviderIds(final List<Long> providerIds) {this.providerIds = providerIds;}
	public void setServiceDefinitionIds(final List<Long> serviceDefinitionIds) {this.serviceDefinitionIds = serviceDefinitionIds;}	
}
