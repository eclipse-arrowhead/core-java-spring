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

public class GatewayConsumerConnectionRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4911124425438496148L;
	
	private RelayRequestDTO relay;
	private String queueId;
	private String peerName;
	private String providerGWPublicKey;

	private SystemRequestDTO consumer;
	private SystemRequestDTO provider;
	private CloudRequestDTO consumerCloud;
	private CloudRequestDTO providerCloud;
	private String serviceDefinition;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatewayConsumerConnectionRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public GatewayConsumerConnectionRequestDTO(final RelayRequestDTO relay, final String queueId, final String peerName, final String providerGWPublicKey, final SystemRequestDTO consumer,
											   final SystemRequestDTO provider, final CloudRequestDTO consumerCloud, final CloudRequestDTO providerCloud, final String serviceDefinition) {
		this.relay = relay;
		this.queueId = queueId;
		this.peerName = peerName;
		this.providerGWPublicKey = providerGWPublicKey;
		this.consumer = consumer;
		this.provider = provider;
		this.consumerCloud = consumerCloud;
		this.providerCloud = providerCloud;
		this.serviceDefinition = serviceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO getRelay() { return relay; }
	public String getQueueId() { return queueId; }
	public String getPeerName() { return peerName; }
	public String getProviderGWPublicKey() { return providerGWPublicKey; }
	public SystemRequestDTO getConsumer() { return consumer; }
	public SystemRequestDTO getProvider() { return provider; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	public String getServiceDefinition() { return serviceDefinition; }

	//-------------------------------------------------------------------------------------------------
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setQueueId(final String queueId) { this.queueId = queueId; }
	public void setPeerName(final String peerName) { this.peerName = peerName; }
	public void setProviderGWPublicKey(final String providerGWPublicKey) { this.providerGWPublicKey = providerGWPublicKey; }	
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	
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