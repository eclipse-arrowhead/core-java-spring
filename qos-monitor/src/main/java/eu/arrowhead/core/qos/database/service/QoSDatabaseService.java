package eu.arrowhead.core.qos.database.service;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.quartz.task.CountRestarterTask;

@Service
public class QoSDatabaseService {

	//=================================================================================================
	// members

	@Autowired
	private QoSIntraMeasurementRepository qosIntraMeasurementRepository;

	@Autowired
	private QoSIntraMeasurementPingRepository qoSIntraMeasurementPingRepository;

	@Autowired
	private QoSIntraPingMeasurementLogRepository qoSIntraPingMeasurementLogRepository;

	@Autowired
	private QoSIntraPingMeasurementLogDetailsRepository qoSIntraPingMeasurementLogDetailsRepository;

	@Autowired
	private SystemRepository systemRepository;

	protected Logger logger = LogManager.getLogger(QoSDatabaseService.class);
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Transactional (rollbackFor = ArrowheadException.class)
	public void updateCountStartedAt() {
		logger.debug("updateCountStartedAt started...");

		final List<QoSIntraPingMeasurement> measurementList = qoSIntraMeasurementPingRepository.findAll();
		for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : measurementList) {
			qoSIntraPingMeasurement.setSent(0);
			qoSIntraPingMeasurement.setReceived(0);
			qoSIntraPingMeasurement.setCountStartedAt(ZonedDateTime.now());
		}
		qoSIntraMeasurementPingRepository.saveAll(measurementList);
		qoSIntraMeasurementPingRepository.flush();

	}
}