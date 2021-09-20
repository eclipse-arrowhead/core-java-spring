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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6788896769029136585L;
	
	private String eventType;
	private Map<String,String> metaData;
	private String payload;
	private String timeStamp;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventDTO(final String eventType, final Map<String,String> metaData, final String payload, final String timeStamp) {
		this.eventType = eventType;
		this.metaData = metaData;
		this.payload = payload;
		this.timeStamp = timeStamp;
	}	

	//-------------------------------------------------------------------------------------------------
	public String getEventType() {	return eventType; }
	public Map<String,String> getMetaData() { return metaData; }
	public String getPayload() { return payload; }
	public String getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final String eventType) { this.eventType = eventType; }
	public void setMetaData( final Map<String,String> metaData ) { this.metaData = metaData; }
	public void setPayload(final String payload) { this.payload = payload; }
	public void setTimeStamp(final String timeStamp) { this.timeStamp = timeStamp; }
	
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