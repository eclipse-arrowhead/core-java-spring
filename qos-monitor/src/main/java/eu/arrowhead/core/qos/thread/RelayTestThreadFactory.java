package eu.arrowhead.core.qos.thread;

import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

@Service
public class RelayTestThreadFactory {
	
	//=================================================================================================
	// members

	@Autowired
	private ApplicationContext appContext;

	@Value(CoreCommonConstants.$RELAY_TEST_TIME_TO_REPEAT_WD)
	private byte noIteration;
	
	@Value(CoreCommonConstants.$RELAY_TEST_TIMEOUT_WD)
	private long timeout;
	
	@Value(CoreCommonConstants.$RELAY_TEST_MESSAGE_SIZE_WD)
	private int testMessageSize;
	
	private GatewayRelayClient relayClient;

	private boolean initialized = false;
	
	private final Logger logger = LogManager.getLogger(RelayTestThreadFactory.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public void init(final GatewayRelayClient relayClient) {
		logger.debug("init started...");
		
		Assert.notNull(relayClient, "relay client is null.");
		
		this.relayClient = relayClient;
		this.initialized = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SenderSideRelayTestThread createSenderSideThread(final Session relaySession, final CloudResponseDTO targetCloud, final RelayResponseDTO relay,
															final String receiverQoSMonitorPublicKey, final String queueId) {
		logger.debug("createSenderSideThread started...");
		
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(targetCloud, "targetCloud is null.");
		Assert.notNull(relay, "relay is null");
		Assert.isTrue(!Utilities.isEmpty(receiverQoSMonitorPublicKey), "public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");
		
		if (!initialized) {
			throw new ArrowheadException("Thread factory is not initialized.");
		}

		return new SenderSideRelayTestThread(appContext, relayClient, relaySession, targetCloud, relay, receiverQoSMonitorPublicKey, queueId, noIteration, testMessageSize, timeout);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ReceiverSideRelayTestThread createReceiverSideThread(final Session relaySession, final CloudResponseDTO requesterCloud, final RelayResponseDTO relay,
																final String requesterQoSMonitorPublicKey) {
		logger.debug("createReceiverSideThread started...");

		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(requesterCloud, "requesterCloud is null.");
		Assert.notNull(relay, "relay is null");
		Assert.isTrue(!Utilities.isEmpty(requesterQoSMonitorPublicKey), "public key is null or blank.");
		
		if (!initialized) {
			throw new ArrowheadException("Thread factory is not initialized.");
		}

		return new ReceiverSideRelayTestThread(appContext, relayClient, relaySession, requesterCloud, relay, requesterQoSMonitorPublicKey, noIteration, testMessageSize, timeout);
	}
}