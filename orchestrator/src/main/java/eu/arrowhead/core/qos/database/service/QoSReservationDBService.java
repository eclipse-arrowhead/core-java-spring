/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.qos.database.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSReservation;
import eu.arrowhead.common.database.repository.QoSReservationRepository;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.orchestrator.service.OrchestratorDriver;

@Service
public class QoSReservationDBService {

	//=================================================================================================
	// members

	@Value(CoreCommonConstants.$QOS_RESERVATION_TEMP_LOCK_DURATION_WD)
	private int lockDuration; // in seconds 
	
	@Value(CoreCommonConstants.$QOS_MAX_RESERVATION_DURATION_WD)
	private int maxReservationDuration; // in seconds
	
	private final Logger logger = LogManager.getLogger(QoSReservationDBService.class);

	@Autowired
	private QoSReservationRepository qosReservationRepository;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public int releaseObsoleteReservations() {
		logger.debug("releaseObsoleteReservations started ...");
		
		try {
			final ZonedDateTime now = ZonedDateTime.now();
			final List<QoSReservation> obsoletes = qosReservationRepository.findAllByReservedToLessThanEqual(now);
			qosReservationRepository.deleteAll(obsoletes);
			qosReservationRepository.flush();
			
			return obsoletes.size();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<QoSReservation> getAllReservation() {
		return qosReservationRepository.findAll();
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<QoSReservation> getAllReservationsExceptMine(final String systemName, final String address, final int port) {
		logger.debug("getAllReservationsExceptMine started ...");
		
		Assert.isTrue(!Utilities.isEmpty(systemName), "'systemName' is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(address), "'address' is null or empty.");
		
		final String validatedSystemName = systemName.trim().toLowerCase();
		final String validatedAddress = address.trim().toLowerCase();
		
		return qosReservationRepository.findAllByConsumerSystemNameNotOrConsumerAddressNotOrConsumerPortNot(validatedSystemName, validatedAddress, port);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void applyTemporaryLock(final String systemName, final String address, final int port, final OrchestrationResultDTO dto) {
		logger.debug("applyTemporaryLock started ...");
		
		Assert.isTrue(!Utilities.isEmpty(systemName), "'systemName' is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(address), "'address' is null or empty.");
		Assert.notNull(dto, "'dto' is null.");
		Assert.notNull(dto.getProvider(), "Provider is null.");
		Assert.notNull(dto.getService(), "Service is null.");
		
		final ZonedDateTime reservedTo = ZonedDateTime.now().plusSeconds(lockDuration);
		final QoSReservation reservation = new QoSReservation(dto.getProvider().getId(), dto.getService().getId(), systemName.trim().toLowerCase(), address.trim().toLowerCase(), port,
															  reservedTo, true);
		try {
			qosReservationRepository.saveAndFlush(reservation);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeTemporaryLock(final OrchestrationResultDTO dto) {
		logger.debug("removeTemporaryLock started ...");
		
		Assert.notNull(dto, "'dto' is null.");
		Assert.notNull(dto.getProvider(), "Provider is null.");
		Assert.notNull(dto.getService(), "Service is null.");

		final Optional<QoSReservation> optReservation = qosReservationRepository.findByReservedProviderIdAndReservedServiceIdAndTemporaryLockTrue(dto.getProvider().getId(),
																																				  dto.getService().getId());
		if (optReservation.isEmpty()) { // maybe temporary lock is expired and removed
			// do nothing
		} else {
			final QoSReservation reservation = optReservation.get();
			try {
				qosReservationRepository.delete(reservation);
				qosReservationRepository.flush();
			} catch (final Exception ex) {
				logger.debug(ex.getMessage(), ex);
				throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void extendReservation(final OrchestrationResultDTO reserved, final SystemRequestDTO requester) {
		logger.debug("extendReservation started ...");
		
		Assert.notNull(reserved, "'reserved' is null.");
		Assert.notNull(reserved.getProvider(), "Provider is null.");
		Assert.notNull(reserved.getService(), "Service is null.");
		Assert.notNull(requester, "'requester' is null.");
		Assert.isTrue(!Utilities.isEmpty(requester.getSystemName()),  "Requester system's name is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(requester.getAddress()),  "Requester system's address is null or empty.");
		Assert.notNull(requester.getPort(), "Requester system's port is null.");

		final Optional<QoSReservation> optReservation = qosReservationRepository.findByReservedProviderIdAndReservedServiceId(reserved.getProvider().getId(), reserved.getService().getId());
		QoSReservation reservation;
		if (optReservation.isEmpty()) { // maybe temporary lock is expired and removed
			reservation = new QoSReservation(reserved.getProvider().getId(), reserved.getService().getId(), requester.getSystemName().trim().toLowerCase(), 
											 requester.getAddress().trim().toLowerCase(), requester.getPort(),
											 null, false);
		} else {
			reservation = optReservation.get();
		}
		
		reservation.setTemporaryLock(false);
		reservation.setReservedTo(getReservationTime(reserved));
		try {
			qosReservationRepository.saveAndFlush(reservation);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ZonedDateTime getReservationTime(final OrchestrationResultDTO dto) {
		logger.debug("getReservationTime started ...");
		
		final ZonedDateTime time = ZonedDateTime.now();
		
		int seconds = maxReservationDuration;
		if (dto.getMetadata() != null && dto.getMetadata().containsKey(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME)) {
			try {
				final String calculatedTimeFrameStr = dto.getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME);
				final int calculatedTimeFrame = Integer.parseInt(calculatedTimeFrameStr);
				seconds = Math.min(maxReservationDuration, calculatedTimeFrame);
			} catch (final NumberFormatException ex) {
				logger.debug(ex.getMessage());
				logger.trace("Stacktracke:", ex);
			}
		}
		
		return time.plusSeconds(seconds);
	}
}