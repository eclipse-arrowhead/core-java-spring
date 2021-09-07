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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;

public class ChoreographerRunPlanResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -9033278230529407150L;
	
	private Long planId;
	private Long sessionId;
	
	private ChoreographerSessionStatus status;
	private List<String> errorMessages = new ArrayList<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO(final Long planId, final Long sessionId, final ChoreographerSessionStatus status, final List<String> errorMessages) {
		this.planId = planId;
		this.sessionId = sessionId;
		this.status = status;
		this.errorMessages.addAll(errorMessages);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO(final long planId, final long sessionId) {
		this(planId, sessionId, ChoreographerSessionStatus.INITIATED, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO(final Long planId, final List<String> errorMessages) {
		this(planId, null, ChoreographerSessionStatus.ABORTED, errorMessages);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getPlanId() { return planId; }
	public Long getSessionId() { return sessionId; }
	public ChoreographerSessionStatus getStatus() { return status; }
	public List<String> getErrorMessages() { return errorMessages; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPlanId(final Long planId) { this.planId = planId; }
	public void setSessionId(final Long sessionId) { this.sessionId = sessionId; }
	public void setStatus(final ChoreographerSessionStatus status) { this.status = status; }
	public void setErrorMessages(final List<String> errorMessages) { this.errorMessages = errorMessages; }
	
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