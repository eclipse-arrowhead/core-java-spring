/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit.model;


/**
 * Enum for outbound UPDATE_ACTION_STATUS type classification.
 */
public enum ActionStatus {
    DOWNLOAD("DOWNLOAD"),
    RETRIEVED("RETRIEVED"),
    RUNNING("RUNNING"),
    FINISHED("FINISHED"),
    ERROR("ERROR"),
    WARNING("WARNING"),
    CANCELED("CANCELED"),
    CANCEL_REJECTED("CANCEL_REJECTED"),
    DOWNLOADED("DOWNLOADED");

    private final String stringValue;

    private ActionStatus(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}