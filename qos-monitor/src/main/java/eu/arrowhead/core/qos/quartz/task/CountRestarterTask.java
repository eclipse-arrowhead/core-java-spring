package eu.arrowhead.core.qos.quartz.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class CountRestarterTask implements Job {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(CountRestarterTask.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: count restarter task");
						
		logger.debug("Finished: count restarter task");
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------

}