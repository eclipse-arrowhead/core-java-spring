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
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;

public class EventPublishStartDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 882133275790725633L;
	
	private EventPublishRequestDTO request;
	private Set<Subscription> involvedSubscriptions;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishStartDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishStartDTO(final EventPublishRequestDTO request, final Set<Subscription> involvedSubscriptions) {
		this.request = request;
		this.involvedSubscriptions = involvedSubscriptions;
	}
	
	//-------------------------------------------------------------------------------------------------
	public EventPublishRequestDTO getRequest() { return request; }
	public Set<Subscription> getInvolvedSubscriptions() { return involvedSubscriptions; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequest(final EventPublishRequestDTO request) { this.request = request; }
	public void setInvolvedSubscriptions(final Set<Subscription> involvedSubscriptions) { this.involvedSubscriptions = involvedSubscriptions; }
	
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