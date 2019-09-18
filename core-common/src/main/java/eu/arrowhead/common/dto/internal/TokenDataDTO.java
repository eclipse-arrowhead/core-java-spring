package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

@JsonInclude(Include.NON_NULL)
public class TokenDataDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7439339840205404034L;
	
	private String providerName;
	private String providerAddress;
	private int providerPort;
	
	private Map<String,String> tokens;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public TokenDataDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenDataDTO(final SystemRequestDTO provider, final Map<String,String> tokens) {
		Assert.notNull(provider, "Provider is null.");
		
		this.providerName = provider.getSystemName();
		this.providerAddress = provider.getAddress();
		this.providerPort = provider.getPort();
		this.tokens = tokens;
	}

	//-------------------------------------------------------------------------------------------------
	public String getProviderName() { return providerName; }
	public String getProviderAddress() { return providerAddress; }
	public int getProviderPort() { return providerPort; }
	public Map<String,String> getTokens() { return tokens; }

	//-------------------------------------------------------------------------------------------------
	public void setProviderName(final String providerName) { this.providerName = providerName; }
	public void setProviderAddress(final String providerAddress) { this.providerAddress = providerAddress; }
	public void setProviderPort(final int providerPort) { this.providerPort = providerPort; }
	public void setTokens(final Map<String,String> tokens) { this.tokens = tokens; }
}