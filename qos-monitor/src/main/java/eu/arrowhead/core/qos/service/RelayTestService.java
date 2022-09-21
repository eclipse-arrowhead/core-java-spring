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

package eu.arrowhead.core.qos.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.thread.ReceiverSideRelayTestThread;
import eu.arrowhead.core.qos.thread.RelayTestThreadFactory;
import eu.arrowhead.core.qos.thread.SenderSideRelayTestThread;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.GatewayRelayClientFactory;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@Service
public class RelayTestService {
	
	//=================================================================================================
	// members
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	@Autowired
	private SSLProperties sslProps;
	
	@Autowired
	private QoSMonitorDriver qosMonitorDriver;
	
	@Autowired
	private RelayTestThreadFactory threadFactory;
	
	@Autowired
	private QoSDBService qosDBService;
	
	private final Logger logger = LogManager.getLogger(RelayTestService.class);
	
	private GatewayRelayClient relayClient;
	private PublicKey myPublicKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	@EventListener
	@Order(15) // to make sure QoSMonitorApplicationInitListener finished before this method is called (the common name and the keys are added to the context in the init listener)
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		logger.debug("onApplicationEvent started...");
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)) {
			throw new ArrowheadException("Server's certificate not found.");
		}
		final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Server's public key is not found.");
		}
		myPublicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)) {
			throw new ArrowheadException("Server's private key is not found.");
		}
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);
	
		relayClient = GatewayRelayClientFactory.createGatewayRelayClient(serverCN, privateKey, sslProps);
		threadFactory.init(relayClient);
	}

	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO joinRelayTest(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("joinRelayTest started...");
		
		validateQoSRelayTestProposalRequestDTO(request);
		final CloudWithRelaysResponseDTO requesterCloud = qosMonitorDriver.queryGatekeeperCloudInfo(request.getRequesterCloud().getOperator(), request.getRequesterCloud().getName());
		final RelayRequestDTO relayRequest = request.getRelay();
		final RelayResponseDTO relay = findRelayResponseDTO(requesterCloud, relayRequest.getAddress(), relayRequest.getPort());
		
		qosDBService.getOrCreateInterRelayMeasurement(requesterCloud, relay, QoSMeasurementType.RELAY_ECHO);
		
		final Session session = getRelaySession(relayRequest);

		ReceiverSideRelayTestThread thread = null;
		try {
			thread = threadFactory.createReceiverSideThread(session, requesterCloud, relay, request.getSenderQoSMonitorPublicKey()); 
			final ProviderSideRelayInfo info = relayClient.initializeProviderSideRelay(session, thread);
			thread.init(info.getQueueId(), info.getMessageSender(), info.getControlMessageSender(), info.getMessageConsumer(), info.getControlMessageConsumer());
			thread.start();

			return new QoSRelayTestProposalResponseDTO(info.getQueueId(), info.getPeerName(), Base64.getEncoder().encodeToString(myPublicKey.getEncoded()));
		} catch (final JMSException ex) {
			relayClient.closeConnection(session);
			throw new ArrowheadException("Error occured when initialize relay communication.", HttpStatus.SC_BAD_GATEWAY, ex);
		} catch (final ArrowheadException ex) {
			relayClient.closeConnection(session);

			if (thread != null && thread.isInitialized()) {
				thread.setInterrupted(true);
			}
			
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public void initRelayTest(final QoSMonitorSenderConnectionRequestDTO request) {
		logger.debug("initRelayTest started...");
		
		validateQoSMonitorSenderConnectionRequestDTO(request);
		final CloudWithRelaysResponseDTO targetCloud = qosMonitorDriver.queryGatekeeperCloudInfo(request.getTargetCloud().getOperator(), request.getTargetCloud().getName());
		final RelayRequestDTO relayRequest = request.getRelay();
		final RelayResponseDTO relay = findRelayResponseDTO(targetCloud, relayRequest.getAddress(), relayRequest.getPort());

		final Session session = getRelaySession(relayRequest);
		
		SenderSideRelayTestThread thread = null;
		try {
			thread = threadFactory.createSenderSideThread(session, targetCloud, relay, request.getReceiverQoSMonitorPublicKey(), request.getQueueId()); 
			final ConsumerSideRelayInfo info = relayClient.initializeConsumerSideRelay(session, thread, request.getPeerName(), request.getQueueId());
			thread.init(info.getMessageSender(), info.getControlResponseMessageSender(), info.getMessageConsumer(), info.getControlRequestMessageConsumer());
			thread.start();
		} catch (final JMSException ex) {
			relayClient.closeConnection(session);
			throw new ArrowheadException("Error occured when initialize relay communication.", HttpStatus.SC_BAD_GATEWAY, ex);
		} catch (final ArrowheadException ex) {
			relayClient.closeConnection(session);

			if (thread != null && thread.isInitialized()) {
				thread.setInterrupted(true);
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementListResponseDTO getInterRelayEchoMeasurements(final CloudRequestDTO request) {
		logger.debug("getInterRelayEchoMeasurements started...");
		
		final List<QoSInterRelayMeasurement> measurements = qosDBService.getInterRelayMeasurementByCloud(validateAndGetCloud(request));
		
		final List<QoSInterRelayEchoMeasurement> echoMeasurements = new ArrayList<>();
		for (final QoSInterRelayMeasurement measurement : measurements) {
			final Optional<QoSInterRelayEchoMeasurement> optional = qosDBService.getInterRelayEchoMeasurementByMeasurement(measurement);
			if (optional.isPresent()) {
				echoMeasurements.add(optional.get());
			}
		}
		
		return DTOConverter.convertQoSInterRelayEchoMeasurementPageToQoSInterRelayEchoMeasurementListResponseDTO(new PageImpl<>(echoMeasurements));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSRelayTestProposalRequestDTO(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("validateQoSRelayTestProposalRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("Relay test proposal is null.");
		}
		
		validateRelayRequest(request.getRelay());
		validateCloudRequest(request.getRequesterCloud());

		if (Utilities.isEmpty(request.getSenderQoSMonitorPublicKey())) {
			throw new InvalidParameterException("Sender QoS Monitor's public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSMonitorSenderConnectionRequestDTO(final QoSMonitorSenderConnectionRequestDTO request) {
		logger.debug("validateQoSMonitorSenderConnectionRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("Connection request is null.");
		}
		
		validateRelayRequest(request.getRelay());
		validateCloudRequest(request.getTargetCloud());

		if (Utilities.isEmpty(request.getQueueId())) {
			throw new InvalidParameterException("Queue id is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getPeerName())) {
			throw new InvalidParameterException("Peer id is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getReceiverQoSMonitorPublicKey())) {
			throw new InvalidParameterException("Receiver QoS Monitor's public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudRequest(final CloudRequestDTO cloud) {
		logger.debug("validateCloudRequest started...");
		
		if (cloud == null) {
			throw new InvalidParameterException("Cloud is null");
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new InvalidParameterException("Cloud operator is null or blank");
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new InvalidParameterException("Cloud name is null or empty");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequest(final RelayRequestDTO relay) {
		logger.debug("validateRelayRequest started...");
		
		if (relay == null) {
			throw new InvalidParameterException("relay is null");
		}
			
		if (Utilities.isEmpty(relay.getAddress())) {
			throw new InvalidParameterException("Relay address is null or blank");
		}
		
		if (relay.getPort() == null) {
			throw new InvalidParameterException("Relay port is null");
		}
		
		final int validatedPort = relay.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		if (Utilities.isEmpty(relay.getType())) {
			throw new InvalidParameterException("Relay type is null or blank");
		}
		
		final RelayType type = Utilities.convertStringToRelayType(relay.getType());
		if (type == null || type == RelayType.GATEKEEPER_RELAY) {
			throw new InvalidParameterException("Relay type is invalid");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Session getRelaySession(final RelayRequestDTO relay) {
		logger.debug("getRelaySession started...");
		
		try {
			return relayClient.createConnection(relay.getAddress(), relay.getPort(), relay.isSecure());
		} catch (final JMSException ex) {
			logger.debug("Exception occured while creating connection for address: {} and port {}:", relay.getAddress(), relay.getPort());
			logger.debug("Exception message: {}:", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			
			throw new ArrowheadException("Error while trying to connect relay at " + relay.getAddress() + ":" + relay.getPort(), HttpStatus.SC_BAD_GATEWAY, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private RelayResponseDTO findRelayResponseDTO(final CloudWithRelaysResponseDTO cloud, final String address, final int port) {
		logger.debug("findRelayResponseDTO started...");

		if (cloud.getGatewayRelays() != null) {
			for (final RelayResponseDTO relay : cloud.getGatewayRelays()) {
				if (address.equals(relay.getAddress()) && port == relay.getPort()) {
					return relay;
				}
			}
		}
		
		throw new ArrowheadException("Can't find relay: " + address + ":" + port);
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO validateAndGetCloud(final CloudRequestDTO request) {
		logger.debug("validateAndGetCloud started...");
		
		if (request == null) {
			throw new InvalidParameterException("CloudRequestDTO is null.");
		}		
		
		if (Utilities.isEmpty(request.getOperator())) {
			throw new InvalidParameterException("Cloud operator is null or blank");
		}
		
		if (Utilities.isEmpty(request.getName())) {
			throw new InvalidParameterException("Cloud name is null or empty");
		}
		
		return qosMonitorDriver.queryGatekeeperCloudInfo(request.getOperator(), request.getName());
	}
}