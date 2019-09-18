package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class ServiceDefinitionRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1966787184376371095L;
	
	private String serviceDefinition;
			
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO(final String serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
}