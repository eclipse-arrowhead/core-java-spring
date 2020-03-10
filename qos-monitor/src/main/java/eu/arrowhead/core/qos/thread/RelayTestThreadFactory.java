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
	
	private final Logger logger = LogManager.getLogger(RelayTestThreadFactory.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SenderSideRelayTestThread createSenderSideThread(final GatewayRelayClient relayClient, final Session relaySession, final CloudResponseDTO targetCloud, 
															final RelayResponseDTO relay, final String receiverQoSMonitorPublicKey, final String queueId) {
		logger.debug("createSenderSideThread started...");
		
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(targetCloud, "targetCloud is null.");
		Assert.notNull(relay, "relay is null");
		Assert.isTrue(!Utilities.isEmpty(receiverQoSMonitorPublicKey), "public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");

		return new SenderSideRelayTestThread(appContext, relayClient, relaySession, targetCloud, relay, receiverQoSMonitorPublicKey, queueId, noIteration, testMessageSize, timeout);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ReceiverSideRelayTestThread createReceiverSideThread(final GatewayRelayClient relayClient, final Session relaySession, final CloudResponseDTO requesterCloud,
		   	   													final RelayResponseDTO relay, final String requesterQoSMonitorPublicKey) {
		logger.debug("createReceiverSideThread started...");

		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(requesterCloud, "requesterCloud is null.");
		Assert.notNull(relay, "relay is null");
		Assert.isTrue(!Utilities.isEmpty(requesterQoSMonitorPublicKey), "public key is null or blank.");

		return new ReceiverSideRelayTestThread(appContext, relayClient, relaySession, requesterCloud, relay, requesterQoSMonitorPublicKey, noIteration, testMessageSize, timeout);
	}
}