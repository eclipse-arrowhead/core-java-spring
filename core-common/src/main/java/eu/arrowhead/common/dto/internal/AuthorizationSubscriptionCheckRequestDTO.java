package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Set;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class AuthorizationSubscriptionCheckRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4219066424271471771L;
	
	private SystemRequestDTO consumer;
	private Set<SystemRequestDTO> publishers;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckRequestDTO(final SystemRequestDTO consumer, final Set<SystemRequestDTO> publishers) {
		this.consumer = consumer;
		this.publishers = publishers;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getConsumer() { return consumer; }
	public Set<SystemRequestDTO> getPublishers() { return publishers; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setProviders(final Set<SystemRequestDTO> publishers) { this.publishers = publishers; }

}
