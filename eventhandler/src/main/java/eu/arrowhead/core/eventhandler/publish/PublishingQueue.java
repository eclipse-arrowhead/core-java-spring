package eu.arrowhead.core.eventhandler.publish;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.arrowhead.common.dto.internal.EventPublishStartDTO;

public class PublishingQueue {

	//=================================================================================================
	// members
	
	private static final BlockingQueue<EventPublishStartDTO> publishingQueue = new LinkedBlockingQueue<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void put(final EventPublishStartDTO toPut) throws InterruptedException {
		publishingQueue.put(toPut);
	}

	//-------------------------------------------------------------------------------------------------
	public EventPublishStartDTO take() throws InterruptedException {
		return publishingQueue.take();
	}
}