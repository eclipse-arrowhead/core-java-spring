/********************************************************************************
 * Copyright (c) 2020 AITIA
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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerExecuteStepRequestDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = 7693538826059396949L;

	private long sessionId;
    private long sessionStepId;
    private List<OrchestrationResultDTO> preconditionOrchestrationResults;
    private OrchestrationResultDTO mainOrchestrationResult;
    private int quantity;
    private Map<String,String> staticParameters;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecuteStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerExecuteStepRequestDTO(final long sessionId, final long sessionStepId, final List<OrchestrationResultDTO> preconditionOrchestrationResults, final OrchestrationResultDTO mainOrchestrationResult, final int quantity,
											  final Map<String,String> staticParameters) {
		this.sessionId = sessionId;
		this.sessionStepId = sessionStepId;
		this.preconditionOrchestrationResults = preconditionOrchestrationResults;
		this.mainOrchestrationResult = mainOrchestrationResult;
		this.quantity = quantity;
		this.staticParameters = staticParameters;
	}
	
	//-------------------------------------------------------------------------------------------------
    public long getSessionId() { return sessionId; }
    public long getSessionStepId() { return sessionStepId; }
    public List<OrchestrationResultDTO> getPreconditionOrchestrationResults() { return preconditionOrchestrationResults; }
    public OrchestrationResultDTO getMainOrchestrationResult() { return mainOrchestrationResult; }
    public int getQuantity() { return quantity; }
    public Map<String,String> getStaticParameters() { return staticParameters; }

    //-------------------------------------------------------------------------------------------------
    public void setSessionId(final long sessionId) { this.sessionId = sessionId; }
	public void setSessionStepId(final long sessionStepId) { this.sessionStepId = sessionStepId; }
	public void setPreconditionOrchestrationResults(final List<OrchestrationResultDTO> preconditionOrchestrationResults) { this.preconditionOrchestrationResults = preconditionOrchestrationResults; }
	public void setMainOrchestrationResult(final OrchestrationResultDTO mainOrchestrationResult) { this.mainOrchestrationResult = mainOrchestrationResult; }
	public void setQuantity(final int quantity) { this.quantity = quantity; }
	public void setStaticParameters(final Map<String,String> staticParameters) { this.staticParameters = staticParameters; }
	
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