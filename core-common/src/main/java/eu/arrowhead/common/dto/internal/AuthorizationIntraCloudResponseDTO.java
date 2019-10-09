package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class AuthorizationIntraCloudResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8834973165624838555L;
	
	private long id;
	private SystemResponseDTO consumerSystem;
	private SystemResponseDTO providerSystem; 
	private ServiceDefinitionResponseDTO serviceDefinition;
	private List<ServiceInterfaceResponseDTO> interfaces;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public AuthorizationIntraCloudResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudResponseDTO(final long id, final SystemResponseDTO consumerSystem, final SystemResponseDTO providerSystem, final ServiceDefinitionResponseDTO serviceDefinition,
											  final List<ServiceInterfaceResponseDTO> interfaces, final String createdAt, final String updatedAt) {
		this.id = id;
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.serviceDefinition = serviceDefinition;
		this.interfaces = interfaces;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public SystemResponseDTO getConsumerSystem() { return consumerSystem; }
	public SystemResponseDTO getProviderSystem() { return providerSystem; }
	public ServiceDefinitionResponseDTO getServiceDefinition() { return serviceDefinition; }
	public List<ServiceInterfaceResponseDTO> getInterfaces() { return interfaces; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setConsumerSystem(final SystemResponseDTO consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final SystemResponseDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setInterfaces(final List<ServiceInterfaceResponseDTO> interfaces) { this.interfaces = interfaces; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}