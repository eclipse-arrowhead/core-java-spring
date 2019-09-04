package eu.arrowhead.core.gateway.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gateway.relay.GatewayRelayClient;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;

public class ProviderSideSocketThread extends Thread implements MessageListener {
	
	//=================================================================================================
	// members
	
	private static final int BUFFER_SIZE = 1024;
	
	private static final Logger logger = LogManager.getLogger(ProviderSideSocketThread.class);
	
	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final GatewayProviderConnectionRequestDTO connectionRequest;
	private final PublicKey consumerGatewayPublicKey;
	private final int timeout;
	private final ConcurrentHashMap<String,ActiveSessionDTO> activeSessions;
	private final SSLProperties sslProperties;
	
	private String queueId;
	private MessageProducer sender;
	private SSLSocket sslProviderSocket;
	private InputStream inProvider;
	private OutputStream outProvider;
	private boolean interrupted = false;
	private boolean initialized = false;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public ProviderSideSocketThread(final ApplicationContext appContext, final GatewayRelayClient relayClient, final Session relaySession, final GatewayProviderConnectionRequestDTO connectionRequest,
									final int timeout) {
		super();
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		
		validateConnectionRequest(connectionRequest);
		
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.connectionRequest = connectionRequest;
		this.timeout = timeout;
		this.consumerGatewayPublicKey = Utilities.getPublicKeyFromBase64EncodedString(this.connectionRequest.getConsumerGWPublicKey());
		this.activeSessions = appContext.getBean(CommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentHashMap.class);
		this.sslProperties = appContext.getBean(SSLProperties.class);
		
		setName(connectionRequest.getProvider().getSystemName() + "." + connectionRequest.getServiceDefinition());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init(final String queueId, final MessageProducer sender) {
		logger.debug("init started...");
		
		Assert.isTrue(!Utilities.isEmpty(queueId), "Queue id is null or blank.");
		Assert.notNull(sender, "sender is null.");
		
		this.queueId = queueId;
		this.sender = sender;
		this.initialized = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message message) {
		logger.debug("onMessage started...");
		
		Assert.notNull(outProvider, "Output stream is null.");
		try {
			if (isControlMessage(message)) {
				relayClient.handleCloseControlMessage(message, relaySession);
				closeAndInterrupt();
			} else {
				final byte[] bytes = relayClient.getBytesFromMessage(message, consumerGatewayPublicKey);
				outProvider.write(bytes);
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
			final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(sslProperties);
			final SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			sslProviderSocket = (SSLSocket) socketFactory.createSocket(connectionRequest.getProvider().getAddress(), connectionRequest.getProvider().getPort().intValue());
			sslProviderSocket.setSoTimeout(timeout);
			inProvider = sslProviderSocket.getInputStream();
			outProvider = sslProviderSocket.getOutputStream();
			
			while (true) {
				if (interrupted) {
					close();
					return;
				}
				
				final byte[] buffer = new byte[BUFFER_SIZE];
				final int size = inProvider.read(buffer);
				
				if (size < 0) { // end of stream
					closeAndInterrupt();
				} else {
					final byte[] data = new byte[size];
					System.arraycopy(buffer, 0, data, 0, size);
					relayClient.sendBytes(relaySession, sender, consumerGatewayPublicKey, data);
				}
			}
		} catch (final IOException | JMSException | ArrowheadException | ServiceConfigurationError ex) {
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
	private void validateConnectionRequest(final GatewayProviderConnectionRequestDTO connectionRequest) {
		logger.debug("validateConnectionRequest started...");
		
		Assert.notNull(connectionRequest, "connectionRequest is null.");
		Assert.notNull(connectionRequest.getProvider(), "provider is null.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getProvider().getSystemName()), "provider name is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getProvider().getAddress()), "provider address is null or blank.");
		Assert.notNull(connectionRequest.getProvider().getPort(), "provider port is null.");
		final int port = connectionRequest.getProvider().getPort().intValue();
		Assert.isTrue(port > CommonConstants.SYSTEM_PORT_RANGE_MIN && port < CommonConstants.SYSTEM_PORT_RANGE_MAX, "provider port is invalid.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getProvider().getAuthenticationInfo()), "provider authentication info is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getServiceDefinition()), "Service definition is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getConsumerGWPublicKey()), "Consumer gateway public key is null or blank.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private void close() {
		logger.debug("close started...");
		
		if (queueId != null) {
			activeSessions.remove(queueId);
		}
		
		if (sslProviderSocket != null) {
			try {
				sslProviderSocket.close();
			} catch (final IOException ex) {
				logger.debug("Error while closing socket: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
		}
		
		relayClient.closeConnection(relaySession);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void closeAndInterrupt() {
		close();
		interrupt();
	}
}