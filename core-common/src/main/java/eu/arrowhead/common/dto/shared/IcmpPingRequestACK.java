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
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IcmpPingRequestACK implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -7812241728073959599L;

	private String ackOk;
	private UUID externalMeasurementUuid;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public IcmpPingRequestACK() {}

	//-------------------------------------------------------------------------------------------------
	public String getAckOk() { return ackOk; }
	public UUID getExternalMeasurementUuid() { return externalMeasurementUuid; }

	//-------------------------------------------------------------------------------------------------
	public void setAckOk(final String ackOk) { this.ackOk = ackOk; }
	public void setExternalMeasurementUuid(final UUID externalMeasurementUuid) { this.externalMeasurementUuid = externalMeasurementUuid; }
	
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