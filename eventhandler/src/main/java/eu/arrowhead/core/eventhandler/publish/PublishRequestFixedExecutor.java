/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.eventhandler.publish;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.http.HttpService;

public class PublishRequestFixedExecutor {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS_WD)
	private int maxExpressSubscribers;
	
	private static final int MAX_THREAD_POOL_SIZE = 20;

	private  ThreadPoolExecutor threadPool;
	
	@Autowired
	private HttpService httpService;
	
	private final Logger logger = LogManager.getLogger(PublishRequestExecutor.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	@PostConstruct
	public void init() {
		logger.debug("PublishRequestFixedExecutor.init started...");
		
		if (threadPool == null) {
			threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool( maxExpressSubscribers > MAX_THREAD_POOL_SIZE ? MAX_THREAD_POOL_SIZE : maxExpressSubscribers);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void execute(final EventPublishRequestDTO publishRequestDTO, final Set<Subscription> involvedSubscriptions) {
		logger.debug("PublishRequestFixedExecutor.execute started...");
		
		for (final Subscription subscription : involvedSubscriptions) {			
			try {
				validateSubscription(subscription);
				threadPool.execute(new PublishEventTask(subscription, publishRequestDTO, httpService));
			} catch (final RejectedExecutionException ex) {
				logger.error("PublishEventTask execution rejected at {}", ZonedDateTime.now());
			} catch ( final Throwable ex) {
				logger.debug( ex.getMessage() );
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	public void shutdownExecutionNow() {
		threadPool.shutdownNow();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateSubscription( final Subscription subscription) {
		logger.debug("PublishRequestFixedExecutor.validateSubscription started...");
		
		Assert.notNull( subscription, "subscription is null");
		Assert.notNull( subscription.getNotifyUri(), "subscription is null");
		Assert.notNull( subscription.getSubscriberSystem(), "subscription.SubscriberSystem is null");
		Assert.notNull( subscription.getSubscriberSystem().getSystemName(), "subscriptionsubscription.SubscriberSystem.SystemName is null");
		Assert.notNull( subscription.getSubscriberSystem().getAddress(), "subscriptionsubscription.SubscriberSystem.Address is null");		
	}
}