package eu.arrowhead.core.qos.database.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@Service
public class QoSDatabaseService {

	//=================================================================================================
	// members

	@Autowired
	private QoSIntraMeasurementRepository qoSIntraMeasurementRepository;

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

		try {
			final List<QoSIntraPingMeasurement> measurementList = qoSIntraMeasurementPingRepository.findAll();
			for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : measurementList) {
				qoSIntraPingMeasurement.setSent(0);
				qoSIntraPingMeasurement.setReceived(0);
				qoSIntraPingMeasurement.setCountStartedAt(ZonedDateTime.now());
			}
			qoSIntraMeasurementPingRepository.saveAll(measurementList);
			qoSIntraMeasurementPingRepository.flush();
		} catch (Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class) 
	public QoSIntraMeasurement createMeasurement(final System system,final QoSMeasurementType ping,final ZonedDateTime aroundNow) {
		logger.debug("createMeasurement started...");

		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(system, QoSMeasurementType.PING, aroundNow);
		measurement.setSystem(system);
		measurement.setMeasurementType(QoSMeasurementType.PING);

		try {
			qoSIntraMeasurementRepository.saveAndFlush(measurement);
		} catch (Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public QoSIntraMeasurement getMeasurement(final SystemResponseDTO systemResponseDTO, final ZonedDateTime aroundNow) {
		logger.debug("getMeasurement started...");

		final Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(
				systemResponseDTO.getSystemName(),
				systemResponseDTO.getAddress(),
				systemResponseDTO.getPort());
		final System system;
		if (systemOptional.isPresent()) {

			system = systemOptional.get();
		}else {
			throw new ArrowheadException("Requested system is not in DB");
		}

		final QoSIntraMeasurement measurement;
		final Optional<QoSIntraMeasurement> qoSIntraMeasurementOptional = qoSIntraMeasurementRepository.findBySystemAndMeasurementType(system, QoSMeasurementType.PING);
		if (qoSIntraMeasurementOptional.isEmpty()) {
			measurement = createMeasurement(system, QoSMeasurementType.PING, aroundNow);
		}else {
			 measurement = qoSIntraMeasurementOptional.get();
		}
		return measurement;
	}
}