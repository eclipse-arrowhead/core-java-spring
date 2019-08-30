package eu.arrowhead.core.gateway.service;

import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.core.gateway.relay.ConsumerSideRelayInfo;
import eu.arrowhead.core.gateway.relay.GatewayRelayClient;
import eu.arrowhead.core.gateway.relay.GatewayRelayClientFactory;
import eu.arrowhead.core.gateway.relay.ProviderSideRelayInfo;
import eu.arrowhead.core.gateway.thread.ConsumerSideServerSocketThread;
import eu.arrowhead.core.gateway.thread.ProviderSideSocketThread;

@Component
public class GatewayService {
	
	//=================================================================================================
	// members
	
	@Value(CommonConstants.$GATEWAY_SOCKET_TIMEOUT_WD)
	private int gatewaySocketTimeout;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	private ConcurrentHashMap<String,ActiveSessionDTO> activeSessions;
	
	@Resource(name = CommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE)
	private ConcurrentLinkedQueue<Integer> availablePorts;
	
	@Autowired
	private ApplicationContext appContext;
	
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
	
		relayClient = GatewayRelayClientFactory.createGatewayRelayClient(serverCN, privateKey);	
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO connectProvider(final GatewayProviderConnectionRequestDTO request) {
		logger.debug("connectProvider started...");

		validateProviderConnectionRequest(request);
		
		final ZonedDateTime now = ZonedDateTime.now();
		final RelayRequestDTO relay = request.getRelay();
		final Session session = getRelaySession(relay);
		
		ProviderSideSocketThread thread = null;
		try {
			thread = new ProviderSideSocketThread(appContext, relayClient, session, request, gatewaySocketTimeout);
			final ProviderSideRelayInfo info = relayClient.initializeProviderSideRelay(session, thread);
			thread.init(info.getQueueId(), info.getMessageSender());
			final ActiveSessionDTO activeSession = new ActiveSessionDTO(info.getQueueId(), info.getPeerName(), request.getConsumer(), request.getConsumerCloud(), request.getProvider(),
																		request.getProviderCloud(), request.getServiceDefinition(), request.getRelay(), Utilities.convertZonedDateTimeToUTCString(now),
																		null);
			activeSessions.put(info.getQueueId(), activeSession);
			thread.start();
			
			return new GatewayProviderConnectionResponseDTO(info.getQueueId(), info.getPeerName(), Base64.getEncoder().encodeToString(myPublicKey.getEncoded()));
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
	public int connectConsumer(final GatewayConsumerConnectionRequestDTO request) {
		logger.debug("connectConsumer started...");
		
		//TODO: request check
		
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
			thread.init(info.getMessageSender());
			thread.start();
			
			return serverPort;
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
		try {
			return relayClient.createConnection(relay.getAddress(), relay.getPort());
		} catch (final JMSException ex) {
			logger.debug("Exception occured while creating connection for address: {} and port {}:", relay.getAddress(), relay.getPort());
			logger.debug("Exception message: {}:", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			
			throw new ArrowheadException("Error while trying to connect relay at " + relay.getAddress() + ":" + relay.getPort(), ex);
		}
	}
}