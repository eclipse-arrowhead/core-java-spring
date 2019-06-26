package eu.arrowhead.common.dto;

import java.io.Serializable;

public class InterCloudAuthorizationCheckResponseDTO implements Serializable {


	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1838972483889050448L;
	private long cloudId;
	private long serviceDefinitionId;
	private boolean cloudIdAuthorizationState;
	

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public InterCloudAuthorizationCheckResponseDTO() {}

	public InterCloudAuthorizationCheckResponseDTO(final long cloudId, final long serviceDefinitionId, final boolean cloudIdAuthorizationState) {
		this.cloudId = cloudId;
		this.serviceDefinitionId = serviceDefinitionId;
	}

	//-------------------------------------------------------------------------------------------------
	public long getCloudId() {return cloudId;}
	public long getServiceDefinitionId() {return serviceDefinitionId;}
	public boolean getCloudIdAuthorizationState() {return cloudIdAuthorizationState;}

	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final long cloudId) {this.cloudId = cloudId;}
	public void setServiceDefinitionId(final long serviceDefinitionId) {this.serviceDefinitionId = serviceDefinitionId;} 
	public void setCloudIdAuthorizationState(final boolean cloudIdAuthorizationState) {this.cloudIdAuthorizationState = cloudIdAuthorizationState;}	
}