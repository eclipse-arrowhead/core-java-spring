package eu.arrowhead.core.eventhandler.service;

import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;

public class PublishEventTask implements Runnable{

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(PublishEventTask.class);
	
	private final Subscription subscription;
	private final Session session;
	private final EventPublishRequestDTO publishRequestDTO;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public PublishEventTask(final Subscription subscription, final EventPublishRequestDTO publishRequestDTO) {
		
		this.subscription = subscription;
		this.publishRequestDTO = publishRequestDTO;
		this.session = getSession();
	}

	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {
		try {
			logger.debug("PublishEventTask.run started...");
			
			if (Thread.currentThread().isInterrupted()) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				
				return;
			}
			
			//TODO fork by conditonal session is managed(reguraly checked or frashly crated):
			// if true send request
			// else first send isAlive then send request ...
			
			//if (response == null) {
			//	throw new TimeoutException(subscription.getSystem.getName + " subscriber: SendEventRequest timeout");
			//}
			
		} catch (final InvalidParameterException | BadPayloadException ex) {	
			
			logger.debug("Exception:", ex.getMessage());
			
		} catch (final Throwable ex) {			
			
			logger.debug("Exception:", ex.getMessage());			
		}
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------	
	private Session getSession() {
		
		//TODO create or get cached
		return null;
	}

}
