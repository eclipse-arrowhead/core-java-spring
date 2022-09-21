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

public class ChoreographerRunPlanResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 5685633104518742525L;
	
	private Long planId;
	private Long sessionId;
	
	private Long quantity;
	private ChoreographerSessionStatus status;
	private List<String> errorMessages = new ArrayList<>();
	private boolean needInterCloud = false; 
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO(final Long planId, final Long sessionId, final Long quantity, final ChoreographerSessionStatus status, final List<String> errorMessages, final boolean needInterCloud) {
		this.planId = planId;
		this.sessionId = sessionId;
		this.quantity = quantity;
		this.status = status;
		this.needInterCloud = needInterCloud;
		if (errorMessages != null) {
			this.errorMessages.addAll(errorMessages);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO(final long planId, final long sessionId, final long quantity, final boolean needInterCloud) {
		this(planId, sessionId, quantity, ChoreographerSessionStatus.INITIATED, null, needInterCloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerRunPlanResponseDTO(final Long planId, final Long quantity, final List<String> errorMessages, final boolean needInterCloud) {
		this(planId, null, quantity, ChoreographerSessionStatus.ABORTED, errorMessages, needInterCloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getPlanId() { return planId; }
	public Long getSessionId() { return sessionId; }
	public Long getQuantity() { return quantity; }
	public ChoreographerSessionStatus getStatus() { return status; }
	public List<String> getErrorMessages() { return errorMessages; }
	public boolean getNeedInterCloud() { return needInterCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPlanId(final Long planId) { this.planId = planId; }
	public void setSessionId(final Long sessionId) { this.sessionId = sessionId; }
	public void setQuantity(final Long quantity) { this.quantity = quantity; }
	public void setStatus(final ChoreographerSessionStatus status) { this.status = status; }
	public void setErrorMessages(final List<String> errorMessages) { this.errorMessages = errorMessages; }
	public void setNeedInterCloud(final boolean needInterCloud) { this.needInterCloud = needInterCloud; }
	
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