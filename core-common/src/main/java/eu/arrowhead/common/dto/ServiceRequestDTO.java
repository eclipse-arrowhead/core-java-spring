package eu.arrowhead.common.dto;

import java.io.Serializable;

public class ServiceRequestDTO implements Serializable {

	private static final long serialVersionUID = -1966787184376371095L;
	
	//=================================================================================================
	// members
		
	private String serviceDefinition;
			
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public ServiceRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRequestDTO(final String serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
}
