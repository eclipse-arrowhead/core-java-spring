package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ICNProposalRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8916389174595364064L;
	
	private ServiceQueryFormDTO requestedService;
	private CloudRequestDTO requesterCloud;
	private SystemRequestDTO requesterSystem;
	private List<SystemRequestDTO> preferredSystems = new ArrayList<>();
	private List<RelayRequestDTO> preferredGatewayRelays = new ArrayList<>();
	private OrchestrationFlags negotiationFlags = new OrchestrationFlags();
	private boolean useGateway = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public List<SystemRequestDTO> getPreferredSystems() { return preferredSystems; }
	public List<RelayRequestDTO> getPreferredGatewayRelays() { return preferredGatewayRelays; }
	public OrchestrationFlags getNegotiationFlags() { return negotiationFlags; }
	public boolean isUseGateway() { return useGateway; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setUseGateway(final boolean useGateway) { this.useGateway = useGateway; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredSystems(final List<SystemRequestDTO> preferredSystems) {
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredGatewayRelays(final List<RelayRequestDTO> preferredGatewayRelays) {
		if (preferredGatewayRelays != null) {
			this.preferredGatewayRelays = preferredGatewayRelays;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setNegotiationFlags(final OrchestrationFlags negotiationFlags) {
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}
	}
}