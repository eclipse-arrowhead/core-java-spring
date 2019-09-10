package eu.arrowhead.core.eventhandler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;

public class PublishEventTask implements Runnable{

	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(PublishEventTask.class);
	
	private final Subscription subscription;
	private final EventPublishRequestDTO publishRequestDTO;
	private final HttpService httpService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public PublishEventTask(final Subscription subscription, final EventPublishRequestDTO publishRequestDTO,
			final HttpService httpService) {
		
		this.subscription = subscription;
		this.publishRequestDTO = publishRequestDTO;
		this.httpService = httpService;
	}

	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {
		try {
			logger.debug("PublishEventTask.run started...");
			
			if (Thread.currentThread().isInterrupted()) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				
				return;
			}
			
			final UriComponents subscriptionUri = getSubscriptionUri( subscription );

			final ResponseEntity response = httpService.sendRequest( subscriptionUri, HttpMethod.POST, ResponseEntity.class, DTOConverter.convertEventPublishRequestDTOToEventDTO( publishRequestDTO ));		
			
			if (response == null) {
				throw new TimeoutException(subscription.getSubscriberSystem().getSystemName() + " subscriber: SendEventRequest timeout");
			}
			
			if ( !response.getStatusCode().is2xxSuccessful()) {
				throw new ArrowheadException(subscription.getSubscriberSystem().getSystemName() + " subscriber: SendEventRequest unsuccessful: " + response.getStatusCode());
			}		
			
		} catch (final InvalidParameterException | BadPayloadException ex) {	
			
			logger.debug("Exception:", ex.getMessage());
			
		} catch (final Throwable ex) {			
			
			logger.debug("Exception:", ex.getMessage());			
		}
	}
	
	//=================================================================================================
	//Assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getSubscriptionUri(Subscription subscription ) {
		logger.debug("getSubscriptionUri started...");
		
		final String scheme = Utilities.isEmpty( subscription.getSubscriberSystem().getAuthenticationInfo() ) ? CommonConstants.HTTP : CommonConstants.HTTPS;
		try {
			
			return Utilities.createURI(scheme, subscription.getSubscriberSystem().getAddress(), subscription.getSubscriberSystem().getPort(), subscription.getNotifyUri());
		
		} catch (final ClassCastException ex) {
			throw new ArrowheadException("EventHandler can't find subscription URI.");
		}
		
	}

}
