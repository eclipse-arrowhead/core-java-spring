package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.Map;

public class OrchestratorStoreRequestByIdDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6339446605691363945L;
	
	private Long serviceDefinitionId;
	private Long consumerSystemId;
	private Long providerSystemId;
	private Long cloudId;
	private Long serviceInterfaceId;
	private Integer priority;	
	private Map<String,String> attribute;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestByIdDTO() {}

	public OrchestratorStoreRequestByIdDTO(final Long serviceDefinitionId, final Long consumerSystemId, final Long providerSystemId, final Long cloudId, final Long serviceInterfaceId, final Integer priority, final Map<String,String> attribute) {
		this.serviceDefinitionId = serviceDefinitionId;
		this.consumerSystemId = consumerSystemId;
		this.providerSystemId = providerSystemId;
		this.cloudId = cloudId;
		this.serviceInterfaceId = serviceInterfaceId;
		this.priority = priority;
		this.attribute = attribute;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getServiceDefinitionId() {return serviceDefinitionId;}
	public Long getConsumerSystemId() {return consumerSystemId;}
	public Long getProviderSystemId() {return providerSystemId;}
	public Long getCloudId() {return cloudId;}
	public Long getServiceInterfaceId() {return serviceInterfaceId;}
	public Integer getPriority() {return priority;}
	public Map<String,String> getAttribute() {return attribute;}	

	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionId(final Long serviceDefinitionId) {this.serviceDefinitionId = serviceDefinitionId;}
	public void setConsumerSystemId(final Long consumerSystemId) {this.consumerSystemId = consumerSystemId;}
	public void setProviderSystemId(final Long providerSystemId) {this.providerSystemId = providerSystemId;}
	public void setCloudId(final Long cloudId) {this.cloudId = cloudId;}
	public void setServiceInterfaceId(final Long serviceInterfaceId) {this.serviceInterfaceId = serviceInterfaceId;}
	public void setPriority(final Integer priority) {this.priority = priority;}
	public void setAttribute(final Map<String,String> attribute) {this.attribute = attribute;}
}
