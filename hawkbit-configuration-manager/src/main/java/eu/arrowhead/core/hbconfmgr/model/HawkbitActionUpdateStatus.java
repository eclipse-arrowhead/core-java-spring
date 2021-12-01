/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.model;

import java.util.List;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.ActionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal model for representation of the update of a status of an action in hawkBit.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HawkbitActionUpdateStatus {

    private Long actionId;
    private Long softwareModuleId;
    private ActionStatus actionStatus;
    private List<String> message;
}