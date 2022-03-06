/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit;

public class HawkbitDmfConstants {

    /**
     * The exchange name for all messages sent to hawkBit. It is predefined from hawkBit and can not be changed.
     */
    public static final String SENDING_EXCHANGE = "dmf.exchange";

    /**
     * The routing key for all messages sent to hawkBit. It is predefined from hawkBit and can not be changed.
     */
    public static final String SENDING_ROUTING_KEY = "";

    /**
     * The exchange name for all messages sent from hawkBit to the configuration system. It can be modified.
     */
    public static final String RECEIVING_EXCHANGE = "configuration_system.direct.exchange";

    /**
     * The queue name for all messages sent from hawkBit to the configuration system. It can be modified.
     */
    public static final String RECEIVING_QUEUE = "configuration_system_direct_queue";

    /**
     * The routing key for all messages sent from hawkBit to the configuration system. It can be modified.
     */
    public static final String RECEIVING_ROUTING_KEY = "";
}