package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class ChoreographerStartSessionDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 205131759149025379L;
    
    private long sessionId;
    private long planId;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStartSessionDTO() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStartSessionDTO(long sessionId, long planId) {
        this.sessionId = sessionId;
        this.planId = planId;
    }

    //-------------------------------------------------------------------------------------------------
    public long getSessionId() {
        return sessionId;
    }
    public long getPlanId() {
        return planId;
    }

    //-------------------------------------------------------------------------------------------------
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
    public void setPlanId(long planId) {
        this.planId = planId;
    }
}
