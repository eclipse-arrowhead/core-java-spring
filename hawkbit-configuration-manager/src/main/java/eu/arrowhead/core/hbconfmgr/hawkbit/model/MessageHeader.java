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
 * Enum for outbound header type classification.
 */
public enum MessageHeader {
    SENDER("sender"),
    TENANT("tenant"),
    THING_ID("thingId"),
    TOPIC("topic"),
    TYPE("type");

    private final String stringValue;

    private MessageHeader(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}