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
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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