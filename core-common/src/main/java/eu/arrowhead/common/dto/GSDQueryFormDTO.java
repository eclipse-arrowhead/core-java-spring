package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6651144276576501218L;
	
	private ServiceQueryFormDTO requestedService;
	private List<Long> preferredCloudIds;
	private boolean gatewayIsMandatory = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<Long> preferredCloudIds, final boolean gatewayIsMandatory) {
		this.requestedService = requestedService;
		this.preferredCloudIds = preferredCloudIds;
		this.gatewayIsMandatory = gatewayIsMandatory;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<Long> getPreferredCloudIds() { return preferredCloudIds; }
	public boolean isGatewayIsMandatory() { return gatewayIsMandatory; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setPreferredCloudIds(final List<Long> preferredCloudIds) { this.preferredCloudIds = preferredCloudIds; }
	public void setGatewayIsMandatory(final boolean gatewayIsMandatory) { this.gatewayIsMandatory = gatewayIsMandatory; }	
}
