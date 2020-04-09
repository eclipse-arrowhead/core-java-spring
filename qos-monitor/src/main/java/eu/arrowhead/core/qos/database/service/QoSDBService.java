package eu.arrowhead.core.qos.database.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
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
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.PingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;

@Service
public class QoSDBService {

	//=================================================================================================
	// members

	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = " sortable field is not available.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";

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

	protected Logger logger = LogManager.getLogger(QoSDBService.class);
	
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
	public QoSIntraMeasurement createMeasurement(final System system, final QoSMeasurementType ping, final ZonedDateTime aroundNow) {
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
	public QoSIntraMeasurement getOrCreateMeasurement(final SystemResponseDTO systemResponseDTO) {
		logger.debug("getMeasurement started...");

		validateSystemResponseDTO(systemResponseDTO);

		final System system;
		final Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(
				systemResponseDTO.getSystemName(),
				systemResponseDTO.getAddress(),
				systemResponseDTO.getPort());
		if (systemOptional.isPresent()) {
			system = systemOptional.get();
		} else {
			throw new InvalidParameterException("Requested system" + NOT_IN_DB_ERROR_MESSAGE);
		}

		final QoSIntraMeasurement measurement;
		final Optional<QoSIntraMeasurement> qoSIntraMeasurementOptional = qoSIntraMeasurementRepository.findBySystemAndMeasurementType(system, QoSMeasurementType.PING);
		if (qoSIntraMeasurementOptional.isEmpty()) {
			final ZonedDateTime aroundNow = ZonedDateTime.now();
			measurement = createMeasurement(system, QoSMeasurementType.PING, aroundNow);
		}else {
			 measurement = qoSIntraMeasurementOptional.get();
		}

		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public void createPingMeasurement(final QoSIntraMeasurement measurementParam, final PingMeasurementCalculationsDTO calculations, final ZonedDateTime aroundNow) {
		logger.debug("createPingMeasurement started...");

		if (measurementParam == null) {
			throw new InvalidParameterException("QoSIntraMeasurement" + NULL_ERROR_MESSAGE);
		}
		if (calculations == null) {
			throw new InvalidParameterException("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE);
		}
		if (aroundNow == null) {
			throw new InvalidParameterException("ZonedDateTime" + NULL_ERROR_MESSAGE);
		}

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

			qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);

		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	public Optional<QoSIntraPingMeasurement> getPingMeasurementByMeasurement(final QoSIntraMeasurement measurement) {
		logger.debug("getPingMeasurementByMeasurement started...");

		if (measurement == null) {
			throw new InvalidParameterException("QoSIntraMeasurement" + NULL_ERROR_MESSAGE);
		}

		try {
			return qoSIntraMeasurementPingRepository.findByMeasurement(measurement);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public QoSIntraPingMeasurementLog logMeasurementToDB(final String address, final PingMeasurementCalculationsDTO calculations, final ZonedDateTime aroundNow) {
		logger.debug("logMeasurementToDB started...");

		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		} else if (calculations == null) {
			throw new InvalidParameterException("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE);
		} else if (aroundNow == null) {
			throw new InvalidParameterException("ZonedDateTime" + NULL_ERROR_MESSAGE);
		}

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();
		measurementLog.setMeasuredSystemAddress(address);
		measurementLog.setAvailable(calculations.isAvailable());
		measurementLog.setMinResponseTime(calculations.getMinResponseTime());
		measurementLog.setMaxResponseTime(calculations.getMaxResponseTime());
		measurementLog.setMeanResponseTimeWithoutTimeout(calculations.getMeanResponseTimeWithoutTimeout());
		measurementLog.setMeanResponseTimeWithTimeout(calculations.getMeanResponseTimeWithTimeout());
		measurementLog.setJitterWithoutTimeout(calculations.getJitterWithoutTimeout());
		measurementLog.setJitterWithTimeout(calculations.getJitterWithTimeout());
		measurementLog.setLostPerMeasurementPercent(calculations.getLostPerMeasurementPercent());
		measurementLog.setSent(calculations.getSentInThisPing());
		measurementLog.setReceived(calculations.getReceivedInThisPing());
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

		if (responseList == null || responseList.isEmpty()) {
			throw new InvalidParameterException("List<IcmpPingResponse>" + EMPTY_OR_NULL_ERROR_MESSAGE);
		} else if (measurementLogSaved == null) {
			throw new InvalidParameterException("QoSIntraPingMeasurementLog" + NULL_ERROR_MESSAGE);
		} else if (aroundNow == null) {
			throw new InvalidParameterException("ZonedDateTime" + NULL_ERROR_MESSAGE);
		}

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = new ArrayList<>(responseList.size());

		int measurementSequenece = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final QoSIntraPingMeasurementLogDetails measurementLogDetails = new QoSIntraPingMeasurementLogDetails();
			measurementLogDetails.setMeasurementLog(measurementLogSaved);
			measurementLogDetails.setMeasurementSequeneceNumber( measurementSequenece++ );
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
	public void updatePingMeasurement(final QoSIntraMeasurement measurement,
			final PingMeasurementCalculationsDTO calculations, final QoSIntraPingMeasurement pingMeasurement,final ZonedDateTime aroundNow) {
		logger.debug("updatePingMeasurement started...");

		if (measurement == null) {
			throw new InvalidParameterException("QoSIntraMeasurement" + NULL_ERROR_MESSAGE);
		} else if (calculations == null) {
			throw new InvalidParameterException("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE);
		} else if (pingMeasurement == null) {
			throw new InvalidParameterException("QoSIntraPingMeasurement" + NULL_ERROR_MESSAGE);
		} else if (aroundNow == null) {
			throw new InvalidParameterException("ZonedDateTime" + NULL_ERROR_MESSAGE);
		}

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

			qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);

		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	public void updateMeasurement(final ZonedDateTime aroundNow, final QoSIntraMeasurement measurement) {
		logger.debug("updateMeasurement started...");

		if (measurement == null) {
			throw new InvalidParameterException("QoSIntraMeasurement" + NULL_ERROR_MESSAGE);
		} else if (aroundNow == null) {
			throw new InvalidParameterException("ZonedDateTime" + NULL_ERROR_MESSAGE);
		}

		measurement.setLastMeasurementAt(aroundNow);
		try {
			qoSIntraMeasurementRepository.saveAndFlush(measurement);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementListResponseDTO getPingMeasurementResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getPingMeasurementResponse started...");

		return DTOConverter.convertQoSIntraPingMeasurementPageToPingMeasurementListResponseDTO(getPingMeasurementPage(page, size, direction, sortField));
	}

	//-------------------------------------------------------------------------------------------------
	public Page<QoSIntraPingMeasurement> getPingMeasurementPage(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getPingMeasurementPage started...");

		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

		if (!QoSIntraPingMeasurement.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(validatedSortField + NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE);
		}

		try {
			return qoSIntraMeasurementPingRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementResponseDTO getPingMeasurementBySystemIdResponse(final long id) {
		logger.debug("getPingMeasurementBySystemIdResponse started ...");

		final QoSIntraPingMeasurement pingMeausrement = getPingMeasurementBySystemId(id);
		if (pingMeausrement == null ) {

			final PingMeasurementResponseDTO response = new PingMeasurementResponseDTO();
			response.setId( null );

			return response;
		}

		return DTOConverter.convertQoSIntraPingMeasurementToPingMeasurementResponseDTO(pingMeausrement);
	}

	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurement getPingMeasurementBySystemId(final long id) {
		logger.debug("getPingMeasurementBySystemId started ...");

		if (id < 1) {
			throw new InvalidParameterException("SystemId" + LESS_THAN_ONE_ERROR_MESSAGE);
		}

		final System system;
		final Optional<System> systemOptional;
		try {

			systemOptional = systemRepository.findById(id);
			if (systemOptional.isPresent()) {
				system = systemOptional.get();
			} else {
				throw new InvalidParameterException("Requested system" + NOT_IN_DB_ERROR_MESSAGE);
			}

			final QoSIntraMeasurement measurement;
			final Optional<QoSIntraMeasurement> qoSIntraMeasurementOptional = qoSIntraMeasurementRepository.findBySystemAndMeasurementType(system, QoSMeasurementType.PING);
			if (qoSIntraMeasurementOptional.isEmpty()) {
	
				return null;
			} else {
				 measurement = qoSIntraMeasurementOptional.get();
			}

			final Optional<QoSIntraPingMeasurement> measurementOptional = qoSIntraMeasurementPingRepository.findByMeasurement(measurement);
			if (measurementOptional.isEmpty()) {

				return null;
			} else {

				return measurementOptional.get();
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validateSystemResponseDTO(final SystemResponseDTO systemResponseDTO) {
		logger.debug("validateSystemRequestDTO started...");

		if (systemResponseDTO == null) {
			throw new InvalidParameterException("SystemRequestDTO" + NULL_ERROR_MESSAGE);
		}

		if (Utilities.isEmpty(systemResponseDTO.getSystemName())) {
			throw new InvalidParameterException("System name" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		if (Utilities.isEmpty(systemResponseDTO.getAddress())) {
			throw new InvalidParameterException("System address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
	}	
}