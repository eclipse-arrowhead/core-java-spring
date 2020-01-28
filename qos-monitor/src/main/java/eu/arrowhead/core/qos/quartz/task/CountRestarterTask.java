package eu.arrowhead.core.qos.quartz.task;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
@DisallowConcurrentExecution
public class CountRestarterTask implements Job {

	//=================================================================================================
	// members

	protected Logger logger = LogManager.getLogger(CountRestarterTask.class);

	@Autowired
	private QoSIntraMeasurementPingRepository qoSIntraMeasurementPingRepository;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: count restarter task");

		updateCountStartedAt();

		logger.debug("Finished: count restarter task");
	}

	@Transactional (rollbackFor = ArrowheadException.class)
	public void updateCountStartedAt() {
		logger.debug("updateCountStartedAt started...");

		final List<QoSIntraPingMeasurement> measurementList = qoSIntraMeasurementPingRepository.findAll();
		for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : measurementList) {
			qoSIntraPingMeasurement.setSent(0);
			qoSIntraPingMeasurement.setReceived(0);
			qoSIntraPingMeasurement.setCountStartedAt(ZonedDateTime.now());

			qoSIntraMeasurementPingRepository.saveAndFlush(qoSIntraPingMeasurement);

		}
	}
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------

}