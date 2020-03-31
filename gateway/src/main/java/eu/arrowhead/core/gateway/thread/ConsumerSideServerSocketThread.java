package eu.arrowhead.core.gateway.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gateway.relay.GatewayRelayClient;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;

public class ConsumerSideServerSocketThread extends Thread implements MessageListener {
	
	//=================================================================================================
	// members

	private static final int BUFFER_SIZE = 1024;
	
	private static final Logger logger = LogManager.getLogger(ConsumerSideServerSocketThread.class);

	private final int port;
	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final PublicKey providerGatewayPublicKey;
	private final String queueId;
	private final int timeout;
	private final ConcurrentMap<String,ActiveSessionDTO> activeSessions;
	private final ConcurrentLinkedQueue<Integer> availablePorts;
	private final SSLProperties sslProperties;

	private MessageProducer sender;
	private SSLServerSocket sslServerSocket;
	private SSLSocket sslConsumerSocket;
	private OutputStream outConsumer;
	private boolean interrupted = false;
	private boolean initialized = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public ConsumerSideServerSocketThread(final ApplicationContext appContext, final int port, final GatewayRelayClient relayClient, final Session relaySession, final String providerGatewayPublicKey,
									      final String queueId, final int timeout, final String consumerName, final String serviceDefinition) {
		super();
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.isTrue(!Utilities.isEmpty(providerGatewayPublicKey), "provider gateway public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queue id is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(consumerName), "consumer name is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "service definition is null or blank.");

		this.port = port;
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.queueId = queueId;
		this.timeout = timeout;
		this.providerGatewayPublicKey = Utilities.getPublicKeyFromBase64EncodedString(providerGatewayPublicKey);
		this.activeSessions = appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentHashMap.class);
		this.availablePorts = appContext.getBean(CoreCommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE, ConcurrentLinkedQueue.class);
		this.sslProperties = appContext.getBean(SSLProperties.class);
		
		setName(consumerName + "." + serviceDefinition);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init(final MessageProducer sender) {
		logger.debug("init started...");
		
		Assert.notNull(sender, "sender is null.");
		
		this.sender = sender;
		
		final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(sslProperties);
		final SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
		try {
			sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
			sslServerSocket.setNeedClientAuth(true);
			sslServerSocket.setSoTimeout(timeout);
			this.initialized = true;
		} catch (final IOException ex) {
			logger.debug("Problem occurs in initializing gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			close();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message message) {
		logger.debug("onMessage started...");
		
		Assert.notNull(outConsumer, "Output stream is null.");
		try {
			if (isControlMessage(message)) {
				relayClient.handleCloseControlMessage(message, relaySession);
				closeAndInterrupt();
			} else {
				final byte[] bytes = relayClient.getBytesFromMessage(message, providerGatewayPublicKey);
				outConsumer.write(bytes);
			}
		} catch (final JMSException | ArrowheadException | IOException ex) {
			logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			closeAndInterrupt();
		}
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
		logger.debug("run started...");
		
		if (!initialized) {
			throw new IllegalStateException("Thread is not initialized.");
		}

		try {
			sslConsumerSocket = (SSLSocket) sslServerSocket.accept();
			final InputStream inConsumer = sslConsumerSocket.getInputStream();
			outConsumer = sslConsumerSocket.getOutputStream();
			
			while (true) {
				if (interrupted) {
					close();
					return;
				}
				
				final byte[] buffer = new byte[BUFFER_SIZE];
				final int size = inConsumer.read(buffer);
				
				if (size < 0) { // end of stream
					closeAndInterrupt();
				} else {
					final byte[] data = new byte[size];
					System.arraycopy(buffer, 0, data, 0, size);
					relayClient.sendBytes(relaySession, sender, providerGatewayPublicKey, data);
				}
			}
		} catch (final IOException | JMSException | ArrowheadException | ServiceConfigurationError | IllegalArgumentException ex) {
			logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			closeAndInterrupt();
		}		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private boolean isControlMessage(final Message message) throws JMSException {
		logger.debug("isControlMessage started...");
		
		final Destination destination = message.getJMSDestination();
		final Queue queue = (Queue) destination;
		
		return queue.getQueueName().endsWith(GatewayRelayClient.CONTROL_QUEUE_SUFFIX);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void close() {
		logger.debug("close started...");
		
		if (activeSessions != null && queueId != null) {
			activeSessions.remove(queueId);
		}
		
		if (sslConsumerSocket != null) {
			try {
				sslConsumerSocket.close();
			} catch (final IOException ex) {
				logger.debug("Error while closing socket: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
		}
		
		if (sslServerSocket != null) {
			try {
				sslServerSocket.close();
			} catch (final IOException ex) {
				logger.debug("Error while closing socket: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
		}
		
		if (availablePorts != null) {
			availablePorts.offer(port);
		}
		
		relayClient.closeConnection(relaySession);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void closeAndInterrupt() {
		close();
		interrupt();
	}
}