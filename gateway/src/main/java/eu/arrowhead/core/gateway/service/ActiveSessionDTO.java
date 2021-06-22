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

package eu.arrowhead.core.gateway.service;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

@JsonInclude(Include.NON_NULL)
public class ActiveSessionDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7284074955400443451L;
	
	private String queueId;
	private String peerName;
	
	private SystemRequestDTO consumer;
	private CloudRequestDTO consumerCloud;
	
	private SystemRequestDTO provider;
	private CloudRequestDTO providerCloud;
	
	private String serviceDefinition;
	
	private RelayRequestDTO relay;
	private String requestQueue;
	private String requestControlQueue;
	private String responseQueue;
	private String responseControlQueue;
	
	private String sessionStartedAt;
	private Integer consumerServerSocketPort;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ActiveSessionDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ActiveSessionDTO(final String queueId, final String peerName, final SystemRequestDTO consumer, final CloudRequestDTO consumerCloud, final SystemRequestDTO provider,
							final CloudRequestDTO providerCloud, final String serviceDefinition, final RelayRequestDTO relay, final String sessionStartedAt, final Integer consumerServerSocketPort) {
		Assert.isTrue(!Utilities.isEmpty(peerName), "peerName is null.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null.");
		Assert.notNull(relay, "relay is null.");
		
		this.queueId = queueId;
		this.peerName = peerName;
		this.consumer = consumer;
		this.consumerCloud = consumerCloud;
		this.provider = provider;
		this.providerCloud = providerCloud;
		this.serviceDefinition = serviceDefinition;
		this.relay = relay;
		this.sessionStartedAt = sessionStartedAt;
		this.consumerServerSocketPort = consumerServerSocketPort;
		
		this.requestQueue = GatewayRelayClient.REQUEST_QUEUE_PREFIX + peerName + "-" + queueId;
		this.requestControlQueue = this.requestQueue + GatewayRelayClient.CONTROL_QUEUE_SUFFIX;
		this.responseQueue = GatewayRelayClient.RESPONSE_QUEUE_PREFIX + peerName + "-" + queueId;
		this.responseControlQueue = this.responseQueue + GatewayRelayClient.CONTROL_QUEUE_SUFFIX;
	}

	//-------------------------------------------------------------------------------------------------
	public String getQueueId() { return queueId; }
	public String getPeerName() { return peerName; }
	public SystemRequestDTO getConsumer() { return consumer; }
	public CloudRequestDTO getConsumerCloud() { return consumerCloud; }
	public SystemRequestDTO getProvider() { return provider; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	public String getServiceDefinition() { return serviceDefinition; }
	public RelayRequestDTO getRelay() { return relay; }
	public String getRequestQueue() { return requestQueue; }
	public String getRequestControlQueue() { return requestControlQueue; }
	public String getResponseQueue() { return responseQueue; }
	public String getResponseControlQueue() { return responseControlQueue; }
	public String getSessionStartedAt() { return sessionStartedAt; }
	public Integer getConsumerServerSocketPort() { return consumerServerSocketPort; }

	//-------------------------------------------------------------------------------------------------	
	public void setQueueId(final String queueId) { this.queueId = queueId; }
	public void setPeerName(final String peerName) { this.peerName = peerName; }
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setConsumerCloud(final CloudRequestDTO consumerCloud) { this.consumerCloud = consumerCloud; }
	public void setProvider(final SystemRequestDTO provider) { this.provider = provider; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setRequestQueue(final String requestQueue) { this.requestQueue = requestQueue; }
	public void setRequestControlQueue(final String requestControlQueue) { this.requestControlQueue = requestControlQueue; }
	public void setResponseQueue(final String responseQueue) { this.responseQueue = responseQueue; }
	public void setResponseControlQueue(final String responseControlQueue) { this.responseControlQueue = responseControlQueue; }
	public void setSessionStartedAt(final String sessionStartedAt) { this.sessionStartedAt = sessionStartedAt; }
	public void setConsumerServerSocketPort(final Integer consumerServerSocketPort) { this.consumerServerSocketPort = consumerServerSocketPort; }	
}