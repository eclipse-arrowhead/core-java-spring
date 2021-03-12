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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.http.HttpService;

public class PublishRequestExecutor {
	
	//=================================================================================================
	// members
	
	private static final int MAX_THREAD_POOL_SIZE = 20;

	private final ThreadPoolExecutor threadPool;
	private final EventPublishRequestDTO publishRequestDTO;
	private final Set<Subscription> involvedSubscriptions;
	private final HttpService httpService;
	
	private final Logger logger = LogManager.getLogger(PublishRequestExecutor.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public PublishRequestExecutor(final EventPublishRequestDTO publishRequestDTO, final Set<Subscription> involvedSubscriptions, final HttpService httpService) {
		this.publishRequestDTO = publishRequestDTO;
		this.involvedSubscriptions = involvedSubscriptions;
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.involvedSubscriptions.size() > MAX_THREAD_POOL_SIZE ? MAX_THREAD_POOL_SIZE : this.involvedSubscriptions.size());
		this.httpService = httpService;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void execute() {
		logger.debug("PublishRequestExecutor.execute started...");
		validateMembers();
		
		for (final Subscription subscription : involvedSubscriptions) {			
			try {
				threadPool.execute(new PublishEventTask(subscription, publishRequestDTO, httpService));
			} catch (final RejectedExecutionException ex) {
				logger.error("PublishEventTask execution rejected at {}", ZonedDateTime.now());
			}
		}
		
		threadPool.shutdownNow();
	}
	//-------------------------------------------------------------------------------------------------
	public void shutdownExecutionNow() {
		threadPool.shutdownNow();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateMembers() {
		try {
			Assert.notNull(this.threadPool, "threadPool is null");
			Assert.notNull(this.publishRequestDTO, "publishRequestDTO is null");
			Assert.notNull(this.involvedSubscriptions, "involvedSubscriptions is null");
			Assert.notNull(this.httpService, "httpService is null");
		} catch (final IllegalArgumentException  ex) {
			shutdownExecutionNow();
			throw ex;
		}
	}
}