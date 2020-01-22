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
public class CloudPingTask implements Job {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(CloudPingTask.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Intra cloud ping  task");
						
		logger.debug("Finished: Intra cloud ping  task");
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------

}