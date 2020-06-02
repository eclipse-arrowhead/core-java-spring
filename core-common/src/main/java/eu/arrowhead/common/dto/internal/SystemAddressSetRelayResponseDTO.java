package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.Assert;

public class SystemAddressSetRelayResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3040647144187918101L;
	
	private Set<String> addresses = new HashSet<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO(final Set<String> addresses) {
		Assert.notNull(addresses, "'addresses' is null.");
		
		this.addresses = addresses; 
	}

	//-------------------------------------------------------------------------------------------------
	public Set<String> getAddresses() { return addresses; }

	//-------------------------------------------------------------------------------------------------
	public void setAddresses(final Set<String> addresses) { this.addresses = addresses; }
}