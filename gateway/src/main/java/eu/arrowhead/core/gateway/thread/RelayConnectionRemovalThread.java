package eu.arrowhead.core.gateway.thread;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;

@Component
public class RelayConnectionRemovalThread extends Thread {
	
	//=================================================================================================
	// member
	
	private static final long PERIOD = 10000; //milisec
	
	@Value(CoreCommonConstants.$GATEWAY_INACTIVE_BRIDGE_TIMEOUT_WD)
	private long consumerSideThreshold;
	
	private long providerSideThreshold;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_CONSUMER_SIDE_SOCKET_THREAD_MAP)
	private ConcurrentMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP)
	private ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers;	
	
	private static final Logger logger = LogManager.getLogger(RelayConnectionRemovalThread.class);

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		logger.info("RelayConnectionRemovalThread started...");
		
		this.providerSideThreshold = consumerSideThreshold + PERIOD / CoreCommonConstants.CONVERSION_MILLISECOND_TO_SECOND;
		
		while (true) {
			final ZonedDateTime now = ZonedDateTime.now();
			handleConsumerSideConnections(now);
			handleProviderSideConnections(now);
			sleep();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void interrupt() {
		logger.error("RelayConnectionRemovalThread has been terminated");
		super.interrupt();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void handleConsumerSideConnections(final ZonedDateTime now) {
		for (ConsumerSideServerSocketThread thread : activeConsumerSideSocketThreads.values()) {
			if (now.isAfter(thread.getLastInteractionTime().plusSeconds(consumerSideThreshold))) {
				System.out.println("CONSUMER: removal task interrupt thread");
				thread.setInterrupted(true);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void handleProviderSideConnections(final ZonedDateTime now) {
		for (final ProviderSideSocketThreadHandler threadHandler : activeProviderSideSocketThreadHandlers.values()) {
			if (now.isAfter(threadHandler.getLastInteractionTime().plusSeconds(providerSideThreshold))) {
				threadHandler.close();
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void sleep() {
		try {
			Thread.sleep(PERIOD);
		} catch (final InterruptedException ex) {
			interrupt();
		}
	}
}
