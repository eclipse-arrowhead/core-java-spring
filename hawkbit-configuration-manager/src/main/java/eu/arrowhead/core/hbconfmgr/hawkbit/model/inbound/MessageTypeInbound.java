/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound;


/**
 * Enum for inbound message type classification.
 */
public enum MessageTypeInbound {
    EVENT("EVENT"),
    THING_DELETED("THING_DELETED"),
    PING_RESPONSE("PING_RESPONSE");

    private final String stringValue;

    private MessageTypeInbound(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}