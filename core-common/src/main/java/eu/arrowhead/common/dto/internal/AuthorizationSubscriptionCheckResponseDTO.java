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

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class AuthorizationSubscriptionCheckResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 306170062969145764L;
	
	private SystemResponseDTO consumer;
	private Set<SystemResponseDTO> publishers;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckResponseDTO(final SystemResponseDTO consumer, final Set<SystemResponseDTO> publishers) {
		this.consumer = consumer;
		this.publishers = publishers;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getConsumer() { return consumer; }
	public Set<SystemResponseDTO> getPublishers() { return publishers; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemResponseDTO consumer) { this.consumer = consumer; }
	public void setPublishers(final Set<SystemResponseDTO> publishers) { this.publishers = publishers; }
	
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