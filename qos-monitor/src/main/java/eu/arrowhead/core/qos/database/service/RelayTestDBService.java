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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.dto.RelayEchoMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.dto.RelayEchoMeasurementDetailsDTO;

@Service
public class RelayTestDBService {

	//=================================================================================================
	// members
	
	private static final int INVALID_CALCULATION_VALUE = -1;
	
	@Autowired
	private QoSDBService qosDBService;
	
	@Value(CoreCommonConstants.$RELAY_TEST_TIMEOUT_WD)
	private long timeout;
	
	@Value(CoreCommonConstants.$RELAY_TEST_MESSAGE_SIZE_WD)
	private int testMessageSize;

	@Value(CoreCommonConstants.$RELAY_TEST_LOG_MEASUREMENTS_IN_DB_WD)
	private boolean logMeasurements;
	
	private Logger logger = LogManager.getLogger(RelayTestDBService.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void storeMeasurements(final CloudResponseDTO cloud, final RelayResponseDTO relay, final Map<Byte,long[]> rawResults) {
		logger.debug("storeMeasurements started...");
		
		Assert.notNull(cloud, "'cloud' is null.");
		Assert.notNull(relay, "'relay' is null.");
		Assert.notNull(rawResults, "'rawResults' is null.");
		Assert.isTrue(!rawResults.isEmpty(), "'rawResults' is empty.");
		
		final ZonedDateTime measurementStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rawResults.get((byte)0)[0]), ZoneId.systemDefault());
		final RelayEchoMeasurementCalculationsDTO calculatedResult = processResults(rawResults, measurementStart);
		
		final QoSInterRelayMeasurement measurementHead = qosDBService.getOrCreateInterRelayMeasurement(cloud, relay, QoSMeasurementType.RELAY_ECHO);
		final Optional<QoSInterRelayEchoMeasurement> measurementOpt = qosDBService.getInterRelayEchoMeasurementByMeasurement(measurementHead);
		
		if (measurementOpt.isEmpty()) {
			qosDBService.createInterRelayEchoMeasurement(measurementHead, calculatedResult, measurementStart);
		} else {
			qosDBService.updateInterRelayEchoMeasurement(measurementHead, calculatedResult, measurementOpt.get(), measurementStart);
		}
		
		measurementHead.setStatus(QoSMeasurementStatus.FINISHED);
		qosDBService.updateInterRelayMeasurement(measurementStart, measurementHead);
		
		if (logMeasurements) {
			final List<RelayEchoMeasurementDetailsDTO> measurementDetails = convertRawResultsToMeasurementDetails(rawResults);
			qosDBService.logInterRelayEchoMeasurementToDB(measurementHead, measurementDetails, measurementStart);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void finishMeasurements(final CloudResponseDTO cloud, final RelayResponseDTO relay) {
		logger.debug("storeMeasurements started...");
		
		Assert.notNull(cloud, "'cloud' is null.");
		Assert.notNull(relay, "'relay' is null.");
		
		final QoSInterRelayMeasurement measurementHead = qosDBService.getOrCreateInterRelayMeasurement(cloud, relay, QoSMeasurementType.RELAY_ECHO);
		
		measurementHead.setStatus(QoSMeasurementStatus.FINISHED);
		qosDBService.updateInterRelayMeasurement( ZonedDateTime.now(), measurementHead);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void logErrorIntoMeasurementsTable(final CloudResponseDTO cloud, final RelayResponseDTO relay, final int index, final String errorMessage, final Throwable throwable) {
		logger.debug("storeMeasurements started...");
		
		Assert.notNull(cloud, "'cloud' is null.");
		Assert.notNull(relay, "'relay' is null.");
		Assert.notNull(throwable, "'throwable' is null.");

		if (logMeasurements) {
			final ZonedDateTime now = ZonedDateTime.now();
			final String message = Utilities.isEmpty(errorMessage) ? throwable.getMessage() : errorMessage;
			final QoSInterRelayMeasurement measurementHead = qosDBService.getOrCreateInterRelayMeasurement(cloud, relay, QoSMeasurementType.RELAY_ECHO);
			final RelayEchoMeasurementDetailsDTO errorDTO = new RelayEchoMeasurementDetailsDTO(index, true, message, throwable.toString(), testMessageSize, null, now);
			qosDBService.logInterRelayEchoMeasurementToDB(measurementHead, List.of(errorDTO), now);
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<RelayEchoMeasurementDetailsDTO> convertRawResultsToMeasurementDetails(final Map<Byte,long[]> rawResults) {
		final List<RelayEchoMeasurementDetailsDTO> result = new ArrayList<>(rawResults.size());
		
		for (final Entry<Byte,long[]> entry : rawResults.entrySet()) {
			final byte key = entry.getKey().byteValue();
			final long start = entry.getValue()[0];
			final long end = entry.getValue()[1];
			final ZonedDateTime measurementStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault());
			final RelayEchoMeasurementDetailsDTO dto = new RelayEchoMeasurementDetailsDTO(key, isTimeOut(start, end), null, null, testMessageSize, (int) getDuration(start, end),
																						  measurementStart);
			
			result.add(dto);
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isTimeOut(final long start, final long end) {
		return end == start + timeout + 1;
	}
	
	//-------------------------------------------------------------------------------------------------
	private long getDuration(final long start, final long end) {
		return end - start;
	}

	//-------------------------------------------------------------------------------------------------
	private RelayEchoMeasurementCalculationsDTO processResults(final Map<Byte,long[]> rawResults, final ZonedDateTime measurementStart) {
		final int sent  = rawResults.size();
		int received = 0;
		long maxResponseTime = 0;
		long minResponseTime = Integer.MAX_VALUE;
		double sumOfDurationWithTimeout = 0;
		double sumOfDurationWithoutTimeout = 0;
		int noTimeoutCount = 0;
		
		for (final long[] values : rawResults.values()) {
			final long start = values[0];
			final long end = values[1];

			if (isTimeOut(start, end)) {
				sumOfDurationWithTimeout += timeout;
			} else {
				++received;
				final long duration = getDuration(start, end);
				
				if (duration > maxResponseTime) {
					maxResponseTime = duration;
				}
				
				if (duration < minResponseTime) {
					minResponseTime = duration;
				}

				sumOfDurationWithoutTimeout += duration;
				sumOfDurationWithTimeout += duration;
				++noTimeoutCount;
			}
		}

		final int lostPerMeasurementPercent = (int) (received == 0 ? 100 : 100 - ((double)received / sent) * 100);
		final double meanResponseTimeWithTimeout = rawResults.size() < 1 ? INVALID_CALCULATION_VALUE : sumOfDurationWithTimeout / rawResults.size();
		final double meanResponseTimeWithoutTimeout = noTimeoutCount < 1 ? INVALID_CALCULATION_VALUE : sumOfDurationWithoutTimeout / noTimeoutCount;

		double sumOfDiffsWithTimeout = 0;
		double sumOfDiffsWithoutTimeout = 0;
		for (final long[] values : rawResults.values()) {
			final long start = values[0];
			final long end = values[1];

			long duration;
			if (isTimeOut(start, end)) {
				duration = timeout;
			} else {
				duration = getDuration(start, end);
				sumOfDiffsWithoutTimeout += Math.pow( (duration - meanResponseTimeWithoutTimeout), 2);
			}
			sumOfDiffsWithTimeout += Math.pow(duration - meanResponseTimeWithTimeout, 2);
		}
		
		final double jitterWithTimeout = rawResults.size() < 1 ? INVALID_CALCULATION_VALUE : Math.sqrt(sumOfDiffsWithTimeout / rawResults.size());
		final double jitterWithoutTimeout = noTimeoutCount < 1 ? INVALID_CALCULATION_VALUE : Math.sqrt(sumOfDiffsWithoutTimeout / noTimeoutCount);
		
		final boolean available = noTimeoutCount > 0; 
		
		final RelayEchoMeasurementCalculationsDTO result = new RelayEchoMeasurementCalculationsDTO();
		result.setMinResponseTime(available ? (int) minResponseTime : null);
		result.setMaxResponseTime(available ? (int) maxResponseTime : null);
		result.setMeanResponseTimeWithTimeout(available ? (int) Math.round(meanResponseTimeWithTimeout) : null);
		result.setMeanResponseTimeWithoutTimeout(available ? (int) Math.round(meanResponseTimeWithoutTimeout) : null);
		result.setJitterWithTimeout(available ? (int) Math.round(jitterWithTimeout) : null);
		result.setJitterWithoutTimeout(available ? (int) Math.round(jitterWithoutTimeout) : null);
		result.setSentInThisTest(sent);
		result.setReceivedInThisTest(received);
		result.setLostPerMeasurementPercent(lostPerMeasurementPercent);
		result.setMeasuredAt(measurementStart);

		return result;
	}
}