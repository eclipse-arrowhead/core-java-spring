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

package eu.arrowhead.core.gatekeeper.service;

import java.util.concurrent.BlockingQueue;

import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.GeneralRelayRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

public class AccessTypeCollectionTask implements Runnable {

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(AccessTypeCollectionTask.class);
	
	private final GatekeeperRelayClient relayClient;
	private final Session session;
	private final String recipientCloudName;
	private final String recipientCloudOperator;
	private final String recipientCloudCN;
	private final String recipientCloudPublicKey;
	private final BlockingQueue<ErrorWrapperDTO> queue;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public AccessTypeCollectionTask(final GatekeeperRelayClient relayClient, final Session session, final String recipientCloudName, final String recipientCloudOperator, final String recipientCloudCN, final String recipientCloudPublicKey,
									final BlockingQueue<ErrorWrapperDTO> queue) {
		Assert.notNull(relayClient, "relayClient is null");
		Assert.notNull(session, "session is null");
		Assert.isTrue(!Utilities.isEmpty(recipientCloudName), "recipientCloudName is empty");
		Assert.isTrue(!Utilities.isEmpty(recipientCloudOperator), "recipientCloudOperator is empty");
		Assert.isTrue(!Utilities.isEmpty(recipientCloudCN), "recipientCloudCN is empty");
		Assert.isTrue(!Utilities.isEmpty(recipientCloudPublicKey), "recipientCloudPublicKey is empty");
		Assert.notNull(queue, "queue is null");
		
		this.relayClient = relayClient;
		this.session = session;
		this.recipientCloudName = recipientCloudName;
		this.recipientCloudOperator = recipientCloudOperator;
		this.recipientCloudCN = recipientCloudCN;
		this.recipientCloudPublicKey = recipientCloudPublicKey;
		this.queue = queue;		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		try {			
			if (Thread.currentThread().isInterrupted()) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				queue.add(new CloudAccessResponseDTO());
				return;
			}
			
			final GeneralAdvertisementResult result = relayClient.publishGeneralAdvertisement(session, recipientCloudCN, recipientCloudPublicKey);
			if (result == null) {
				throw new TimeoutException(recipientCloudCN + " cloud: GeneralAdvertisementResult timeout");
			}
			
			final GatekeeperRelayResponse response = relayClient.sendRequestAndReturnResponse(session, result, new GeneralRelayRequestDTO(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACCESS_TYPE));
			if (response == null) {
				throw new TimeoutException(recipientCloudCN + " cloud: GatekeeperRelayResponse timeout");
			}
			
			final boolean directAccess = response.getAccessTypeResponse().isDirectAccess();
			queue.add(new CloudAccessResponseDTO(recipientCloudName, recipientCloudOperator, directAccess));
		} catch (final InvalidParameterException | BadPayloadException ex) {	
			// We forward this two type of arrowhead exception to the caller
			logger.debug("Exception: {}", ex.getMessage());
			queue.add(new ErrorMessageDTO(ex));
		} catch (final Throwable ex) {			
			// Must catch all throwable, otherwise the blocking queue would block the whole process
			logger.debug("Exception: {}", ex.getMessage());
			
			if (ex instanceof ArrowheadException) {
				queue.add(new ErrorMessageDTO((ArrowheadException) ex));
			} else {
				queue.add(new ErrorMessageDTO(new ArrowheadException("Exception in AccessTypeCollectionTask", ex)));
			} 			
		}		
	}
}
