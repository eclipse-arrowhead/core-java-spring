/********************************************************************************
 * Copyright (c) 2019 AITIA
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

package eu.arrowhead.core.authorization.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

public class PublishAuthUpdateTask implements Runnable {

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(PublishAuthUpdateTask.class);
	
	private final AuthorizationDriver authorizationDriver;
	private final long systemId;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public PublishAuthUpdateTask(final AuthorizationDriver authorizationDriver, final long systemId) {
		Assert.notNull(authorizationDriver, "authorizationDriver is null");
		Assert.isTrue(systemId > 0, "systemId is less than one");
		
		this.authorizationDriver = authorizationDriver;
		this.systemId = systemId;
	}

	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {
		try {
			logger.debug("PublishAuthUpdateTask.run started...");

			if (Thread.currentThread().isInterrupted()) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				
				return;
			}
			
			authorizationDriver.publishAuthUpdate(systemId);		
		} catch (final Throwable ex) {			
			logger.debug("Exception:", ex.getMessage());
		}
	}
}