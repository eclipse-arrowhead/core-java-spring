package eu.arrowhead.common.dto.internal;

import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;

import javax.jms.Session;

public class ChoreographerStartSessionDTO {

    private long sessionId;
    private long planId;

    public ChoreographerStartSessionDTO() {
    }

    public ChoreographerStartSessionDTO(long sessionId, long planId) {
        this.sessionId = sessionId;
        this.planId = planId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
    }
}
