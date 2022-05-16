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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventTypeRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1177806380638957855L;
	
	private String eventTypeName;
		
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public EventTypeRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventTypeRequestDTO(final String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getEventTypeName() { return eventTypeName; }

	//-------------------------------------------------------------------------------------------------
	public void setEventTypeName(final String eventTypeName) { this.eventTypeName = eventTypeName; }
	
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