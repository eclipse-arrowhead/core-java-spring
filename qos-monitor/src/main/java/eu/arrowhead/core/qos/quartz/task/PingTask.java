package eu.arrowhead.core.qos.quartz.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@Component
@DisallowConcurrentExecution
public class PingTask implements Job {

	//=================================================================================================
	// members
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final int TIMES_TO_REPEAT = 10;

	protected Logger logger = LogManager.getLogger(PingTask.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: ping  task");

		final SystemResponseDTO system = getSystemToMessure();
		pingSystem(system);

		logger.debug("Finished: ping  task");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemToMessure() {
		logger.debug("getSystemToMessure started...");
		// TODO Implement method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private void pingSystem(final SystemResponseDTO system ) {
		logger.debug("pingSystem started...");
		
		if (system == null || Utilities.isEmpty(system.getAddress())) {
			//throw new InvalidParameterException("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		//final String address = system.getAddress();
		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest ();
			//request.setHost (address);
			request.setHost ("github.com");

			for (int count = 0; count < TIMES_TO_REPEAT; count ++) {
				// delegate
				final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
				// log
				final String formattedResponse = IcmpPingUtil.formatResponse (response);
				logger.info(formattedResponse);
				// rest
				Thread.sleep (1000);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}