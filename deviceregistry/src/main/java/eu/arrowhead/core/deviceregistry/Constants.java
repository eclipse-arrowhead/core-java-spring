/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.deviceregistry;

class Constants {

    //=================================================================================================
    // members
    static final String PATH_VARIABLE_ID = "deviceId";
    static final String PATH_VARIABLE_DEVICE_NAME = "deviceName";

    static final String DEVICES_URI = "/devices";
    static final String DEVICE_BY_ID_URI = "/device/{" + PATH_VARIABLE_ID + "}";
    static final String DEVICES_BY_ID_URI = "/devices/{" + PATH_VARIABLE_ID + "}";

    protected Constants() { super(); }
}
