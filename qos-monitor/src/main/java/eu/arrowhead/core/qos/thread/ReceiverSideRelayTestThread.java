package eu.arrowhead.core.qos.thread;

import java.security.PublicKey;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

public class ReceiverSideRelayTestThread extends Thread implements MessageListener {
	
	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(ReceiverSideRelayTestThread.class);
	
	private boolean interrupted = false;
	private boolean initialized = false;
	
	private final QoSDBService qosDBService;
	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final Cloud requesterCloud;
	private final Relay relay;
	private final PublicKey requesterQoSMonitorPublicKey;
	
	private final int noIteration;
	private final int testMessageSize;
	
	private String queueId;
	private MessageProducer sender;
	private MessageProducer controlSender;
	
	private final BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<Object>(1);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ReceiverSideRelayTestThread(final ApplicationContext appContext, final GatewayRelayClient relayClient, final Session relaySession, final Cloud requesterCloud,
									   final Relay relay, final String requesterQoSMonitorPublicKey, final int noIteration, final int testMessageSize) {
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(requesterCloud, "requesterCloud is null.");
		Assert.notNull(relay, "relay is null");
		Assert.isTrue(!Utilities.isEmpty(requesterQoSMonitorPublicKey), "public key is null or blank.");
		Assert.isTrue(noIteration > 0, "Number of iteration must be positive.");
		Assert.isTrue(testMessageSize > 0, "Test message's size must be positive.");
		
		this.qosDBService = appContext.getBean(QoSDBService.class);
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.requesterCloud = requesterCloud;
		this.relay = relay;
		this.requesterQoSMonitorPublicKey = Utilities.getPublicKeyFromBase64EncodedString(requesterQoSMonitorPublicKey);
		this.noIteration = noIteration;
		this.testMessageSize = testMessageSize;
		
		setName("TEST-RECEIVER-" + requesterCloud.getName() + "." + requesterCloud.getOperator() + "|" + relay.getAddress() + ":" + relay.getPort());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init(final String queueId, final MessageProducer sender, final MessageProducer controlSender) {
		logger.debug("init started...");
		
		Assert.isTrue(!Utilities.isEmpty(queueId), "Queue id is null or blank.");
		Assert.notNull(sender, "sender is null.");
		Assert.notNull(controlSender, "controlSender is null.");
		
		this.queueId = queueId;
		this.sender = sender;
		this.controlSender = controlSender;
		this.initialized = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message message) {
		// TODO Auto-generated method stub

	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void interrupt() {
		logger.debug("interrupt started...");
		
		super.interrupt();
		interrupted = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setInterrupted(final boolean interrupted) { this.interrupted = interrupted; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
	}
}