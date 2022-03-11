/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.websocket.model;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.ActionStatus;
import eu.arrowhead.core.hbconfmgr.model.HawkbitActionUpdateStatus;

public class ActionUpdateStatusMapper {

    private ActionUpdateStatusMapper() {
    	throw new UnsupportedOperationException();
    }

    public static HawkbitActionUpdateStatus mapToActionUpdateStatus(final UpdateActionRequestDTO input) throws IllegalArgumentException {
        return HawkbitActionUpdateStatus.builder()
                .actionId(input.getActionId())
                .actionStatus(ActionStatus.valueOf(input.getActionStatus()))
                .message(input.getMessage())
                .softwareModuleId(input.getSoftwareModuleId())
                .build();
    }
}