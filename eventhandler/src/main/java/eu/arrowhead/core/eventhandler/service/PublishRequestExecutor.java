package eu.arrowhead.core.eventhandler.service;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.ErrorWrapperDTO;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;

public class PublishRequestExecutor {
	//=================================================================================================
	// members
	
	private static final int MAX_THREAD_POOL_SIZE = 20;

	private final ThreadPoolExecutor threadPool;
	private final EventPublishRequestDTO publishRequestDTO;
	private final Set<Subscription> involvedSubscriptions;
	
	private final Logger logger = LogManager.getLogger(PublishRequestExecutor.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public PublishRequestExecutor(final EventPublishRequestDTO publishRequestDTO, 
								  final Set<Subscription> involvedSubscriptions) {
		
		this.publishRequestDTO = publishRequestDTO;
		this.involvedSubscriptions = involvedSubscriptions;
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
				this.involvedSubscriptions.size() > MAX_THREAD_POOL_SIZE ? 
						MAX_THREAD_POOL_SIZE : this.involvedSubscriptions.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void execute() {
		logger.debug("PublishRequestExecutor.execute started...");
		validateMembers();
		
		for ( final Subscription subscription : involvedSubscriptions ) {			
			try {

				threadPool.execute(new PublishEventTask(subscription, publishRequestDTO));
			} catch (final RejectedExecutionException ex) {
				logger.error("PublishEventTask execution rejected at {}", ZonedDateTime.now());
				
			}
		}
		
		threadPool.shutdown();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void shutdownExecutionNow() {
		threadPool.shutdownNow();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateMembers() {
		Assert.notNull(this.threadPool, "threadPool is null");
		Assert.notNull(this.publishRequestDTO, "publishRequestDTO is null");
		Assert.notNull(this.involvedSubscriptions, "involvedSubscriptions is null");
	}
}
