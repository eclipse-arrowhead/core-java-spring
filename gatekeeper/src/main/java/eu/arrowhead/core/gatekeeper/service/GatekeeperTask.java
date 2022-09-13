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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GeneralAdvertisementMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayRequest;

public class GatekeeperTask implements Runnable {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GatekeeperTask.class);

	private final String relayHost;
	private final int relayPort;
	private final boolean securedRelay;
	private final GatekeeperRelayClient relayClient;
	private final Message msg;
	
	private final GatekeeperService gatekeeperService; 
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatekeeperTask(final ApplicationContext appContext, final String relayHost, final int relayPort, final boolean securedRelay, final GatekeeperRelayClient relayClient, final Message msg) {
		logger.debug("Constructor started...");
		
		Assert.notNull(appContext, "appContext is null.");
		Assert.isTrue(!Utilities.isEmpty(relayHost), "relayHost is null or blank.");
		Assert.isTrue(relayPort > CommonConstants.SYSTEM_PORT_RANGE_MIN && relayPort < CommonConstants.SYSTEM_PORT_RANGE_MAX, "relayPort is invalid.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(msg, "Message is null");
		
		this.relayHost = relayHost;
		this.relayPort = relayPort;
		this.securedRelay = securedRelay;
		this.relayClient = relayClient;
		this.msg = msg;
		this.gatekeeperService = appContext.getBean(GatekeeperService.class);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		logger.debug("run started...");
		
		if (Thread.currentThread().isInterrupted()) {
			logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
			return;
		}
		
		try {
			final GeneralAdvertisementMessageDTO gaMsg = relayClient.getGeneralAdvertisementMessage(msg);
			if (gaMsg != null) { // means this gatekeeper is the recipient
				handleMessage(gaMsg);
			}
		} catch (final JMSException | ArrowheadException ex) {
			logger.debug("Error while extracting message from General Advertisement topic: {}", ex.getMessage());
			logger.debug("Exception:", ex);
			
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void handleMessage(final GeneralAdvertisementMessageDTO gaMsg) {
		logger.debug("handleMessage started...");

		Session session = null;
		try {
			session = relayClient.createConnection(relayHost, relayPort, securedRelay);
			final GatekeeperRelayRequest request = relayClient.sendAcknowledgementAndReturnRequest(session, gaMsg);
			final Object response = handleRequest(request);
			relayClient.sendResponse(session, request, response);
		} catch (final JMSException | ArrowheadException ex) {
			logger.debug("Error while communicating with an other gatekeeper: {}", ex.getMessage());
			logger.debug("Exception:", ex);
		} finally {
			relayClient.closeConnection(session);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Object handleRequest(final GatekeeperRelayRequest request) {
		logger.debug("handleRequest started...");
		try {
			switch (request.getMessageType()) {
			case CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL: 
				return gatekeeperService.doGSDPoll(request.getGSDPollRequest());
			case CoreCommonConstants.RELAY_MESSAGE_TYPE_MULTI_GSD_POLL:
				return gatekeeperService.doMultiGSDPoll(request.getGSDMultiPollRequest());
			case CoreCommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL:
				return gatekeeperService.doICN(request.getICNProposalRequest());
			case CoreCommonConstants.RELAY_MESSAGE_TYPE_ACCESS_TYPE:
				return gatekeeperService.returnAccessType();
			case CoreCommonConstants.RELAY_MESSAGE_TYPE_SYSTEM_ADDRESS_LIST:
				return gatekeeperService.doSystemAddressCollection();
			case CoreCommonConstants.RELAY_MESSAGE_TYPE_QOS_RELAY_TEST:
				return gatekeeperService.joinRelayTest(request.getQoSRelayTestProposalRequest());
			default:
				throw new BadPayloadException("Invalid message type: " + request.getMessageType());
			}
		} catch (final ArrowheadException ex) {
			logger.debug("Error while handle request from an other gatekeeper: {}", ex.getMessage());
			logger.debug("Exception:", ex);
			return convertExceptionToErrorMessageDTO(ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private ErrorMessageDTO convertExceptionToErrorMessageDTO(final ArrowheadException ex) {
		logger.debug("convertExceptionToErrorMessageDTO started...");
		
		final HttpStatus status = Utilities.calculateHttpStatusFromArrowheadException(ex);
		final ErrorMessageDTO dto = new ErrorMessageDTO(ex);
		if (ex.getErrorCode() <= 0) {
			dto.setErrorCode(status.value());
		}
		
		return dto;
	}
}