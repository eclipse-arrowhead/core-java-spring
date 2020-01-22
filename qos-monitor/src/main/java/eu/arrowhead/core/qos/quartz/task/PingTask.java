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
public class PingTask implements Job {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(PingTask.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: ping  task");
						
		logger.debug("Finished: ping  task");
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------

}