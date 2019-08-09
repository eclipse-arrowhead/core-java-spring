package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6651144276576501218L;
	
	private ServiceQueryFormDTO requestedService;
	private List<Long> cloudIdBoundaries;
	private boolean gatewayIsMandatory = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<Long> cloudIdBoundaries, final boolean gatewayIsMandatory) {
		this.requestedService = requestedService;
		this.cloudIdBoundaries = cloudIdBoundaries;
		this.gatewayIsMandatory = gatewayIsMandatory;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<Long> getCloudIdBoundaries() { return cloudIdBoundaries; }
	public boolean isGatewayIsMandatory() { return gatewayIsMandatory; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setCloudIdBoundaries(final List<Long> cloudIdBoundaries) { this.cloudIdBoundaries = cloudIdBoundaries; }
	public void setGatewayIsMandatory(final boolean gatewayIsMandatory) { this.gatewayIsMandatory = gatewayIsMandatory; }	
}
