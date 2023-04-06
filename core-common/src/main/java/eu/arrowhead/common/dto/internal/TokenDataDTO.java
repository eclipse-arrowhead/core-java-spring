/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}