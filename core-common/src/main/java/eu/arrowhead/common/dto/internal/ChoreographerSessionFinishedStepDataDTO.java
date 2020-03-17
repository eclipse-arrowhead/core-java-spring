package eu.arrowhead.common.dto.internal;

public class ChoreographerSessionFinishedStepDataDTO {

    private long sessionId;

    private long runningStepId;

    public ChoreographerSessionFinishedStepDataDTO() {}

    public ChoreographerSessionFinishedStepDataDTO(long sessionId, long runningStepId) {
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
