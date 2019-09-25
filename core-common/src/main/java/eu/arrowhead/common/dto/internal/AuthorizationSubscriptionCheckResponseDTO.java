package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Set;

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class AuthorizationSubscriptionCheckResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 306170062969145764L;
	
	private SystemResponseDTO consumer;
	private Set<SystemResponseDTO> publishers;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckResponseDTO(final SystemResponseDTO consumer, final Set<SystemResponseDTO> publishers) {
		this.consumer = consumer;
		this.publishers = publishers;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getConsumer() { return consumer; }
	public Set<SystemResponseDTO> getPublishers() { return publishers; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemResponseDTO consumer) { this.consumer = consumer; }
	public void setProviders(final Set<SystemResponseDTO> publishers) { this.publishers = publishers; }

}
