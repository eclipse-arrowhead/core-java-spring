package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6651144276576501218L;
	
	private ServiceQueryFormDTO requestedService;
	private List<Long> cloudIdBoundaries;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<Long> cloudIdBoundaries) {
		this.requestedService = requestedService;
		this.cloudIdBoundaries = cloudIdBoundaries;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<Long> getCloudIdBoundaries() { return cloudIdBoundaries; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setCloudIsBoundaries(final List<Long> cloudIdBoundaries) { this.cloudIdBoundaries = cloudIdBoundaries; }	
}
