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

public enum EventType {
    CANCEL_DOWNLOAD("CANCEL_DOWNLOAD"),
    DOWNLOAD("DOWNLOAD"),
    DOWNLOAD_AND_INSTALL("DOWNLOAD_AND_INSTALL"),
    MULTI_ACTION("MULTI_ACTION"),
    REQUEST_ATTRIBUTES_UPDATE("REQUEST_ATTRIBUTES_UPDATE"),
    UPDATE_ACTION_STATUS("UPDATE_ACTION_STATUS");

    private final String stringValue;

    private EventType(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}