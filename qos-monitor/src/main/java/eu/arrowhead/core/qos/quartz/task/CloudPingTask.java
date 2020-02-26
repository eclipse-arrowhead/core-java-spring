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
import org.icmp4j.IcmpPingResponse;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSInterMeasurement;
import eu.arrowhead.common.database.entity.QoSInterPingMeasurement;
import eu.arrowhead.common.database.entity.QoSInterPingMeasurementLog;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.measurement.properties.InterPingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;

@Component
@DisallowConcurrentExecution
public class CloudPingTask implements Job {

	//=================================================================================================
	// members

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
		logger.debug("STARTED: cloud ping task");

		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {

			logger.debug("Finished: cloud ping task can not run if server is in standalon mode");
			return;
		}

		final CloudWithRelaysListResponseDTO responseDTO = qoSMonitorDriver.queryGatekeeperAllCloud();
		final Set<CloudResponseDTO> clouds = getCloudsFromResponse(responseDTO);
		if (clouds == null || clouds.isEmpty()) {

			return;
		}

		final CloudResponseDTO cloudResponseDTO = chooseCloudToMeasure(clouds);
		final Set<String> systemAddressSet = qoSMonitorDriver.queryGatekeeperAllSystemAddresses(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloudResponseDTO)).getAddresses();
		if (systemAddressSet == null || systemAddressSet.isEmpty()) {

			return;
		}

		for (final String address : systemAddressSet) {

			if (!Utilities.isEmpty(address)) {
				pingSystem(address, cloudResponseDTO);
			}

		}

		logger.debug("Finished: ping task success");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Set<CloudResponseDTO> getCloudsFromResponse(final CloudWithRelaysListResponseDTO responseDTO) {
		logger.debug("getCloudsFromResponse started...");

		final List<CloudRequestDTO> cloudsToRequest = new ArrayList<>();
		for (final CloudWithRelaysResponseDTO cloudWithRelay : responseDTO.getData()) {

			if (cloudWithRelay != null && !cloudWithRelay.getOwnCloud()) {
				cloudsToRequest.add(DTOConverter.convertCloudWithRelaysResponseDTOToCloudRequestDTO(cloudWithRelay));
			}
		}
		
		return filterCloudsByAccessType(cloudsToRequest, responseDTO);
	}

	//-------------------------------------------------------------------------------------------------
	private Set<CloudResponseDTO> filterCloudsByAccessType(final List<CloudRequestDTO> cloudsToRequest,
			final CloudWithRelaysListResponseDTO responseDTO) {
		logger.debug("filterCloudsByAccessType started...");

		final List<CloudAccessResponseDTO> cloudAccessResponseDTOList = qoSMonitorDriver.queryGatekeeperGatewayIsMandatory(cloudsToRequest).getData();
		if (cloudAccessResponseDTOList == null || cloudAccessResponseDTOList.isEmpty()) {
			return null;
		}

		final Set<CloudResponseDTO> clouds = new HashSet<>();
		for (final CloudWithRelaysResponseDTO cloudWithRelay : responseDTO.getData()) {

			if (cloudWithRelay != null && !cloudWithRelay.getOwnCloud()) {
				if( cloudIsDirectlyAccessable(cloudWithRelay, cloudAccessResponseDTOList)) {

					clouds.add(DTOConverter.convertCloudWithRelaysResponseDTOToCloudResponseDTO(cloudWithRelay));
				}
			}
		}

		return clouds;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean cloudIsDirectlyAccessable(final CloudWithRelaysResponseDTO cloudWithRelay,
			final List<CloudAccessResponseDTO> cloudAccessResponseDTOList) {
		logger.debug("cloudIsDirectlyAccessable started...");

		for (final CloudAccessResponseDTO cloudAccessResponseDTO : cloudAccessResponseDTOList) {

			if (cloudAccessResponseDTO != null && cloudAccessResponseDTO.isDirectAccess()) {
				if ( (cloudWithRelay.getName().equalsIgnoreCase(cloudAccessResponseDTO.getCloudName())) && 
						(cloudWithRelay.getOperator().equalsIgnoreCase(cloudAccessResponseDTO.getCloudOperator())) ) {

					return true;
				}
			}
		}

		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO chooseCloudToMeasure(final Set<CloudResponseDTO> cloudList) {
		logger.debug("chooseCloudToMeasure started...");

		if(cloudList == null || cloudList.isEmpty()) {

			return null;
		}

		boolean cloudToMeasureFound = false;
		CloudResponseDTO cloudToMeasure = null;

		final HashMap<ZonedDateTime, CloudResponseDTO> earliestUpdatedAtMapedToCloud = new HashMap<>(cloudList.size());
		for (final CloudResponseDTO cloudResponseDTO : cloudList) {

			final List<QoSInterMeasurement> measurementList = qoSDBService.getInterMeasurementByCloud(cloudResponseDTO);
			if (measurementList.isEmpty()) {
				// has no measurement for the cloud yet

				cloudToMeasure = cloudResponseDTO;
				cloudToMeasureFound = true;
				break;

			}else {

				ZonedDateTime min = ZonedDateTime.now();
				for (final QoSInterMeasurement qoSInterMeasurement : measurementList) {
					if (qoSInterMeasurement.getUpdatedAt().isBefore(min)) {
						min = qoSInterMeasurement.getUpdatedAt();
					}
				}

				earliestUpdatedAtMapedToCloud.put(min, cloudResponseDTO);
			}
		}

		if (!cloudToMeasureFound) {
			ZonedDateTime earliestMeasurement = ZonedDateTime.now();
			for (final ZonedDateTime date : earliestUpdatedAtMapedToCloud.keySet()) {
				if (date.isBefore(earliestMeasurement)) {
					earliestMeasurement = date;
				}
			}

			cloudToMeasure = earliestUpdatedAtMapedToCloud.get(earliestMeasurement);
		}

		return cloudToMeasure;
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

			final boolean successFlag = icmpPingResponse.getSuccessFlag();

			if (successFlag) {
				++receivedInThisPing;
				final long duration = icmpPingResponse.getDuration();

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

			final boolean successFlag = icmpPingResponse.getSuccessFlag();
			final double duration;
			if (successFlag) {
				 duration = icmpPingResponse.getDuration();
				 sumOfDiffsForJitterWithoutTimeout += Math.pow( (duration - meanResponseTimeWithoutTimeout), 2);
			} else {
				duration = timeout;
			}

			sumOfDiffsForJitterWithTimeout += Math.pow( (duration - meanResponseTimeWithTimeout), 2);
		}
		final double jitterWithTimeout = meanResponseTimeWithoutTimeoutMembersCount < 1 ? INVALID_CALCULATION_VALUE : Math.sqrt(sumOfDiffsForJitterWithTimeout / meanResponseTimeWithoutTimeoutMembersCount);
		final double jitterWithoutTimeout = responseList.size() < 1 ? INVALID_CALCULATION_VALUE : Math.sqrt(sumOfDiffsForJitterWithoutTimeout / responseList.size());

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

		final QoSInterMeasurement measurement = qoSDBService.getOrCreateInterMeasurement(address, cloud);
		final PingMeasurementCalculationsDTO calculationsDTO = handleInterPingMeasurement(measurement, responseList, aroundNow);

		qoSDBService.updateInterMeasurement(aroundNow, measurement);

		return calculationsDTO;

	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO handleInterPingMeasurement(final QoSInterMeasurement measurement,
			final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("handelPingMeasurement started...");

		final PingMeasurementCalculationsDTO calculationsDTO = calculatePingMeasurementValues(responseList, aroundNow);
		final Optional<QoSInterPingMeasurement> pingMeasurementOptional = qoSDBService.getInterPingMeasurementByMeasurement(measurement);

		if (pingMeasurementOptional.isEmpty()) {

			qoSDBService.createInterPingMeasurement(measurement, calculationsDTO, aroundNow);

			if (pingMeasurementProperties.getLogMeasurementsToDB()) {

				final QoSInterPingMeasurementLog measurementLogSaved = qoSDBService.logInterMeasurementToDB(measurement.getAddress(), calculationsDTO, aroundNow);

				if (pingMeasurementProperties.getLogMeasurementsDetailsToDB() && measurementLogSaved != null) {

					qoSDBService.logInterMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}

		} else {

			qoSDBService.updateInterPingMeasurement(measurement, calculationsDTO, pingMeasurementOptional.get(), aroundNow);

			if(pingMeasurementProperties.getLogMeasurementsToDB()) {

				final QoSInterPingMeasurementLog measurementLogSaved = qoSDBService.logInterMeasurementToDB(measurement.getAddress(), calculationsDTO, aroundNow);

				if(pingMeasurementProperties.getLogMeasurementsDetailsToDB() && measurementLogSaved != null) {

					qoSDBService.logInterMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
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
