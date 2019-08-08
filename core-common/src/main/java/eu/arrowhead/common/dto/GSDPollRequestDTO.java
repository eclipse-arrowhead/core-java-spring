package eu.arrowhead.common.dto;

import java.io.Serializable;

public class GSDPollRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3327546226003685713L;
	
	private ServiceQueryFormDTO requestedService;
	private CloudRequestDTO requesterCloud;	
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollRequestDTO() {} 	
	
	public GSDPollRequestDTO(final ServiceQueryFormDTO requestedService, final CloudRequestDTO requesterCloud) {
		this.requestedService = requestedService;
		this.requesterCloud = requesterCloud;
	}

	//-------------------------------------------------------------------------------------------------	
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	
	//-------------------------------------------------------------------------------------------------	
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }	
}