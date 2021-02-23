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
