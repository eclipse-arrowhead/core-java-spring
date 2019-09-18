package eu.arrowhead.core.eventhandler.quartz.task;

import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
		logger.debug("STARTED: EventFilter end of validity check task");
		
		//TODO implement method logic here
				
		//logger.debug("FINISHED: EventFilter end of validity check task. Number of removed service registry entry: {}", removedEventFilters.size());
	}
	

	
	//=================================================================================================
	// assistant methods

	
	//-------------------------------------------------------------------------------------------------
	private boolean isTTLValid(final ZonedDateTime endOfValidity) {
		return endOfValidity.isAfter(ZonedDateTime.now());
	}
}