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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.QoSInterDirectMeasurement;
import eu.arrowhead.common.database.entity.QoSInterDirectPingMeasurement;
import eu.arrowhead.common.database.entity.QoSInterDirectPingMeasurementLog;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.measurement.properties.InterPingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;

@Component
@DisallowConcurrentExecution
public class CloudPingTask implements Job {

	//=================================================================================================
	// members
	
	@Resource(name = "cloudPingSheduler")
	private Scheduler cloudPingTaskScheduler;

	private static final List<CoreSystemService> REQUIRED_CORE_SERVICES = List.of(CoreSystemService.GATEKEEPER_PULL_CLOUDS, CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES,
																				  CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES);
	
	private static final int INVALID_CALCULATION_VALUE = -1;

	protected Logger logger = LogManager.getLogger(CloudPingTask.class);

	@Autowired
	private InterPingMeasurementProperties pingMeasurementProperties;

	@Autowired
	private PingService pingService;

	@Autowired
	private QoSDBService qoSDBService;

	@Autowired
	private QoSMonitorDriver qoSMonitorDriver;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Cloud Ping task");

		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {			
			logger.debug("FINISHED: Cloud Ping task can not run if server is in standalon mode");
			shutdown();
			return;
		}
		
		if (!checkRequiredCoreSystemServiceUrisAvailable()) {
			logger.debug("FINISHED: Cloud Ping task. Reqired Core System Sevice URIs aren't available");
			return;
		}
			
		try {
			final CloudWithRelaysAndPublicRelaysListResponseDTO responseDTO = qoSMonitorDriver.queryGatekeeperAllCloud();
			final Set<CloudResponseDTO> clouds = getCloudsFromResponse(responseDTO);
			if (clouds == null || clouds.isEmpty()) {
				logger.debug("FINISHED: Cloud Ping task. Have no neighbor cloud registered");
				return;
			}
			
			final CloudResponseDTO cloudResponseDTO = chooseCloudToMeasure(clouds);
			final Set<String> systemAddressSet = qoSMonitorDriver.queryGatekeeperAllSystemAddresses(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloudResponseDTO)).getAddresses();
			if (systemAddressSet == null || systemAddressSet.isEmpty()) {
				logger.debug("FINISHED: Cloud Ping task. Have no intercloud provider with direct access.");
				return;
			}
			
			for (final String address : systemAddressSet) {
				if (!Utilities.isEmpty(address)) {
					pingSystem(address, cloudResponseDTO);
				}
			}
			
			logger.debug("FINISHED: Cloud Ping task success");			
		} catch (final ArrowheadException ex) {
			logger.debug("FAILED: Cloud Ping task: " + ex.getMessage());
		}
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void shutdown() {
		logger.debug("shutdown started...");
		try {
			cloudPingTaskScheduler.shutdown();
			logger.debug("SHUTDOWN: Cloud Ping task");
		} catch (final SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean checkRequiredCoreSystemServiceUrisAvailable() {
		logger.debug("checkRequiredCoreSystemServiceUrisAvailable started...");
		for (final CoreSystemService coreSystemService : REQUIRED_CORE_SERVICES) {
			final String key = coreSystemService.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
			if (!arrowheadContext.containsKey(key) && !(arrowheadContext.get(key) instanceof UriComponents)) {
				return false;
			}
		}
		return true;
	}

	//-------------------------------------------------------------------------------------------------
	private Set<CloudResponseDTO> getCloudsFromResponse(final CloudWithRelaysAndPublicRelaysListResponseDTO responseDTO) {
		logger.debug("getCloudsFromResponse started...");

		final List<CloudRequestDTO> cloudsToRequest = new ArrayList<>();
		for (final CloudWithRelaysAndPublicRelaysResponseDTO cloudWithRelay : responseDTO.getData()) {

			if (cloudWithRelay != null && !cloudWithRelay.getOwnCloud()) {
				cloudsToRequest.add(DTOConverter.convertCloudWithRelaysResponseDTOToCloudRequestDTO(cloudWithRelay));
			}
		}
		
		return filterCloudsByAccessType(cloudsToRequest, responseDTO);
	}

	//-------------------------------------------------------------------------------------------------
	private Set<CloudResponseDTO> filterCloudsByAccessType(final List<CloudRequestDTO> cloudsToRequest,
			final CloudWithRelaysAndPublicRelaysListResponseDTO responseDTO) {
		logger.debug("filterCloudsByAccessType started...");

		final List<CloudAccessResponseDTO> cloudAccessResponseDTOList = qoSMonitorDriver.queryGatekeeperCloudAccessTypes(cloudsToRequest).getData();
		if (cloudAccessResponseDTOList == null || cloudAccessResponseDTOList.isEmpty()) {
			return null;
		}

		final Set<CloudResponseDTO> clouds = new HashSet<>();
		for (final CloudWithRelaysAndPublicRelaysResponseDTO cloudWithRelay : responseDTO.getData()) {
			if (cloudWithRelay != null && !cloudWithRelay.getOwnCloud()) {
				if (cloudIsDirectlyAccessable(cloudWithRelay, cloudAccessResponseDTOList)) {
					clouds.add(cloudWithRelay);
				}
			}
		}

		return clouds;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean cloudIsDirectlyAccessable(final CloudWithRelaysAndPublicRelaysResponseDTO cloudWithRelay, final List<CloudAccessResponseDTO> cloudAccessResponseDTOList) {
		logger.debug("cloudIsDirectlyAccessable started...");

		for (final CloudAccessResponseDTO cloudAccessResponseDTO : cloudAccessResponseDTOList) {
			if (cloudAccessResponseDTO != null && cloudAccessResponseDTO.isDirectAccess()) {
				if ((cloudWithRelay.getName().equalsIgnoreCase(cloudAccessResponseDTO.getCloudName())) && 
						(cloudWithRelay.getOperator().equalsIgnoreCase(cloudAccessResponseDTO.getCloudOperator()))) {
					return true;
				}
			}
		}

		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO chooseCloudToMeasure(final Set<CloudResponseDTO> cloudList) {
		logger.debug("chooseCloudToMeasure started...");

		boolean cloudToMeasureFound = false;
		CloudResponseDTO cloudToMeasure = null;

		final HashMap<ZonedDateTime, CloudResponseDTO> earliestUpdatedAtMappedToCloud = new HashMap<>(cloudList.size());
		for (final CloudResponseDTO cloudResponseDTO : cloudList) {

			final List<QoSInterDirectMeasurement> measurementList = qoSDBService.getInterDirectMeasurementByCloud(cloudResponseDTO, QoSMeasurementType.PING);
			if (measurementList.isEmpty()) {
				// has no measurement for the cloud yet

				cloudToMeasure = cloudResponseDTO;
				cloudToMeasureFound = true;
				break;
			} else {
				ZonedDateTime min = ZonedDateTime.now();
				for (final QoSInterDirectMeasurement qoSInterMeasurement : measurementList) {
					if (qoSInterMeasurement.getUpdatedAt().isBefore(min)) {
						min = qoSInterMeasurement.getUpdatedAt();
					}
				}

				earliestUpdatedAtMappedToCloud.put(min, cloudResponseDTO);
			}
		}

		if (!cloudToMeasureFound) {
			ZonedDateTime earliestMeasurement = ZonedDateTime.now();
			for (final ZonedDateTime date : earliestUpdatedAtMappedToCloud.keySet()) {
				if (date.isBefore(earliestMeasurement)) {
					earliestMeasurement = date;
				}
			}

			cloudToMeasure = earliestUpdatedAtMappedToCloud.get(earliestMeasurement);
		}

		return cloudToMeasure;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO calculatePingMeasurementValues(final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("calculatePingMeasurementValues started...");

		Assert.notNull(responseList, "ResponseList is null");
		Assert.notNull(aroundNow, "AroundNow is null");
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
	private PingMeasurementCalculationsDTO pingSystem(final String address , final CloudResponseDTO cloud) {
		logger.debug("pingSystem started...");

		final ZonedDateTime aroundNow = ZonedDateTime.now();

		final List<IcmpPingResponse> responseList = pingService.getPingResponseList(address);
		if(responseList == null) {
			throw new ArrowheadException("Ping Service response is null");
		}

		final QoSInterDirectMeasurement measurement = qoSDBService.getOrCreateDirectInterMeasurement(address, cloud, QoSMeasurementType.PING);
		final PingMeasurementCalculationsDTO calculationsDTO = handleInterPingMeasurement(measurement, responseList, aroundNow);

		qoSDBService.updateInterDirectMeasurement(aroundNow, measurement);

		return calculationsDTO;

	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO handleInterPingMeasurement(final QoSInterDirectMeasurement measurement, final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("handelPingMeasurement started...");

		final PingMeasurementCalculationsDTO calculationsDTO = calculatePingMeasurementValues(responseList, aroundNow);
		final Optional<QoSInterDirectPingMeasurement> pingMeasurementOptional = qoSDBService.getInterDirectPingMeasurementByMeasurement(measurement);

		if (pingMeasurementOptional.isEmpty()) {

			qoSDBService.createInterDirectPingMeasurement(measurement, calculationsDTO, aroundNow);

			if (pingMeasurementProperties.getLogMeasurementsToDB()) {
				final QoSInterDirectPingMeasurementLog measurementLogSaved = qoSDBService.logInterDirectMeasurementToDB(measurement.getAddress(), calculationsDTO, aroundNow);

				if (pingMeasurementProperties.getLogMeasurementsDetailsToDB() && measurementLogSaved != null) {
					qoSDBService.logInterDirectMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}
		} else {
			qoSDBService.updateInterDirectPingMeasurement(measurement, calculationsDTO, pingMeasurementOptional.get(), aroundNow);

			if (pingMeasurementProperties.getLogMeasurementsToDB()) {
				final QoSInterDirectPingMeasurementLog measurementLogSaved = qoSDBService.logInterDirectMeasurementToDB(measurement.getAddress(), calculationsDTO, aroundNow);

				if (pingMeasurementProperties.getLogMeasurementsDetailsToDB() && measurementLogSaved != null) {
					qoSDBService.logInterDirectMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}
		}

		return calculationsDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean calculateAvailable(final int sentInThisPing, final int availableCount) {
		logger.debug("calculateAvailable started...");

		final int availableFromSuccessPercent = pingMeasurementProperties.getAvailableFromSuccessPercent();
		final double availablePercent = 100 - ((sentInThisPing - availableCount) / (double)sentInThisPing) * 100 ;

		return availableFromSuccessPercent <= Math.round(availablePercent);
	}
}