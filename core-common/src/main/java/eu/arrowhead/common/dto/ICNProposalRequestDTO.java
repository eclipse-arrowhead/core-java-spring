package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

public class ICNProposalRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -644090196952055587L;
	
	private ServiceQueryFormDTO requestedService;
	private CloudRequestDTO requesterCloud;
	private SystemRequestDTO requesterSystem;
	private List<SystemRequestDTO> preferredSystems = new ArrayList<>();
	private List<RelayRequestDTO> preferredGatewayRelays = new ArrayList<>();
	private OrchestrationFlags negotiationFlags = new OrchestrationFlags();
	private boolean gatewayIsPresent = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalRequestDTO(final ServiceQueryFormDTO requestedService, final CloudRequestDTO requesterCloud, final SystemRequestDTO requesterSystem, 
							     final List<SystemRequestDTO> preferredSystems, final List<RelayRequestDTO> preferredGatewayRelays, final OrchestrationFlags negotiationFlags,
							     final boolean gatewayIsPresent) {
		Assert.notNull(requestedService, "Requested service is null.");
		Assert.notNull(requesterCloud, "Requester cloud is null.");
		Assert.notNull(requesterSystem, "Requester system is null.");
		
		this.requestedService = requestedService;
		this.requesterCloud = requesterCloud;
		this.requesterSystem = requesterSystem;
		this.gatewayIsPresent = gatewayIsPresent;
		
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
		
		if (preferredGatewayRelays != null) {
			this.preferredGatewayRelays = preferredGatewayRelays;
		}
		
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}

	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; }
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public List<SystemRequestDTO> getPreferredSystems() { return preferredSystems; }
	public List<RelayRequestDTO> getPreferredGatewayRelays() { return preferredGatewayRelays; }
	public OrchestrationFlags getNegotiationFlags() { return negotiationFlags; }
	public boolean getGatewayIsPresent() { return gatewayIsPresent; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setGatewayIsPresent(final boolean gatewayIsPresent) { this.gatewayIsPresent = gatewayIsPresent; }
	
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