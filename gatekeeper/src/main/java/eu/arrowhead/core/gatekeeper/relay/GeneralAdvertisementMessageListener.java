package eu.arrowhead.core.gatekeeper.relay;

import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;

public class GeneralAdvertisementMessageListener implements Closeable, MessageListener {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GeneralAdvertisementMessageListener.class);
	private static int idCounter = 1;
	
	private final int id;
	private final ApplicationContext appContext;
	private final String relayHost;
	private final int relayPort;
	private final GatekeeperRelayClient relayClient;
	private final ThreadPoolExecutor threadPool;
	
	private boolean closed = false;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageListener(final ApplicationContext appContext, final String relayHost, final int relayPort, final GatekeeperRelayClient relayClient, final int threadPoolSize) {
		logger.debug("Constructor started...");
		
		Assert.notNull(appContext, "appContext is null.");
		Assert.isTrue(!Utilities.isEmpty(relayHost), "relayHost is null or blank.");
		Assert.isTrue(relayPort > CoreCommonConstants.SYSTEM_PORT_RANGE_MIN && relayPort < CoreCommonConstants.SYSTEM_PORT_RANGE_MAX, "relayPort is invalid.");
		Assert.notNull(relayClient, "Gatekeeper relay client is null.");
		Assert.isTrue(threadPoolSize > 0, "threadPoolSize must be a positive number.");
		
		this.id = idCounter++;
		this.appContext = appContext;
		this.relayHost = relayHost;
		this.relayPort = relayPort;
		this.relayClient = relayClient;
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
		logger.debug("GeneralAdvertisementMessageListener-{} started...", id);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message msg) {
		logger.debug("onMessage started...");
		
		if (!closed) {
			try {
				if (msg != null) {
					threadPool.execute(new GatekeeperTask(appContext, relayHost, relayPort, relayClient, msg)); 
				}
			} catch (final RejectedExecutionException ex) {
				logger.error("Message rejected at {}", ZonedDateTime.now());
			}
		} else {
			logger.trace("Message rejected at {} because listener is closed", ZonedDateTime.now());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void close() throws IOException {
		logger.debug("close started...");
		
		closed = true;
		threadPool.shutdownNow();
		
		logger.debug("GeneralAdvertisementMessageListener-{} stopped...", id);
	}
}