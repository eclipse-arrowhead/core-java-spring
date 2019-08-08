package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6651144276576501218L;
	
	private ServiceQueryFormDTO requestedService;
	private List<CloudWithRelaysResponseDTO> cloudBoundaries;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<CloudWithRelaysResponseDTO> cloudBoundaries) {
		this.requestedService = requestedService;
		this.cloudBoundaries = cloudBoundaries;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<CloudWithRelaysResponseDTO> getCloudBoundaries() { return cloudBoundaries; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setCloudBoundaries(final List<CloudWithRelaysResponseDTO> cloudBoundaries) { this.cloudBoundaries = cloudBoundaries; }	
}
