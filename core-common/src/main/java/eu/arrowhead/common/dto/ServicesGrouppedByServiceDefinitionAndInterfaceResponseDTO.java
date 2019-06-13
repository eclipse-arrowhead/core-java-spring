package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO implements Serializable {

	private static final long serialVersionUID = 8564324985631020025L;
	
	//=================================================================================================
	// members
	
	private long serviceDefinitionId;
	private String serviceDefinition;
	private String interfaceName;
	private List<ServiceRegistryResponseDTO> providerServices;
			
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO() {}
	
	public ServicesGrouppedByServiceDefinitionAndInterfaceResponseDTO(final long serviceDefinitionId, final String serviceDefinition, final String interfaceName, final List<ServiceRegistryResponseDTO> providerServices) {
		this.serviceDefinitionId = serviceDefinitionId;
		this.serviceDefinition = serviceDefinition;
		this.interfaceName = interfaceName;
		this.providerServices = providerServices;
	}

	//-------------------------------------------------------------------------------------------------
	public long getServiceDefinitionId() {return serviceDefinitionId;}
	public String getServiceDefinition() {return serviceDefinition;}
	public String getInterfaceName() {return interfaceName;}
	public List<ServiceRegistryResponseDTO> getProviderServices() {return providerServices;}	
}
