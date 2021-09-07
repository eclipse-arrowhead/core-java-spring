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

import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;

public class ChoreographerSessionStepResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7338823471311785589L;
	
	private long id;
	private ChoreographerSessionResponseDTO session;
	private ChoreographerStepResponseDTO step;
	private ChoreographerExecutorResponseDTO executor;
	private ChoreographerSessionStepStatus status;
	private String message;
    private String startedAt;
    private String updatedAt;
    
    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionStepResponseDTO() {}
    		
    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStepResponseDTO(final long id, final ChoreographerSessionResponseDTO session, final ChoreographerStepResponseDTO step,
											   final ChoreographerExecutorResponseDTO executor, final ChoreographerSessionStepStatus status, final String message,
											   final String startedAt, final String updatedAt) {
		this.id = id;
		this.session = session;
		this.step = step;
		this.executor = executor;
		this.status = status;
		this.message = message;
		this.startedAt = startedAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ChoreographerSessionResponseDTO getSession() { return session; }
	public ChoreographerStepResponseDTO getStep() { return step; }
	public ChoreographerExecutorResponseDTO getExecutor() { return executor; }
	public ChoreographerSessionStepStatus getStatus() { return status; }
	public String getMessage() { return message; }
	public String getStartedAt() { return startedAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSession(final ChoreographerSessionResponseDTO session) { this.session = session; }
	public void setStep(final ChoreographerStepResponseDTO step) { this.step = step; }
	public void setExecutor(final ChoreographerExecutorResponseDTO executor) { this.executor = executor; }
	public void setStatus(final ChoreographerSessionStepStatus status) { this.status = status; }
	public void setMessage(final String message) { this.message = message; }
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