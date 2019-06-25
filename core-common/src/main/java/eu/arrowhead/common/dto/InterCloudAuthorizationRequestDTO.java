package eu.arrowhead.common.dto;

import java.io.Serializable;

public class InterCloudAuthorizationRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5349360773532348565L;
	
	private CloudRequestDTO cloud; 
	private ServiceDefinitionRequestDTO serviceDefinition;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudAuthorizationRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public InterCloudAuthorizationRequestDTO(CloudRequestDTO cloud,
			ServiceDefinitionRequestDTO serviceDefinition) {

		this.cloud = cloud;
		this.serviceDefinition = serviceDefinition;
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudRequestDTO getCloud() { return cloud; }
	public ServiceDefinitionRequestDTO getServiceDefinition() { return serviceDefinition;	}
	
	//-------------------------------------------------------------------------------------------------
	public void setCloud(CloudRequestDTO cloud) { this.cloud = cloud; }
	public void setServiceDefinition(ServiceDefinitionRequestDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
}



