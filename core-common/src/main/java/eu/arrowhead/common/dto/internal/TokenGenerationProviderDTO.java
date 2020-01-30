package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class TokenGenerationProviderDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -8058322682102502369L;
	
	private SystemRequestDTO provider;
	private List<String> serviceInterfaces = new ArrayList<>();
	private int tokenDuration = -1; // in seconds
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public TokenGenerationProviderDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationProviderDTO(final SystemRequestDTO provider, final int tokenDuration, final List<String> serviceInterfaces) {
		Assert.notNull(provider, "Provider is null.");
		Assert.isTrue(serviceInterfaces != null && !serviceInterfaces.isEmpty(), "Interface list is null or empty.");
		
		this.provider = provider;
		this.tokenDuration = tokenDuration;
		this.serviceInterfaces = serviceInterfaces;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getProvider() { return provider; }
	public int getTokenDuration() { return tokenDuration; }
	public List<String> getServiceInterfaces() { return serviceInterfaces; }
	
	//-------------------------------------------------------------------------------------------------
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setTokenDuration(final int tokenDuration) { this.tokenDuration = tokenDuration; }
	public void setServiceInterfaces(final List<String> serviceInterfaces) { this.serviceInterfaces = serviceInterfaces; }
}