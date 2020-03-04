package eu.arrowhead.core.qos.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

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
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.thread.ReceiverSideRelayTestThread;
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
	private ApplicationContext appContext;

	@Autowired
	private SSLProperties sslProps;
	
	@Value(CoreCommonConstants.$RELAY_TEST_TIME_TO_REPEAT_WD)
	private byte noIteration;
	
	@Value(CoreCommonConstants.$RELAY_TEST_TIMEOUT_WD)
	private long timeout;
	
	@Value(CoreCommonConstants.$RELAY_TEST_MESSAGE_SIZE_WD)
	private int testMessageSize;
	
	@Value(CoreCommonConstants.$RELAY_TEST_LOG_MEASUREMENTS_IN_DB_WD)
	private boolean logIndividualMeasurements;
	
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
	}

	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO joinRelayTest(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("joinRelayTest started...");
		
		validateQoSRelayTestProposalRequestDTO(request);
		// TODO: find cloud and relay db objects
		final Cloud requesterCloud = null;
		final Relay relay = null;
		
		// TODO: find or create measurement record for the cloud, relay pair (and set to pending/new)
		
		final RelayRequestDTO relayRequest = request.getRelay();
		final Session session = getRelaySession(relayRequest);

		ReceiverSideRelayTestThread thread = null;
		try {
			thread = new ReceiverSideRelayTestThread(appContext, relayClient, session, requesterCloud, relay, request.getSenderQoSMonitorPublicKey(), noIteration, testMessageSize,
													 timeout, logIndividualMeasurements);
			final ProviderSideRelayInfo info = relayClient.initializeProviderSideRelay(session, thread);
			thread.init(info.getQueueId(), info.getMessageSender(), info.getControlMessageSender());
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
		// TODO: find cloud and relay db objects
		final Cloud requesterCloud = null;
		final Relay relay = null;

		final RelayRequestDTO relayRequest = request.getRelay();
		final Session session = getRelaySession(relayRequest);
		
		SenderSideRelayTestThread thread = null;
		try {
			thread = new SenderSideRelayTestThread(appContext, relayClient, session, requesterCloud, relay, request.getReceiverQoSMonitorPublicKey(), request.getQueueId(),
												   noIteration, testMessageSize, timeout, logIndividualMeasurements);
			ConsumerSideRelayInfo info = relayClient.initializeConsumerSideRelay(session, thread, request.getPeerName(), request.getQueueId());
			thread.init(info.getMessageSender(), info.getControlResponseMessageSender());
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
		try {
			return relayClient.createConnection(relay.getAddress(), relay.getPort(), relay.isSecure());
		} catch (final JMSException ex) {
			logger.debug("Exception occured while creating connection for address: {} and port {}:", relay.getAddress(), relay.getPort());
			logger.debug("Exception message: {}:", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			
			throw new ArrowheadException("Error while trying to connect relay at " + relay.getAddress() + ":" + relay.getPort(), ex);
		}
	}
}