package eu.arrowhead.core.qos.quartz.task;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@Component
@DisallowConcurrentExecution
public class CountRestarterTask implements Job {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(CountRestarterTask.class);

	@Autowired
	private QoSDBService qoSDBService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: count restarter task");

		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {

			return;
		}

		qoSDBService.updateCountStartedAt();

		logger.debug("Finished: count restarter task");
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------

}