/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.quartz.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.arrowhead.core.qos.database.service.QoSReservationDBService;

@Component
@DisallowConcurrentExecution
public class ReservationCheckTask implements Job {
	
	//=================================================================================================
	// members

	protected final Logger logger = LogManager.getLogger(ReservationCheckTask.class);
	
	@Autowired
	private QoSReservationDBService qosReservationDBService;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Services reservation check task");
		
		final int removeCount = qosReservationDBService.releaseObsoleteReservations();
		
		logger.debug("FINISHED: Services reservation check task. Number of removed reservation entry: {}", removeCount);
	}
}