package eu.arrowhead.core.eventhandler.quartz.task;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;

@Component
@DisallowConcurrentExecution
public class EventFilterEndOfValidityCheckTask implements Job{

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(EventFilterEndOfValidityCheckTask.class);
	private static final int PAGE_SIZE = 1000;
	
	@Autowired
	private EventHandlerDBService eventHandlerDBService;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Subscription end of validity check task");
		
		final List<Subscription> removedSubscriptions = checkSubscriptionEndOfValidity();
				
		logger.debug("FINISHED: Subscription end of validity check task. Number of removed service registry entry: {}", removedSubscriptions.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<Subscription> checkSubscriptionEndOfValidity() {
		final List<Subscription> removedSubscriptionEntries = new ArrayList<>();
		int pageIndexCounter = 0;
		try {
			Page<Subscription> pageOfSubscriptionEntries = eventHandlerDBService.getSubscriptions(pageIndexCounter, PAGE_SIZE, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
			if (pageOfSubscriptionEntries.isEmpty()) {
				logger.debug("Subscription database is empty");
			} else {
				final int totalPages = pageOfSubscriptionEntries.getTotalPages();
				removedSubscriptionEntries.addAll(removeSubscriptionsWithInvalidTTL(pageOfSubscriptionEntries));
				pageIndexCounter++;
				while (pageIndexCounter < totalPages) {
					pageOfSubscriptionEntries = eventHandlerDBService.getSubscriptions(pageIndexCounter, PAGE_SIZE, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
					removedSubscriptionEntries.addAll(removeSubscriptionsWithInvalidTTL(pageOfSubscriptionEntries));
					pageIndexCounter++;
				}
			}
		} catch (final IllegalArgumentException exception) {
			logger.debug(exception.getMessage());
		}
		
		return removedSubscriptionEntries;
	}

	
	//=================================================================================================
	// assistant methods

	
	//-------------------------------------------------------------------------------------------------
	private boolean isTTLValid(final ZonedDateTime endOfValidity) {
		logger.debug( "EventFilterEndOfValidityCheckTask.isTTLValid started..." );
		
		return endOfValidity.isAfter(ZonedDateTime.now());
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Subscription> removeSubscriptionsWithInvalidTTL(final Page<Subscription> pageOfSubscriptionEntries) {
		logger.debug( "EventFilterEndOfValidityCheckTask.removeSubscriptionsWithInvalidTTL started..." );
		
		final List<Subscription> toBeRemoved = new ArrayList<>();
		for (final Subscription subscriptionEntry : pageOfSubscriptionEntries) {
			
			final ZonedDateTime endOfValidity = subscriptionEntry.getEndDate();
			if (endOfValidity != null && ! isTTLValid(endOfValidity)) {
				toBeRemoved.add(subscriptionEntry);
				logger.debug("REMOVED: {}", subscriptionEntry);
			}
		}
		
		eventHandlerDBService.removeSubscriptionEntries( toBeRemoved );
		
		return toBeRemoved;
	}
}