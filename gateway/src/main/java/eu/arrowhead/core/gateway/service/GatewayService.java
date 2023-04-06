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

package eu.arrowhead.core.gateway.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.core.gateway.thread.ConsumerSideServerSocketThread;
import eu.arrowhead.core.gateway.thread.ProviderSideSocketThreadHandler;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.GatewayRelayClientFactory;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@Component
public class GatewayService {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$GATEWAY_SOCKET_TIMEOUT_WD)
	private int gatewaySocketTimeout;
	
	@Value(CoreCommonConstants.$GATEWAY_PROVIDER_SIDE_MAX_REQUEST_PER_SOCKET)
	private int gatewayProviderSideMaxRequestPerSocket;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	private ConcurrentMap<String,ActiveSessionDTO> activeSessions;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_CONSUMER_SIDE_SOCKET_THREAD_MAP)
	private ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP)
	private ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers;
	
	@Resource(name = CoreCommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE)
	private ConcurrentLinkedQueue<Integer> availablePorts;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Autowired
	private SSLProperties sslProps;
	
	private final Logger logger = LogManager.getLogger(GatewayService.class);
	
	private GatewayRelayClient relayClient;
	private PublicKey myPublicKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	@EventListener
	@Order(15) // to make sure GatewayApplicationInitListener finished before this method is called (the common name and the keys are added to the context in the init listener)
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
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO connectProvider(final GatewayProviderConnectionRequestDTO request) {
		logger.debug("connectProvider started...");

		validateProviderConnectionRequest(request);
		
		final ZonedDateTime now = ZonedDateTime.now();
		final RelayRequestDTO relay = request.getRelay();
		final Session session = getRelaySession(relay);
		
		ProviderSideSocketThreadHandler handler = null;
		try {
			handler = new ProviderSideSocketThreadHandler(appContext, relayClient, session, request, gatewaySocketTimeout, gatewayProviderSideMaxRequestPerSocket);
			final ProviderSideRelayInfo info = relayClient.initializeProviderSideRelay(session, handler);
			handler.init(info.getQueueId(), info.getMessageSender(), info.getControlMessageSender(), info.getMessageConsumer(), info.getControlMessageConsumer());
			activeProviderSideSocketThreadHandlers.put(info.getQueueId(), handler);
			final ActiveSessionDTO activeSession = new ActiveSessionDTO(info.getQueueId(), info.getPeerName(), request.getConsumer(), request.getConsumerCloud(), request.getProvider(),
																		request.getProviderCloud(), request.getServiceDefinition(), request.getRelay(), Utilities.convertZonedDateTimeToUTCString(now),
																		null);
			activeSessions.put(info.getQueueId(), activeSession);
			
			return new GatewayProviderConnectionResponseDTO(info.getQueueId(), info.getPeerName(), Base64.getEncoder().encodeToString(myPublicKey.getEncoded()));
		} catch (final JMSException ex) {
			relayClient.closeConnection(session);
			throw new ArrowheadException("Error occured when initialize relay communication.", HttpStatus.SC_BAD_GATEWAY, ex);
		} catch (final ArrowheadException ex) {
			relayClient.closeConnection(session);
			
			if (handler != null) {
				handler.close();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public int connectConsumer(final GatewayConsumerConnectionRequestDTO request) {
		logger.debug("connectConsumer started...");
		
		validateConsumerConnectionRequest(request);
		
		final ZonedDateTime now = ZonedDateTime.now();
		final Integer serverPort = availablePorts.poll();
		
		if (serverPort == null) { // no free port
			throw new UnavailableServerException("No available gateway port.");
		}
		
		final ActiveSessionDTO activeSession = new ActiveSessionDTO(request.getQueueId(), request.getPeerName(), request.getConsumer(), request.getConsumerCloud(), request.getProvider(), 
																	request.getProviderCloud(), request.getServiceDefinition(), request.getRelay(), Utilities.convertZonedDateTimeToUTCString(now),
																	serverPort);
		activeSessions.put(request.getQueueId(), activeSession);
		
		final RelayRequestDTO relay = request.getRelay();
		final Session session = getRelaySession(relay);

		ConsumerSideServerSocketThread thread = null;
		try {
			thread = new ConsumerSideServerSocketThread(appContext, serverPort, relayClient, session, request.getProviderGWPublicKey(), request.getQueueId(), gatewaySocketTimeout, 
														request.getConsumer().getSystemName(), request.getServiceDefinition());
			final ConsumerSideRelayInfo info = relayClient.initializeConsumerSideRelay(session, thread, request.getPeerName(), request.getQueueId());
			thread.init(info.getMessageSender(), info.getControlResponseMessageSender(), info.getMessageConsumer(), info.getControlRequestMessageConsumer());
			thread.start();
			
			activeConsumerSideSocketThreads.put(request.getQueueId(), thread);			
			return serverPort;
			
		} catch (final JMSException ex) {
			activeSessions.remove(request.getQueueId());
			relayClient.closeConnection(session);
			throw new ArrowheadException("Error occured when initialize relay communication.", HttpStatus.SC_BAD_GATEWAY, ex);
			
		} catch (final ArrowheadException ex) {
			activeSessions.remove(request.getQueueId());
			relayClient.closeConnection(session);
			
			if (thread != null && thread.isInitialized()) {
				thread.setInterrupted(true);
			}
			
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public void closeSession(final ActiveSessionDTO sessionDTO) {
		logger.debug("closeSession started...");
		
		validateCloseSessionRequest(sessionDTO);
		
		final Session session = getRelaySession(sessionDTO.getRelay());
		
		try {
			final ControlRelayInfo controlRelayInfo = relayClient.initializeControlRelay(session, sessionDTO.getPeerName(), sessionDTO.getQueueId());
			relayClient.sendCloseControlMessage(session, controlRelayInfo.getControlRequestMessageSender(), sessionDTO.getQueueId());
			relayClient.sendCloseControlMessage(session, controlRelayInfo.getControlResponseMessageSender(), sessionDTO.getQueueId());
			activeSessions.remove(sessionDTO.getQueueId());
		} catch (final JMSException ex) {
			throw new ArrowheadException("Error occured when initialize relay communication.", HttpStatus.SC_BAD_GATEWAY, ex);
		} finally {
			relayClient.closeConnection(session);			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public String closeSession(final int port) { 
		logger.debug("closeSession started...");
		
		final ActiveSessionDTO session = findSessionForPort(port);
		if (session == null) {
			return "Active session not found.";
		}

		try {
			closeSession(session);
			
			return null;
		} catch (final ArrowheadException ex) {
			return ex.getMessage();
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validateProviderConnectionRequest(final GatewayProviderConnectionRequestDTO request) {
		logger.debug("validateProviderConnectionRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("request is null.");
		}
		
		validateRelay(request.getRelay());
		validateSystem(request.getConsumer());
		validateSystem(request.getProvider());
		validateCloud(request.getConsumerCloud());
		validateCloud(request.getProviderCloud());
		
		if (Utilities.isEmpty(request.getServiceDefinition())) {
			throw new InvalidParameterException("Service definition is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getConsumerGWPublicKey())) {
			throw new InvalidParameterException("Consumer gateway public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateConsumerConnectionRequest(final GatewayConsumerConnectionRequestDTO request) {
		logger.debug("validateConsumerConnectionRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("request is null.");
		}
		
		validateRelay(request.getRelay());
		validateSystem(request.getConsumer());
		validateSystem(request.getProvider());
		validateCloud(request.getConsumerCloud());
		validateCloud(request.getProviderCloud());
		
		if (Utilities.isEmpty(request.getServiceDefinition())) {
			throw new InvalidParameterException("Service definition is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getProviderGWPublicKey())) {
			throw new InvalidParameterException("Provider gateway public key is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getQueueId())) {
			throw new InvalidParameterException("Queue id is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getPeerName())) {
			throw new InvalidParameterException("Peer name is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloseSessionRequest(final ActiveSessionDTO sessionDTO) {
		logger.debug("validateCloseSessionRequest started...");
		
		if (sessionDTO == null) {
			throw new InvalidParameterException("ActiveSessionDTO is null.");
		}
		
		if (Utilities.isEmpty(sessionDTO.getPeerName())) {
			throw new InvalidParameterException("peerName is null or blank.");
		}
		
		if (Utilities.isEmpty(sessionDTO.getQueueId())) {
			throw new InvalidParameterException("queueId is null or blank.");
		}
		
		validateRelay(sessionDTO.getRelay());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelay(final RelayRequestDTO relay) {
		logger.debug("validateRelay started...");
		
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
	private void validateSystem(final SystemRequestDTO system) {
		logger.debug("validateSystem started...");
		
		if (system == null) {
			throw new InvalidParameterException("System is null");
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new InvalidParameterException("System name is null or blank");
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new InvalidParameterException("System address is null or blank");
		}
		
		if (system.getPort() == null) {
			throw new InvalidParameterException("System port is null");
		}
		
		final int validatedPort = system.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloud(final CloudRequestDTO cloud) {
		logger.debug("validateCloud started...");
		
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
	private Session getRelaySession(final RelayRequestDTO relay) {
		logger.debug("getRelaySession started...");
		
		try {
			return relayClient.createConnection(relay.getAddress(), relay.getPort(), relay.isSecure());
		} catch (final JMSException ex) {
			logger.debug("Exception occured while creating connection for address: {} and port {}:", relay.getAddress(), relay.getPort());
			logger.debug("Exception message: {}:", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			
			throw new ArrowheadException("Error while trying to connect relay at " + relay.getAddress() + ":" + relay.getPort(), ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private ActiveSessionDTO findSessionForPort(int port) {
		logger.debug("findSessionForPort started...");
		
		for (final ActiveSessionDTO session : activeSessions.values()) {
			if (port == session.getConsumerServerSocketPort().intValue()) {
				return session;
			}
		}
		
		return null;
	}
}