/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.websocket.model;

import eu.arrowhead.core.confmgr.hawkbit.model.ActionStatus;
import eu.arrowhead.core.confmgr.model.HawkbitActionUpdateStatus;

public class ActionUpdateStatusMapper {

    private ActionUpdateStatusMapper() {
    }

    public static HawkbitActionUpdateStatus mapToActionUpdateStatus(UpdateActionRequestDTO input) throws IllegalArgumentException {
        return HawkbitActionUpdateStatus.builder()
                .actionId(input.getActionId())
                .actionStatus(ActionStatus.valueOf(input.getActionStatus()))
                .message(input.getMessage())
                .softwareModuleId(input.getSoftwareModuleId())
                .build();
    }

}
