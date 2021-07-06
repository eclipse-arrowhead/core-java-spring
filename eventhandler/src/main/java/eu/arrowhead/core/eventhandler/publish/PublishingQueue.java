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