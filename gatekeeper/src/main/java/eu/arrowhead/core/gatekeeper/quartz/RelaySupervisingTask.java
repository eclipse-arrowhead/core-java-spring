/********************************************************************************
 * Copyright (c) 2021 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.gatekeeper.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

@Component
@DisallowConcurrentExecution
public class RelaySupervisingTask implements Job {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		for (final GatekeeperRelayClient client : RelaySupervisor.getRegistry()) {
			client.destroyStaleQueuesAndConnections();
		}
	}
}
