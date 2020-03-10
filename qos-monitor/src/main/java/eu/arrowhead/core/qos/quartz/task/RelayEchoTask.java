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
public class RelayEchoTask implements Job {
	
	private Logger logger = LogManager.getLogger(RelayEchoTask.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: relay echo task");
		
		// TODO
		
	}

}
