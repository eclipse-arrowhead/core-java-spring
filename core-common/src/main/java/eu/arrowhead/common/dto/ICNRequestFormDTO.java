package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

public class ICNRequestFormDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 2348058628698510590L;
	
	private ServiceQueryFormDTO requestedService;
	private Long targetCloudId;
	private SystemRequestDTO requesterSystem;
	private List<SystemRequestDTO> preferredSystems = new ArrayList<>();
	private OrchestrationFlags negotiationFlags = new OrchestrationFlags();
	private boolean useGateway = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNRequestFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ICNRequestFormDTO(final ServiceQueryFormDTO requestedService, final Long targetCloudId, final SystemRequestDTO requesterSystem, final List<SystemRequestDTO> preferredSystems,
						     final OrchestrationFlags negotiationFlags, final boolean useGateway) {
		Assert.notNull(requestedService, "Requested service is null.");
		Assert.notNull(targetCloudId, "Target cloud id is null.");
		Assert.notNull(requesterSystem, "Requester system is null.");
		
		this.requestedService = requestedService;
		this.targetCloudId = targetCloudId;
		this.requesterSystem = requesterSystem;
		this.useGateway = useGateway;
		
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
		
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public Long getTargetCloudId() { return targetCloudId; }
	public SystemRequestDTO getRequesterSystem() { return requesterSystem; }
	public List<SystemRequestDTO> getPreferredSystems() { return preferredSystems; }
	public OrchestrationFlags getNegotiationFlags() { return negotiationFlags; }
	public boolean isUseGateway() { return useGateway; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setTargetCloudId(final Long targetCloudId) { this.targetCloudId = targetCloudId; }
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setUseGateway(final boolean useGateway) { this.useGateway = useGateway; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredSystems(final List<SystemRequestDTO> preferredSystems) {
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
	}
	
	public void setNegotiationFlags(final OrchestrationFlags negotiationFlags) {
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}
	}
}