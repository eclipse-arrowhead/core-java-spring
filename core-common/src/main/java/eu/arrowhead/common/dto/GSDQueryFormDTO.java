package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -6651144276576501218L;
	
	private ServiceQueryFormDTO requestedService;
	private List<CloudRequestDTO> preferredCloud;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<CloudRequestDTO> preferredCloud) {
		this.requestedService = requestedService;
		this.preferredCloud = preferredCloud;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<CloudRequestDTO> getPreferredCloud() { return preferredCloud; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setPreferredCloud(final List<CloudRequestDTO> preferredCloud) { this.preferredCloud = preferredCloud; }
}	