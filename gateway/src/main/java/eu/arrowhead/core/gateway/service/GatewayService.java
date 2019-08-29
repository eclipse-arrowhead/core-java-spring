package eu.arrowhead.core.gateway.service;

import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gateway.relay.GatewayRelayClient;
import eu.arrowhead.core.gateway.relay.GatewayRelayClientFactory;

@Component
public class GatewayService {
	
	//=================================================================================================
	// members
	
	@Value(CommonConstants.$GATEWAY_SOCKET_TIMEOUT_WD)
	private long gatewaySocketTimeout;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
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
		
		try {
			//TODO: create thread
			relayClient.initializeProviderSideRelay(session, null);
			//TODO: create active session
			//TODO: start thread
			
			//TODO: implement
		} catch (final Throwable t) {
			//TODO: handle exceptions
			relayClient.closeConnection(session);
		}
		
		return null;
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