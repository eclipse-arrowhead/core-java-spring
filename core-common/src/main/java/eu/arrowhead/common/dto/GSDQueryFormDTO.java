package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6651144276576501218L;
	
	private ServiceQueryFormDTO requestedService;
	private List<Long> preferredCloudIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<Long> preferredCloudIds) {
		this.requestedService = requestedService;
		this.preferredCloudIds = preferredCloudIds;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<Long> getPreferredCloudIds() { return preferredCloudIds; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setPreferredCloudIds(final List<Long> preferredCloudIds) { this.preferredCloudIds = preferredCloudIds; }
}
