package eu.arrowhead.core.eventhandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
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
	
	private static final String AUTH_SUBSCRIPTION_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	
	@Autowired
	private EventHandlerDBService eventHandlerDBService;
	
//	@Autowired
//	private EventHandlerDriver eventHandlerDriver;
	
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
		
		updateSubscriberAuthorizations();
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
				final Set<SystemResponseDTO> authorizedPublishers = getAuthorizedPublishers(subscriber);
				
				eventHandlerDBService.updateSubscriberAuthorization(subscriptionList, authorizedPublishers);
				
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
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = applicationContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		
		if (context.containsKey(AUTH_SUBSCRIPTION_CHECK_URI_KEY)) {
			try {
				return (UriComponents) context.get(AUTH_SUBSCRIPTION_CHECK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
			}
		}
		
		throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
	}
}