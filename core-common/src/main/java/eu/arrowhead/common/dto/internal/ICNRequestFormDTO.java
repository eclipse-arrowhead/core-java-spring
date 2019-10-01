package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ICNRequestFormDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 932072920257234750L;
	
	private ServiceQueryFormDTO requestedService;
	private Long targetCloudId;
	private SystemRequestDTO requesterSystem;
	private List<SystemRequestDTO> preferredSystems = new ArrayList<>();
	private OrchestrationFlags negotiationFlags = new OrchestrationFlags();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ICNRequestFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ICNRequestFormDTO(final ServiceQueryFormDTO requestedService, final Long targetCloudId, final SystemRequestDTO requesterSystem, final List<SystemRequestDTO> preferredSystems,
						     final OrchestrationFlags negotiationFlags) {
		Assert.notNull(requestedService, "Requested service is null.");
		Assert.notNull(targetCloudId, "Target cloud id is null.");
		Assert.notNull(requesterSystem, "Requester system is null.");
		
		this.requestedService = requestedService;
		this.targetCloudId = targetCloudId;
		this.requesterSystem = requesterSystem;
		
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
	
	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setTargetCloudId(final Long targetCloudId) { this.targetCloudId = targetCloudId; }
	public void setRequesterSystem(final SystemRequestDTO requesterSystem) { this.requesterSystem = requesterSystem; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPreferredSystems(final List<SystemRequestDTO> preferredSystems) {
		if (preferredSystems != null) {
			this.preferredSystems = preferredSystems;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setNegotiationFlags(final OrchestrationFlags negotiationFlags) {
		if (negotiationFlags != null) {
			this.negotiationFlags = negotiationFlags;
		}
	}
}