/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound;


/**
 * Enum for outbound message type classification.
 */
public enum MessageTypeOutbound {
    EVENT("EVENT"),
    THING_CREATED("THING_CREATED"),
    THING_REMOVED("THING_REMOVED");

    private final String stringValue;

    private MessageTypeOutbound(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}