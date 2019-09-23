package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

@JsonInclude(Include.NON_NULL)
public class ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8564324985631020025L;
	
	private long serviceDefinitionId;
	private String serviceDefinition;
	private String interfaceName;
	private List<ServiceRegistryResponseDTO> providerServices;
			
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGroupedByServiceDefinitionAndInterfaceResponseDTO(final long serviceDefinitionId, final String serviceDefinition, final String interfaceName,
																	 final List<ServiceRegistryResponseDTO> providerServices) {
		this.serviceDefinitionId = serviceDefinitionId;
		this.serviceDefinition = serviceDefinition;
		this.interfaceName = interfaceName;
		this.providerServices = providerServices;
	}

	//-------------------------------------------------------------------------------------------------
	public long getServiceDefinitionId() { return serviceDefinitionId; }
	public String getServiceDefinition() { return serviceDefinition; }
	public String getInterfaceName() { return interfaceName; }
	public List<ServiceRegistryResponseDTO> getProviderServices() { return providerServices; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionId(final long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setInterfaceName(final String interfaceName) { this.interfaceName = interfaceName; }
	public void setProviderServices(final List<ServiceRegistryResponseDTO> providerServices) { this.providerServices = providerServices; }	
}