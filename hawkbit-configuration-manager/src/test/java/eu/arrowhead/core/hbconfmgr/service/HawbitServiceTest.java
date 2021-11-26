/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.arrowhead.core.hbconfmgr.hawkbit.HawkbitDmfOutboundClient;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.ActionStatus;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.UpdateActionStatusOutboundMessage;
import eu.arrowhead.core.hbconfmgr.model.HawkbitActionUpdateStatus;
import eu.arrowhead.core.hbconfmgr.service.HawkbitService;

public class HawbitServiceTest {
    HawkbitService hawkbitService;
    HawkbitDmfOutboundClient mock_hawkbitDmfClient;

    @BeforeEach
    public void init() {
        mock_hawkbitDmfClient = mock(HawkbitDmfOutboundClient.class);

        hawkbitService = new HawkbitService("testTenant", mock_hawkbitDmfClient);
    }

    @Test
    public void testUpdateActionStatus() throws ConstraintViolationException, IOException {
        ArrayList<String> messageList = new ArrayList<String>();
        messageList.add("test");

        HawkbitActionUpdateStatus hActionUpdateStatus = HawkbitActionUpdateStatus
            .builder()
            .actionId(123456789L)
            .actionStatus(ActionStatus.CANCELED)
            .softwareModuleId(987654321L)
            .message(messageList)
            .build();

        UpdateActionStatusOutboundMessage message = UpdateActionStatusOutboundMessage.builder()
            .body(
                UpdateActionStatusOutboundMessage.UpdateActionStatusOutboundMessageBody.builder()
                    .actionId(123456789L)
                    .actionStatus(ActionStatus.CANCELED)
                    .softwareModuleId(987654321L)
                    .message(messageList).build()
            )
            .headers(
                UpdateActionStatusOutboundMessage.UpdateActionStatusOutboundMessageHeaders.builder()
                .tenant("DEFAULT")
                .build()
            )
            .build();


        hawkbitService.updateActionStatus(hActionUpdateStatus);

        verify(mock_hawkbitDmfClient).updateActionStatus(message);
    }
}
