package eu.arrowhead.common.dto.shared;

import java.util.List;

public class ChoreographerSessionRunningStepDataDTO {

    //=================================================================================================
    // members

    private long sessionId;
    private long runningStepId;
    private List<OrchestrationResponseDTO> preconditionOrchestrationResponses;
    private OrchestrationResponseDTO mainOrchestrationResponse;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionRunningStepDataDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionRunningStepDataDTO(long sessionId, long runningStepId, List<OrchestrationResponseDTO> preconditionOrchestrationResponses, OrchestrationResponseDTO mainOrchestrationResponse) {
        this.sessionId = sessionId;
        this.runningStepId = runningStepId;
        this.preconditionOrchestrationResponses = preconditionOrchestrationResponses;
        this.mainOrchestrationResponse = mainOrchestrationResponse;
    }

    //-------------------------------------------------------------------------------------------------
    public long getSessionId() { return sessionId; }
    public long getRunningStepId() { return runningStepId; }
    public List<OrchestrationResponseDTO> getPreconditionOrchestrationResponses() { return preconditionOrchestrationResponses; }
    public OrchestrationResponseDTO getMainOrchestrationResponse() { return mainOrchestrationResponse; }

    //-------------------------------------------------------------------------------------------------
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }
    public void setRunningStepId(long runningStepId) { this.runningStepId = runningStepId; }
    public void setPreconditionOrchestrationResponses(List<OrchestrationResponseDTO> preconditionOrchestrationResponses) {
        this.preconditionOrchestrationResponses = preconditionOrchestrationResponses; }
    public void setMainOrchestrationResponse(OrchestrationResponseDTO mainOrchestrationResponse) {
        this.mainOrchestrationResponse = mainOrchestrationResponse; }
}
