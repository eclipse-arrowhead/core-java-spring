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

package eu.arrowhead.core.eventhandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.metadatafiltering.DefaultMetadataFilter;
import eu.arrowhead.core.eventhandler.metadatafiltering.MetadataFilteringAlgorithm;
import eu.arrowhead.core.eventhandler.publish.PublishRequestFixedExecutor;
import eu.arrowhead.core.eventhandler.publish.PublishingQueue;
import eu.arrowhead.core.eventhandler.publish.PublishingQueueWatcherTask;

@Component
public class EventHandlerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	

	@Value(CoreCommonConstants.$EVENTHANDLER_MAX_RETRY_CONNECT_AUTH_WD)
	private int maxRetry;
	
	@Value(CoreCommonConstants.$EVENTHANDLER_RETRY_CONNECT_AUTH_INTERVAL_SEC_WD)
	private int delaySec;
	
	@Autowired
	private EventHandlerDBService eventHandlerDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CommonConstants.EVENT_METADATA_FILTER)
	public MetadataFilteringAlgorithm getMetadataFilter() {
		return new DefaultMetadataFilter();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.EVENT_PUBLISHING_QUEUE)
	public PublishingQueue getPublishingQueue() {
		return new PublishingQueue();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.EVENT_PUBLISHING_QUEUE_WATCHER_TASK)
	public PublishingQueueWatcherTask getPublishingQueueWatcherTask() {
		return new PublishingQueueWatcherTask();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.EVENT_PUBLISHING_EXPRESS_EXECUTOR)
	public PublishRequestFixedExecutor getPublishingExpressExecutor() {
		return new PublishRequestFixedExecutor();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of(CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {				
		logger.debug("customInit started...");
		
		final PublishingQueueWatcherTask publishingQueueWatcherTask = applicationContext.getBean(CoreCommonConstants.EVENT_PUBLISHING_QUEUE_WATCHER_TASK, PublishingQueueWatcherTask.class);
		publishingQueueWatcherTask.start();
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = event.getApplicationContext().getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		standaloneMode = context.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);

		if (!standaloneMode) {
			int retry = 0;
			while (retry < maxRetry) {
				try {
					updateSubscriberAuthorizations();
					logger.info("SubscriberAuthorizations are up to date.");
					break;
				} catch (final Exception ex) {
					++retry;
					logger.info("Unsuccessful update SubscriberAuthorizations. Tries left: " + (maxRetry - retry));
					
					if (retry == maxRetry) {
						logger.info("EventHandler could not start because of unsuccessful Subscribers Authorization: " + ex);
						throw ex;
					} else {
						try {
							Thread.sleep(delaySec * 1000);
						} catch (final InterruptedException e) {			
							logger.error(e.getMessage());
						}
					}
				}		
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {				
		logger.debug("customDestroy started...");
		
		final PublishingQueueWatcherTask publishingQueueWatcherTask = applicationContext.getBean(CoreCommonConstants.EVENT_PUBLISHING_QUEUE_WATCHER_TASK, PublishingQueueWatcherTask.class);
		publishingQueueWatcherTask.destroy();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void updateSubscriberAuthorizations() {
		logger.debug("updateSubscriberAuthorizations started ...");
		
		final List<Subscription> subscriptions = eventHandlerDBService.getSubscriptionsList();
		if (subscriptions.isEmpty()) {	
			return;	
		} else {	
			
			final Map<System, List<Subscription>> mapOfSubscriptions = subscriptions.stream().collect(Collectors.groupingBy(Subscription::getSubscriberSystem));		
			
			for (final List<Subscription> subscriptionList : mapOfSubscriptions.values()) {
				
				final SystemRequestDTO subscriber = DTOConverter.convertSystemToSystemRequestDTO(subscriptionList.get(0).getSubscriberSystem());		
				
				try {
					
					final Set<SystemResponseDTO> authorizedPublishers = getAuthorizedPublishers(subscriber);
					eventHandlerDBService.updateSubscriberAuthorization(subscriptionList, authorizedPublishers);
					
				} catch (final ArrowheadException ex) {
					
					logger.debug("EventHandler can't update Subscribers Authorization: " + ex.getMessage());
					throw new ArrowheadException("EventHandler can't update Subscribers Authorization: " + ex.getMessage());
				}

			}	
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private Set<SystemResponseDTO> getAuthorizedPublishers(final SystemRequestDTO subscriberSystem) {
		logger.debug("getAuthorizedPublishers started...");
		
		Assert.notNull(subscriberSystem, "subscriberSystem is null.");
		
		final UriComponents checkUri = getAuthSubscriptionCheckUri();
		final AuthorizationSubscriptionCheckRequestDTO payload = new AuthorizationSubscriptionCheckRequestDTO(subscriberSystem, null);
		final ResponseEntity<AuthorizationSubscriptionCheckResponseDTO> response = httpService.sendRequest(checkUri, HttpMethod.POST, AuthorizationSubscriptionCheckResponseDTO.class, payload);		
		
		return response.getBody().getPublishers();
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthSubscriptionCheckUri() {
		logger.debug("getAuthSubscriptionCheckUri started...");

		final UriComponents queryUri = getQueryUri();
		checkServiceRegistryConnection(queryUri);
		
		
		final UriComponents authSubscriptionCheckUri = findCoreSystemServiceUri(CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE, queryUri);
		
		if (authSubscriptionCheckUri != null) {
			return authSubscriptionCheckUri;
		}
		
		throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryUri() {
		logger.debug("getQueryUri started...");
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = applicationContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		
		if (context.containsKey(CoreCommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) context.get(CoreCommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("EventHandler can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("EventHandler can't find Service Registry Query URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryConnection(final UriComponents queryUri) {
		logger.debug("checkServiceRegistryConnection started...");
	
		final UriComponents echoUri = createEchoUri(queryUri);
		try {
			httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
		} catch (final ArrowheadException ex) {
			throw new ArrowheadException("EventHandler can't access Service Registry.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createEchoUri(final UriComponents queryUri) {
		logger.debug("createEchoUri started...");
				
		final String scheme = queryUri.getScheme();
		final String echoUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.ECHO_URI;
		return Utilities.createURI(scheme, queryUri.getHost(), queryUri.getPort(), echoUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents findCoreSystemServiceUri(final CoreSystemService coreSystemService, final UriComponents queryUri) {
		logger.debug("findCoreSystemServiceUri started...");
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO.Builder(coreSystemService.getServiceDefinition()).build();
		
		try {
			final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
			final ServiceQueryResultDTO result = response.getBody();
			if (!result.getServiceQueryData().isEmpty()) {
				final int lastIdx = result.getServiceQueryData().size() - 1; // to make sure we use the newest one if some entries stuck in the DB
				final ServiceRegistryResponseDTO entry = result.getServiceQueryData().get(lastIdx);
				final String scheme = entry.getSecure() == ServiceSecurityType.NOT_SECURE ? CommonConstants.HTTP : CommonConstants.HTTPS;
				final UriComponents uri = Utilities.createURI(scheme, entry.getProvider().getAddress(), entry.getProvider().getPort(), entry.getServiceUri());
			
				return uri;
			}
		} catch (final Exception ex) {
			logger.error(ex.getMessage());
			logger.debug("Could not find CoreSystemServiceUri:", ex);
		}
			
		return null;
	}
}