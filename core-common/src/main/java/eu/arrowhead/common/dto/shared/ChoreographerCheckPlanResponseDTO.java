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

public class ChoreographerCheckPlanResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6599506456874073377L;
	
	private long planId;
	private List<String> errorMessages = new ArrayList<>();
	private boolean needInterCloud;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerCheckPlanResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerCheckPlanResponseDTO(final long planId, final List<String> errorMessages, final boolean needInterCloud) {
		this.planId = planId;
		this.needInterCloud = needInterCloud;
		if (errorMessages != null) {
			this.errorMessages.addAll(errorMessages);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getPlanId() { return planId; }
	public List<String> getErrorMessages() { return errorMessages; }
	public boolean getNeedInterCloud() { return needInterCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public void setPlanId(final long planId) { this.planId = planId; }
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