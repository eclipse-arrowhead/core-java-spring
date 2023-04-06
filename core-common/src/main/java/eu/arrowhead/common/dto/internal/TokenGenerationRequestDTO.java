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

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class TokenGenerationRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -662827797790310767L;
	
	private SystemRequestDTO consumer;
	private CloudRequestDTO consumerCloud;
	private List<TokenGenerationProviderDTO> providers = new ArrayList<>();
	private String service;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public TokenGenerationRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationRequestDTO(final SystemRequestDTO consumer, final CloudRequestDTO consumerCloud, final List<TokenGenerationProviderDTO> providers, final String service) {
		Assert.notNull(consumer, "Consumer is null.");
		Assert.isTrue(providers != null && !providers.isEmpty(), "Provider list is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(service), "Service is null or blank.");
		
		this.consumer = consumer;
		this.consumerCloud = consumerCloud;
		this.providers = providers;
		this.service = service;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getConsumer() { return consumer; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public List<TokenGenerationProviderDTO> getProviders() { return providers; }
	public String getService() { return service; }
	
	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProviders(final List<TokenGenerationProviderDTO> providers) { this.providers = providers; }
	public void setService(final String service) { this.service = service; }
	
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