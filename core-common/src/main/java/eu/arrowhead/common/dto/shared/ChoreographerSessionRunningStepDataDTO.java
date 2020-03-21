package eu.arrowhead.common.dto.shared;

public class ChoreographerSessionRunningStepDataDTO {

    private long sessionId;

    private long runningStepId;

    public ChoreographerSessionRunningStepDataDTO() {}

    public ChoreographerSessionRunningStepDataDTO(long sessionId, long runningStepId) {
        this.sessionId = sessionId;
        this.runningStepId = runningStepId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getRunningStepId() {
        return runningStepId;
    }

    public void setRunningStepId(long runningStepId) {
        this.runningStepId = runningStepId;
    }
}
