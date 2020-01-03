package eu.arrowhead.core.eventhandler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.eventhandler.metadatafiltering.DefaultMetadataFilter;
import eu.arrowhead.core.eventhandler.metadatafiltering.MetadataFilteringAlgorithm;
import eu.arrowhead.core.eventhandler.publish.PublishRequestFixedExecutor;
import eu.arrowhead.core.eventhandler.publish.PublishingQueue;
import eu.arrowhead.core.eventhandler.publish.PublishingQueueWatcherTask;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;

@Component
public class EventHandlerApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private EventHandlerService eventHandlerService;
	
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
		
		eventHandlerService.updateSubscriberAuthorizations();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {				
		logger.debug("customDestroy started...");
		
		final PublishingQueueWatcherTask publishingQueueWatcherTask = applicationContext.getBean(CoreCommonConstants.EVENT_PUBLISHING_QUEUE_WATCHER_TASK, PublishingQueueWatcherTask.class);
		publishingQueueWatcherTask.destroy();
	}
}