package eu.arrowhead.common.dto;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TokenDataDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 496539523077090989L;
	
	private String providerName;
	private String providerAddress;
	private int providerPort;
	
	private String token;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public TokenDataDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenDataDTO(final SystemRequestDTO provider, final String token) {
		Assert.notNull(provider, "Provider is null.");
		
		this.providerName = provider.getSystemName();
		this.providerAddress = provider.getAddress();
		this.providerPort = provider.getPort();
		this.token = token;
	}

	//-------------------------------------------------------------------------------------------------
	public String getProviderName() { return providerName; }
	public String getProviderAddress() { return providerAddress; }
	public int getProviderPort() { return providerPort; }
	public String getToken() { return token; }

	//-------------------------------------------------------------------------------------------------
	public void setProviderName(String providerName) { this.providerName = providerName; }
	public void setProviderAddress(String providerAddress) { this.providerAddress = providerAddress; }
	public void setProviderPort(int providerPort) { this.providerPort = providerPort; }
	public void setToken(String token) { this.token = token; }
}