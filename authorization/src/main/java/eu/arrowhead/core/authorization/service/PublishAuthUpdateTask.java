package eu.arrowhead.core.authorization.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.exception.InvalidParameterException;

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
			
			authorizationDriver.publishAuthUpdate( systemId );		
				
		} catch (final Throwable ex) {			
			
			logger.debug("Exception:", ex.getMessage());			
		}
	}

}