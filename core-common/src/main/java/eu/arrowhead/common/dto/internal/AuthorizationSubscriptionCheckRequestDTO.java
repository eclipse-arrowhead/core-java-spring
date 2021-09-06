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

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class AuthorizationSubscriptionCheckRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4219066424271471771L;
	
	private SystemRequestDTO consumer;
	private Set<SystemRequestDTO> publishers;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckRequestDTO(final SystemRequestDTO consumer, final Set<SystemRequestDTO> publishers) {
		this.consumer = consumer;
		this.publishers = publishers;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getConsumer() { return consumer; }
	public Set<SystemRequestDTO> getPublishers() { return publishers; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setProviders(final Set<SystemRequestDTO> publishers) { this.publishers = publishers; }
	
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