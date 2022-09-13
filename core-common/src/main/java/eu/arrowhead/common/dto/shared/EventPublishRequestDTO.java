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

public class EventPublishRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -5708154911939798799L;
	
	private String eventType;
	private SystemRequestDTO source;
	private Map<String,String> metaData;
	private String payload;
	private String timeStamp;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishRequestDTO(final String eventType, final SystemRequestDTO source, final Map<String,String> metaData, final String payload, final String timeStamp) {
		this.eventType = eventType;
		this.source = source;
		this.metaData = metaData;
		this.payload = payload;
		this.timeStamp = timeStamp;
	}	

	//-------------------------------------------------------------------------------------------------
	public String getEventType() {	return eventType; }
	public SystemRequestDTO getSource() {	return source; }
	public Map<String,String> getMetaData() { return metaData; }
	public String getPayload() { return payload; }
	public String getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final String eventType) { this.eventType = eventType; }
	public void setSource(final SystemRequestDTO subscriberSystem) { this.source = subscriberSystem; }
	public void setMetaData(final Map<String,String> metaData) { this.metaData = metaData; }
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