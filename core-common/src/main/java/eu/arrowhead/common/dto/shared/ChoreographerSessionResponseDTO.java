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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerSessionResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 6273263178560429948L;
	
	private long id;
	private long planid;
	private String planName;
	private ChoreographerSessionStatus status;
	private long quantityDone;
	private long quantityGoal;
	private long executionNumber;
    private String notifyUri;
    private String startedAt;
    private String updatedAt;
    
    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionResponseDTO() {};
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionResponseDTO(final long id, final long planid, final String planName, final ChoreographerSessionStatus status, final long quantityDone, final long quantityGoal,
										   final long executionNumber, final String notifyUri, final String startedAt, final String updatedAt) {
		this.id = id;
		this.planid = planid;
		this.planName = planName;
		this.status = status;
		this.quantityDone = quantityDone;
		this.quantityGoal = quantityGoal;
		this.executionNumber = executionNumber;
		this.notifyUri = notifyUri;
		this.startedAt = startedAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public long getPlanid() { return planid; }
	public String getPlanName() { return planName; }
	public ChoreographerSessionStatus getStatus() { return status; }
	public long getQuantityDone() { return quantityDone; }
	public long getQuantityGoal() { return quantityGoal; }
	public long getExecutionNumber() { return executionNumber; }
	public String getNotifyUri() { return notifyUri; }	
	public String getStartedAt() { return startedAt; }
	public String getUpdatedAt() { return updatedAt; }

	//------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setPlanid(final long planid) { this.planid = planid; }
	public void setPlanName(final String planName) { this.planName = planName; }
	public void setStatus(final ChoreographerSessionStatus status) { this.status = status; }
	public void setQuantityDone(final long quantityDone) { this.quantityDone = quantityDone; }
	public void setQuantityGoal(final long quantityGoal) { this.quantityGoal = quantityGoal; }
	public void setExecutionNumber(final long executionNumber) { this.executionNumber = executionNumber; }
	public void setNotifyUri(final String notifyUri) { this.notifyUri = notifyUri; }
	public void setStartedAt(final String startedAt) { this.startedAt = startedAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
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