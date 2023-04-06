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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QoSReservationResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7502548027753697110L;
	
	private long id;
	private long reservedProviderId;
	private long reservedServiceId;	
	private String consumerSystemName;
	private String consumerAddress;
	private int consumerPort;	
	private String reservedTo;	
	private boolean temporaryLock;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSReservationResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSReservationResponseDTO(final long id, final long reservedProviderId, final long reservedServiceId, final String consumerSystemName, final String consumerAddress,
									 final int consumerPort, final String reservedTo, final boolean temporaryLock, final String createdAt, final String updatedAt) {
		this.id = id;
		this.reservedProviderId = reservedProviderId;
		this.reservedServiceId = reservedServiceId;
		this.consumerSystemName = consumerSystemName;
		this.consumerAddress = consumerAddress;
		this.consumerPort = consumerPort;
		this.reservedTo = reservedTo;
		this.temporaryLock = temporaryLock;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public long getReservedProviderId() { return reservedProviderId; }
	public long getReservedServiceId() { return reservedServiceId; }
	public String getConsumerSystemName() { return consumerSystemName; }
	public String getConsumerAddress() { return consumerAddress; }
	public int getConsumerPort() { return consumerPort; }
	public String getReservedTo() { return reservedTo; }
	public boolean isTemporaryLock() { return temporaryLock; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setReservedProviderId(final long reservedProviderId) { this.reservedProviderId = reservedProviderId; }
	public void setReservedServiceId(final long reservedServiceId) { this.reservedServiceId = reservedServiceId; }
	public void setConsumerSystemName(final String consumerSystemName) { this.consumerSystemName = consumerSystemName; }
	public void setConsumerAddress(final String consumerAddress) { this.consumerAddress = consumerAddress; }
	public void setConsumerPort(final int consumerPort) { this.consumerPort = consumerPort; }
	public void setReservedTo(final String reservedTo) { this.reservedTo = reservedTo; }
	public void setTemporaryLock(final boolean temporaryLock) { this.temporaryLock = temporaryLock; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
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