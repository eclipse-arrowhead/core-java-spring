package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class TokenGenerationProviderDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5119997814495760531L;
	
	private SystemRequestDTO provider;
	private List<String> serviceInterfaces = new ArrayList<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public TokenGenerationProviderDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationProviderDTO(final SystemRequestDTO provider, final List<String> serviceInterfaces) {
		Assert.notNull(provider, "Provider is null.");
		Assert.isTrue(serviceInterfaces != null && !serviceInterfaces.isEmpty(), "Interface list is null or empty.");
		
		this.provider = provider;
		this.serviceInterfaces = serviceInterfaces;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getProvider() { return provider; }
	public List<String> getServiceInterfaces() { return serviceInterfaces; }
	
	//-------------------------------------------------------------------------------------------------
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setServiceInterfaces(final List<String> serviceInterfaces) { this.serviceInterfaces = serviceInterfaces; }
}