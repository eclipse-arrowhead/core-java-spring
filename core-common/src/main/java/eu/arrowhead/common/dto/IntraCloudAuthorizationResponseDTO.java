package eu.arrowhead.common.dto;

import java.io.Serializable;

public class IntraCloudAuthorizationResponseDTO implements Serializable {

	private static final long serialVersionUID = 8834973165624838555L;

	//=================================================================================================
	// members
	
	private long id;
	private SystemResponseDTO consumerSystem;
	private SystemResponseDTO providerSystem; 
	private ServiceDefinitionResponseDTO serviceDefinition;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public IntraCloudAuthorizationResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public IntraCloudAuthorizationResponseDTO(final long id, final SystemResponseDTO consumerSystem, final SystemResponseDTO providerSystem,
			final ServiceDefinitionResponseDTO serviceDefinition, final String createdAt, final String updatedAt) {
		this.id = id;
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.serviceDefinition = serviceDefinition;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getId() {return id;}
	public SystemResponseDTO getConsumerSystem() {return consumerSystem;}
	public SystemResponseDTO getProviderSystem() {return providerSystem;}
	public ServiceDefinitionResponseDTO getServiceDefinition() {return serviceDefinition;}
	public String getCreatedAt() {return createdAt;}
	public String getUpdatedAt() {return updatedAt;}

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) {this.id = id;}
	public void setConsumerSystem(final SystemResponseDTO consumerSystem) {this.consumerSystem = consumerSystem;}
	public void setProviderSystem(final SystemResponseDTO providerSystem) {this.providerSystem = providerSystem;}
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) {this.serviceDefinition = serviceDefinition;}
	public void setCreatedAt(final String createdAt) {this.createdAt = createdAt;}
	public void setUpdatedAt(final String updatedAt) {this.updatedAt = updatedAt;}
}
