/********************************************************************************
 * Copyright (c) 2020 AITIA
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

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;

public class QoSMonitorSenderConnectionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7942734332194435665L;
	
	private CloudRequestDTO targetCloud;
	private RelayRequestDTO relay;
	private String queueId;
	private String peerName;
	private String receiverQoSMonitorPublicKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSMonitorSenderConnectionRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSMonitorSenderConnectionRequestDTO(final CloudRequestDTO targetCloud, final RelayRequestDTO relay, final String queueId, final String peerName, 
											    final String receiverQoSMonitorPublicKey) {
		Assert.notNull(targetCloud, "'targetCloud' is null.");
		Assert.notNull(relay, "'relay' is null.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "'queueId' is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(peerName), "'peerName' is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(receiverQoSMonitorPublicKey), "'receiverQoSMonitorPublicKey' is null or blank.");
		
		this.targetCloud = targetCloud;
		this.relay = relay;
		this.queueId = queueId;
		this.peerName = peerName;
		this.receiverQoSMonitorPublicKey = receiverQoSMonitorPublicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudRequestDTO getTargetCloud() { return targetCloud; }
	public RelayRequestDTO getRelay() { return relay; }
	public String getQueueId() { return queueId; }
	public String getPeerName() { return peerName; }
	public String getReceiverQoSMonitorPublicKey() { return receiverQoSMonitorPublicKey; }

	//-------------------------------------------------------------------------------------------------
	public void setTargetCloud(final CloudRequestDTO targetCloud) { this.targetCloud = targetCloud; }
	public void setRelay(final RelayRequestDTO relay) { this.relay = relay; }
	public void setQueueId(final String queueId) { this.queueId = queueId; }
	public void setPeerName(final String peerName) { this.peerName = peerName; }
	public void setReceiverQoSMonitorPublicKey(final String receiverQoSMonitorPublicKey) { this.receiverQoSMonitorPublicKey = receiverQoSMonitorPublicKey; }
	
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