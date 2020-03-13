package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1906800805302171428L;
	
	private ServiceQueryFormDTO requestedService;
	private List<CloudRequestDTO> preferredClouds;
	private boolean needQoSMeasurements;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<CloudRequestDTO> preferredClouds,
						   final boolean needQoSMeasurements) {
		this.requestedService = requestedService;
		this.preferredClouds = preferredClouds;
		this.needQoSMeasurements = needQoSMeasurements;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<CloudRequestDTO> getPreferredClouds() { return preferredClouds; }
	public boolean needQoSMeasurements() { return needQoSMeasurements; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setPreferredClouds(final List<CloudRequestDTO> preferredClouds) { this.preferredClouds = preferredClouds; }
	public void setNeedQoSMeasurements (final boolean needQoSMeasurements) { this.needQoSMeasurements = needQoSMeasurements; }
}