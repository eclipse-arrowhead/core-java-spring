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

package eu.arrowhead.core.qos.quartz.task;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;

@Component
@DisallowConcurrentExecution
public class PingTask implements Job {

	//=================================================================================================
	// members
	
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";

	private static final int INVALID_CALCULATION_VALUE = -1;

	protected Logger logger = LogManager.getLogger(PingTask.class);

	@Autowired
	private PingMeasurementProperties pingMeasurementProperties;

	@Autowired
	private PingService pingService;

	@Autowired
	private QoSDBService qoSDBService;

	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: ping task");

		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {

			logger.debug("Finished: ping task can not run if server is in standalon mode");
			return;
		}

		final List<SystemResponseDTO> systems = getSystemsToMeasure();
		if (systems == null || systems.isEmpty()) {

			return;
		}

		final HashMap<String, PingMeasurementCalculationsDTO> pingCache = new HashMap<>(systems.size());
		for (final SystemResponseDTO systemResponseDTO : systems) {

			final String address = systemResponseDTO.getAddress();
			if (pingCache.containsKey(address)) {
				copyCalculationsToPingMeasurement(systemResponseDTO, pingCache.get(address));
			} else {
				final PingMeasurementCalculationsDTO calculations = pingSystem(systemResponseDTO);
				pingCache.put(address, calculations);
			}
		}

		logger.debug("Finished: ping task success");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<SystemResponseDTO> getSystemsToMeasure() {
		logger.debug("getSystemToMeasure started...");

		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = queryServiceRegistryAll();
		if (serviceRegistryListResponseDTO == null || serviceRegistryListResponseDTO.getData() == null ||  serviceRegistryListResponseDTO.getData().isEmpty()) {
			return null;
		}
		
		final List<SystemResponseDTO> systemList = serviceRegistryListResponseDTO.getData().stream().map(ServiceRegistryResponseDTO::getProvider).collect(Collectors.toList());

		return systemList;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO calculatePingMeasurementValues(final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("calculatePingMeasurementValues started...");

		final int sentInThisPing = responseList.size();
		Assert.isTrue(sentInThisPing > 0, "Sent in this Ping value must be greater than zero");

		final int timeout = pingMeasurementProperties.getTimeout() + 1;

		int receivedInThisPing = 0;
		long maxResponseTime = 0;
		long minResponseTime = Integer.MAX_VALUE;
		double sumOfDurationForMeanResponseTimeWithTimeout = 0;
		double sumOfDurationForMeanResponseTimeWithoutTimeout = 0;
		int meanResponseTimeWithoutTimeoutMembersCount = 0;

		for (final IcmpPingResponse icmpPingResponse : responseList) {
			final boolean successFlag = icmpPingResponse.isSuccessFlag();

			if (successFlag) {
				++receivedInThisPing;
				final long duration = icmpPingResponse.getRtt();

				if (duration > maxResponseTime) {
					maxResponseTime = duration;
				}

				if (duration < minResponseTime) {
					minResponseTime = duration;
				}

				sumOfDurationForMeanResponseTimeWithoutTimeout += duration;
				sumOfDurationForMeanResponseTimeWithTimeout += duration;
				++meanResponseTimeWithoutTimeoutMembersCount;
			} else {
				sumOfDurationForMeanResponseTimeWithTimeout += timeout;
			}
		}

		final boolean available = calculateAvailable(sentInThisPing, receivedInThisPing);
		final int lostPerMeasurementPercent = (int) (receivedInThisPing == 0 ? 100 : 100 - ((double)receivedInThisPing / sentInThisPing) * 100);

		final double meanResponseTimeWithTimeout = responseList.size() < 1 ? INVALID_CALCULATION_VALUE : sumOfDurationForMeanResponseTimeWithTimeout / responseList.size();
		final double meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeoutMembersCount < 1 ? INVALID_CALCULATION_VALUE : sumOfDurationForMeanResponseTimeWithoutTimeout / meanResponseTimeWithoutTimeoutMembersCount;

		double sumOfDiffsForJitterWithTimeout = 0;
		double sumOfDiffsForJitterWithoutTimeout = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {
			final boolean successFlag = icmpPingResponse.isSuccessFlag();
			final double duration;
			if (successFlag) {
				 duration = icmpPingResponse.getDuration();
				 sumOfDiffsForJitterWithoutTimeout += Math.pow( (duration - meanResponseTimeWithoutTimeout), 2);
			} else {
				duration = timeout;
			}

			sumOfDiffsForJitterWithTimeout += Math.pow( (duration - meanResponseTimeWithTimeout), 2);
		}
		
		final double jitterWithTimeout = responseList.size() < 1 ? INVALID_CALCULATION_VALUE : Math.sqrt(sumOfDiffsForJitterWithTimeout / responseList.size());
		final double jitterWithoutTimeout =  meanResponseTimeWithoutTimeoutMembersCount < 1 ? INVALID_CALCULATION_VALUE : Math.sqrt(sumOfDiffsForJitterWithoutTimeout / meanResponseTimeWithoutTimeoutMembersCount );

		final PingMeasurementCalculationsDTO calculations = new PingMeasurementCalculationsDTO();
		calculations.setAvailable(available);
		calculations.setMinResponseTime(available ? (int) minResponseTime : null);
		calculations.setMaxResponseTime(available ? (int) maxResponseTime : null);
		calculations.setMeanResponseTimeWithTimeout(available ? (int) Math.round(meanResponseTimeWithTimeout) : null);
		calculations.setMeanResponseTimeWithoutTimeout(available ? (int) Math.round(meanResponseTimeWithoutTimeout) : null);
		calculations.setJitterWithTimeout(available ? (int) Math.round(jitterWithTimeout) : null);
		calculations.setJitterWithoutTimeout(available ? (int) Math.round(jitterWithoutTimeout) : null);
		calculations.setSentInThisPing(sentInThisPing);
		calculations.setReceivedInThisPing(receivedInThisPing);
		calculations.setLostPerMeasurementPercent(lostPerMeasurementPercent);
		calculations.setMeasuredAt(aroundNow);

		return calculations;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO pingSystem(final SystemResponseDTO systemResponseDTO ) {
		logger.debug("pingSystem started...");

		final ZonedDateTime aroundNow = ZonedDateTime.now();

		if (systemResponseDTO == null || Utilities.isEmpty(systemResponseDTO.getAddress())) {
			throw new InvalidParameterException("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}

		final String address = systemResponseDTO.getAddress();

		final List<IcmpPingResponse> responseList = pingService.getPingResponseList(address);

		final QoSIntraMeasurement measurement = qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);
		final PingMeasurementCalculationsDTO calculationsDTO = handlePingMeasurement(measurement, responseList, aroundNow);

		qoSDBService.updateIntraMeasurement(aroundNow, measurement);

		return calculationsDTO;

	}

	//-------------------------------------------------------------------------------------------------
	private void copyCalculationsToPingMeasurement(final SystemResponseDTO systemResponseDTO, final PingMeasurementCalculationsDTO calculationsDTO) {
		logger.debug("copyCalculationsToPingMeasurement started...");

		final ZonedDateTime aroundNow = calculationsDTO.getMeasuredAt();

		if (systemResponseDTO == null || Utilities.isEmpty(systemResponseDTO.getAddress())) {
			throw new InvalidParameterException("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}

		final QoSIntraMeasurement measurement = qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);
		handlePingMeasurement(measurement, aroundNow, calculationsDTO);

		qoSDBService.updateIntraMeasurement(aroundNow, measurement);

	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO handlePingMeasurement(final QoSIntraMeasurement measurement, final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("handelPingMeasurement started...");

		final PingMeasurementCalculationsDTO calculationsDTO = calculatePingMeasurementValues(responseList, aroundNow);
		final Optional<QoSIntraPingMeasurement> pingMeasurementOptional = qoSDBService.getIntraPingMeasurementByMeasurement(measurement);

		if (pingMeasurementOptional.isEmpty()) {
			qoSDBService.createIntraPingMeasurement(measurement, calculationsDTO, aroundNow);

			if (pingMeasurementProperties.getLogMeasurementsToDB()) {
				final QoSIntraPingMeasurementLog measurementLogSaved = qoSDBService.logIntraMeasurementToDB(measurement.getSystem().getAddress(), calculationsDTO, aroundNow);

				if (pingMeasurementProperties.getLogMeasurementsDetailsToDB() && measurementLogSaved != null) {
					qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}
		} else {
			qoSDBService.updateIntraPingMeasurement(measurement, calculationsDTO, pingMeasurementOptional.get(), aroundNow);

			if (pingMeasurementProperties.getLogMeasurementsToDB()) {
				final QoSIntraPingMeasurementLog measurementLogSaved = qoSDBService.logIntraMeasurementToDB(measurement.getSystem().getAddress(), calculationsDTO, aroundNow);

				if (pingMeasurementProperties.getLogMeasurementsDetailsToDB() && measurementLogSaved != null) {
					qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}
		}

		return calculationsDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private void handlePingMeasurement(final QoSIntraMeasurement measurement, final ZonedDateTime aroundNow, final PingMeasurementCalculationsDTO calculationsDTO) {
		logger.debug("handelPingMeasurement started...");

		final Optional<QoSIntraPingMeasurement> pingMeasurementOptional = qoSDBService.getIntraPingMeasurementByMeasurement(measurement);

		if (pingMeasurementOptional.isEmpty()) {
			qoSDBService.createIntraPingMeasurement(measurement, calculationsDTO, aroundNow);
		} else {
			qoSDBService.updateIntraPingMeasurement(measurement, calculationsDTO, pingMeasurementOptional.get(), aroundNow);
		}

	}

	//-------------------------------------------------------------------------------------------------
	private ServiceRegistryListResponseDTO queryServiceRegistryAll() {
		logger.debug("queryServiceRegistryAll started...");

		try {
			final UriComponents queryBySystemDTOUri = getQueryAllUri();
			final ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(queryBySystemDTOUri, HttpMethod.GET, ServiceRegistryListResponseDTO.class);

			return response.getBody();
		} catch (final ArrowheadException ex) {
			logger.debug("Exception: " + ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryAllUri() {
		logger.debug("getQueryUri started...");

		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
			}
		} else {
			throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private boolean calculateAvailable(final int sentInThisPing, final int availableCount) {
		logger.debug("calculateAvailable started...");

		final int availableFromSuccessPercent = pingMeasurementProperties.getAvailableFromSuccessPercent();
		final double availablePercent = 100 - ((sentInThisPing - availableCount) / (double)sentInThisPing) * 100 ;

		return availableFromSuccessPercent <= Math.round(availablePercent);
	}
}