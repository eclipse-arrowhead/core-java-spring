package eu.arrowhead.core.qos.database.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLogDetails;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;

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
		} catch (final Exception ex) {
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
		} catch (final Exception ex) {
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

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public QoSIntraPingMeasurement createPingMeasurement(final QoSIntraMeasurement measurementParam,
	final List<IcmpPingResponse> responseList,final PingMeasurementCalculationsDTO calculations, final ZonedDateTime aroundNow) {
		logger.debug("createPingMeasurement started...");

		final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();

		pingMeasurement.setMeasurement(measurementParam);
		pingMeasurement.setAvailable(calculations.isAvailable());
		pingMeasurement.setMaxResponseTime(calculations.getMaxResponseTime());
		pingMeasurement.setMinResponseTime(calculations.getMinResponseTime());
		pingMeasurement.setMeanResponseTimeWithoutTimeout(calculations.getMeanResponseTimeWithoutTimeout());
		pingMeasurement.setMeanResponseTimeWithTimeout(calculations.getMeanResponseTimeWithTimeout());
		pingMeasurement.setJitterWithoutTimeout(calculations.getJitterWithoutTimeout());
		pingMeasurement.setJitterWithTimeout(calculations.getJitterWithTimeout());
		pingMeasurement.setLostPerMeasurementPercent(calculations.getLostPerMeasurementPercent());
		pingMeasurement.setCountStartedAt(aroundNow);
		pingMeasurement.setLastAccessAt(calculations.isAvailable() ? aroundNow : null);
		pingMeasurement.setSent(calculations.getSentInThisPing());
		pingMeasurement.setSentAll(calculations.getSentInThisPing());
		pingMeasurement.setReceived(calculations.getReceivedInThisPing());
		pingMeasurement.setReceivedAll(calculations.getReceivedInThisPing());

		try {
			return qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	public Optional<QoSIntraPingMeasurement> getPingMeasurementByMeasurement(final QoSIntraMeasurement measurement) {
		logger.debug("getPingMeasurementByMeasurement started...");

		try {
			return qoSIntraMeasurementPingRepository.findByMeasurement(measurement);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public QoSIntraPingMeasurementLog logMeasurementToDB(final String address, final QoSIntraPingMeasurement pingMeasurement, final ZonedDateTime aroundNow) {
		logger.debug("logMeasurementToDB started...");

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();
		measurementLog.setMeasuredSystemAddress(address);
		measurementLog.setAvailable(pingMeasurement.isAvailable());
		measurementLog.setMinResponseTime(pingMeasurement.getMinResponseTime());
		measurementLog.setMaxResponseTime(pingMeasurement.getMaxResponseTime());
		measurementLog.setMeanResponseTimeWithoutTimeout(pingMeasurement.getMeanResponseTimeWithoutTimeout());
		measurementLog.setMeanResponseTimeWithTimeout(pingMeasurement.getMeanResponseTimeWithTimeout());
		measurementLog.setJitterWithoutTimeout(pingMeasurement.getJitterWithoutTimeout());
		measurementLog.setJitterWithTimeout(pingMeasurement.getJitterWithTimeout());
		measurementLog.setLostPerMeasurementPercent(pingMeasurement.getLostPerMeasurementPercent());
		measurementLog.setSent(pingMeasurement.getSent());
		measurementLog.setReceived(pingMeasurement.getReceived());
		measurementLog.setMeasuredAt(aroundNow);

		try {
			return qoSIntraPingMeasurementLogRepository.saveAndFlush(measurementLog);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public void logMeasurementDetailsToDB(final QoSIntraPingMeasurementLog measurementLogSaved,
			final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("logMeasurementDetailsToDB started...");

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = new ArrayList<>(responseList.size());

		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final QoSIntraPingMeasurementLogDetails measurementLogDetails = new QoSIntraPingMeasurementLogDetails();
			measurementLogDetails.setMeasurementLog(measurementLogSaved);
			measurementLogDetails.setSuccessFlag(icmpPingResponse.getSuccessFlag());
			measurementLogDetails.setTimeoutFlag(icmpPingResponse.getTimeoutFlag());
			measurementLogDetails.setErrorMessage(icmpPingResponse.getErrorMessage());
			measurementLogDetails.setThrowable(icmpPingResponse.getThrowable() == null ? null : icmpPingResponse.getThrowable().toString());
			measurementLogDetails.setSize(icmpPingResponse.getSize());
			measurementLogDetails.setTtl(icmpPingResponse.getTtl());
			measurementLogDetails.setRtt(icmpPingResponse.getRtt());
			measurementLogDetails.setDuration((int) icmpPingResponse.getDuration());
			measurementLogDetails.setMeasuredAt(aroundNow);

			measurementLogDetailsList.add(measurementLogDetails);
		}

		try {
			qoSIntraPingMeasurementLogDetailsRepository.saveAll(measurementLogDetailsList);
			qoSIntraPingMeasurementLogDetailsRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public QoSIntraPingMeasurement updatePingMeasurement(final QoSIntraMeasurement measurement,
			final PingMeasurementCalculationsDTO calculations, final QoSIntraPingMeasurement pingMeasurement,final ZonedDateTime aroundNow) {
		logger.debug("updatePingMeasurement started...");

		pingMeasurement.setMeasurement(measurement);
		pingMeasurement.setAvailable(calculations.isAvailable());
		pingMeasurement.setMaxResponseTime(calculations.getMaxResponseTime());
		pingMeasurement.setMinResponseTime(calculations.getMinResponseTime());
		pingMeasurement.setMeanResponseTimeWithoutTimeout(calculations.getMeanResponseTimeWithoutTimeout());
		pingMeasurement.setMeanResponseTimeWithTimeout(calculations.getMeanResponseTimeWithTimeout());
		pingMeasurement.setJitterWithoutTimeout(calculations.getJitterWithoutTimeout());
		pingMeasurement.setJitterWithTimeout(calculations.getJitterWithTimeout());
		pingMeasurement.setLostPerMeasurementPercent(calculations.getLostPerMeasurementPercent());
		pingMeasurement.setCountStartedAt(pingMeasurement.getCountStartedAt());
		pingMeasurement.setLastAccessAt(calculations.isAvailable() ? aroundNow : pingMeasurement.getLastAccessAt());
		pingMeasurement.setSent(pingMeasurement.getSent() + calculations.getSentInThisPing());
		pingMeasurement.setSentAll(pingMeasurement.getSentAll() + calculations.getSentInThisPing());
		pingMeasurement.setReceived(pingMeasurement.getReceived() + calculations.getReceivedInThisPing());
		pingMeasurement.setReceivedAll(pingMeasurement.getReceivedAll() + calculations.getReceivedInThisPing());

		try {
			return qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public void updateMeasurement(final ZonedDateTime aroundNow, final QoSIntraMeasurement measurement) {
		logger.debug("updateMeasurement started...");

		measurement.setLastMeasurementAt(aroundNow);
		try {
			qoSIntraMeasurementRepository.saveAndFlush(measurement);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

}