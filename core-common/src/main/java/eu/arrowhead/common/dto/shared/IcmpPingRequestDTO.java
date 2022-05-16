/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IcmpPingRequestDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 8327872537376847255L;

	private String host;
	private Integer ttl;
	private Integer packetSize;
	private Long timeout;
	private Integer timeToRepeat;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String getHost() { return host; }
	public Integer getTtl() { return ttl; }
	public Integer getPacketSize() { return packetSize; }
	public Long getTimeout() { return timeout; }
	public Integer getTimeToRepeat() { return timeToRepeat; }

	//-------------------------------------------------------------------------------------------------
	public void setHost(final String host) { this.host = host; }
	public void setTtl(final Integer ttl) { this.ttl = ttl; }
	public void setPacketSize(final Integer packetSize) { this.packetSize = packetSize; }
	public void setTimeout(final Long timeout) { this.timeout = timeout; }
	public void setTimeToRepeat(final Integer timeToRepeat) { this.timeToRepeat = timeToRepeat; }
	
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