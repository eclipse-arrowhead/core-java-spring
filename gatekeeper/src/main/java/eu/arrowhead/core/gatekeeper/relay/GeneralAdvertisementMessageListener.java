package eu.arrowhead.core.gatekeeper.relay;

import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

public class GeneralAdvertisementMessageListener implements Closeable, MessageListener {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GeneralAdvertisementMessageListener.class);
	private static int ID_COUNTER = 1;
	
	private final int id;
	private final ApplicationContext appContext;
	private final Session session;
	private final GatekeeperRelayClient relayClient;
	private final ThreadPoolExecutor threadPool;
	
	private boolean closed = false;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageListener(final ApplicationContext appContext, final Session session, final GatekeeperRelayClient relayClient, final int threadPoolSize) {
		logger.debug("Constructor started...");
		
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(session, "Session is null.");
		Assert.notNull(relayClient, "Gatekeeper relay client is null.");
		Assert.isTrue(threadPoolSize > 0, "threadPoolSize must be a positive number.");
		
		this.id = ID_COUNTER++;
		this.appContext = appContext;
		this.session = session;
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
					threadPool.execute(new GatekeeperTask(appContext, session, relayClient, msg)); 
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