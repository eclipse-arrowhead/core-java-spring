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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class GatewayProviderConnectionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2845753936695022228L;
	
	private RelayRequestDTO relay;
	private SystemRequestDTO consumer;
	private SystemRequestDTO provider;
	private CloudRequestDTO consumerCloud;
	private CloudRequestDTO providerCloud;
	private String serviceDefinition;
	private String consumerGWPublicKey;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionRequestDTO(final RelayRequestDTO relay, final SystemRequestDTO consumer, final SystemRequestDTO provider, final CloudRequestDTO consumerCloud, 
											   final CloudRequestDTO providerCloud, final String serviceDefinition, final String consumerGWPublicKey) {
		this.relay = relay;
		this.consumer = consumer;
		this.provider = provider;
		this.consumerCloud = consumerCloud;
		this.providerCloud = providerCloud;
		this.serviceDefinition = serviceDefinition;
		this.consumerGWPublicKey = consumerGWPublicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO getRelay() { return relay; }
	public SystemRequestDTO getConsumer() { return consumer; }
	public SystemRequestDTO getProvider() { return provider; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	public String getServiceDefinition() { return serviceDefinition; }
	public String getConsumerGWPublicKey() { return consumerGWPublicKey; }

	//-------------------------------------------------------------------------------------------------
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerGWPublicKey(final String consumerGWPublicKey) { this.consumerGWPublicKey = consumerGWPublicKey; }
	
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