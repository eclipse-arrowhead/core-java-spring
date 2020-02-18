package eu.arrowhead.core.qos.quartz.task;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingResponse;
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
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;

@Component
@DisallowConcurrentExecution
public class PingTask implements Job {

	//=================================================================================================
	// members
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";

	private static final boolean LOG_MEASUREMENT = true;
	private static final boolean LOG_MEASUREMENT_DETAILS = true;

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

		final Set<SystemResponseDTO> systems = getSystemsToMeasure();
		if (systems == null) {

			return;
		}

		for (final SystemResponseDTO systemResponseDTO : systems) {

			pingSystem(systemResponseDTO);

		}

		logger.debug("Finished: ping task success");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Set<SystemResponseDTO> getSystemsToMeasure() {
		logger.debug("getSystemToMessure started...");

		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = queryServiceRegistryAll();
		if(serviceRegistryListResponseDTO == null || serviceRegistryListResponseDTO.getData() == null ||  serviceRegistryListResponseDTO.getData().isEmpty()) {

			return null;
		}
		final Set<SystemResponseDTO> systemList = serviceRegistryListResponseDTO.getData().stream().map(ServiceRegistryResponseDTO::getProvider).collect(Collectors.toSet());

		return systemList;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO calculatePingMeasurementValues(final List<IcmpPingResponse> responseList) {
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
		int availableCount = 0;

		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final boolean successFlag = icmpPingResponse.getSuccessFlag();

			if (successFlag) {
				++availableCount;
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

			}else {
				sumOfDurationForMeanResponseTimeWithTimeout += timeout;
			}
		}

		final boolean available = calculateAvailable(sentInThisPing, availableCount);
		final int lostPerMeasurementPercent = (int) (receivedInThisPing == 0 ? 100 : 100 - ((double)receivedInThisPing / sentInThisPing) * 100);

		final double meanResponseTimeWithTimeout = sumOfDurationForMeanResponseTimeWithTimeout / responseList.size();
		final double meanResponseTimeWithoutTimeout = sumOfDurationForMeanResponseTimeWithoutTimeout / meanResponseTimeWithoutTimeoutMembersCount;

		double sumOfDiffsForJitterWithTimeout = 0;
		double sumOfDiffsForJitterWithoutTimeout =0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final boolean successFlag = icmpPingResponse.getSuccessFlag();
			final double duration;
			if (successFlag) {
				 duration = icmpPingResponse.getDuration();
				 sumOfDiffsForJitterWithoutTimeout += Math.pow( (duration - meanResponseTimeWithoutTimeout), 2);
			}else {
				duration = timeout;
			}

			sumOfDiffsForJitterWithTimeout += Math.pow( (duration - meanResponseTimeWithTimeout), 2);
		}
		final double jitterWithTimeout = Math.sqrt(sumOfDiffsForJitterWithTimeout / meanResponseTimeWithoutTimeoutMembersCount);
		final double jitterWithoutTimeout =  Math.sqrt(sumOfDiffsForJitterWithoutTimeout / responseList.size());

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

		return calculations;
	}

	//-------------------------------------------------------------------------------------------------
	private void pingSystem(final SystemResponseDTO systemResponseDTO ) {
		logger.debug("pingSystem started...");

		final ZonedDateTime aroundNow = ZonedDateTime.now();

		if (systemResponseDTO == null || Utilities.isEmpty(systemResponseDTO.getAddress())) {
			throw new InvalidParameterException("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}

		final String address = systemResponseDTO.getAddress();

		final List<IcmpPingResponse> responseList = pingService.getPingResponseList(address);

		final QoSIntraMeasurement measurement = qoSDBService.getOrCreateMeasurement(systemResponseDTO);
		handlePingMeasurement(measurement, responseList, aroundNow);

		qoSDBService.updateMeasurement(aroundNow, measurement);

	}

	//-------------------------------------------------------------------------------------------------
	private void handlePingMeasurement(final QoSIntraMeasurement measurement,
			final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("handelPingMeasurement started...");

		final PingMeasurementCalculationsDTO calculationsDTO = calculatePingMeasurementValues(responseList);
		final Optional<QoSIntraPingMeasurement> pingMeasurementOptional = qoSDBService.getPingMeasurementByMeasurement(measurement);

		if (pingMeasurementOptional.isEmpty()) {

			qoSDBService.createPingMeasurement(measurement, calculationsDTO, aroundNow);

			if(LOG_MEASUREMENT) {

				final QoSIntraPingMeasurementLog measurementLogSaved = qoSDBService.logMeasurementToDB(measurement.getSystem().getAddress(), calculationsDTO, aroundNow);

				if(LOG_MEASUREMENT_DETAILS && measurementLogSaved != null) {

					qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}

		}else {

			qoSDBService.updatePingMeasurement(measurement, calculationsDTO, pingMeasurementOptional.get(), aroundNow);

			if(LOG_MEASUREMENT) {

				final QoSIntraPingMeasurementLog measurementLogSaved = qoSDBService.logMeasurementToDB(measurement.getSystem().getAddress(), calculationsDTO, aroundNow);

				if(LOG_MEASUREMENT_DETAILS && measurementLogSaved != null) {

					qoSDBService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}
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

			throw ex;

		} catch (final Throwable ex) {

			logger.debug("Exception:", ex.getMessage());
			return null;
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
		}else {
			throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private boolean calculateAvailable(final int sentInThisPing, final int availableCount) {
		logger.debug("calculateAvailable started...");

		final boolean available;
		final int availableFromSuccessPercent = pingMeasurementProperties.getAvailableFromSuccessPercet();
		final double availablePercent = 100 - ((sentInThisPing - availableCount) / (double)sentInThisPing) * 100 ;

		if (availableFromSuccessPercent <= Math.round(availablePercent)) {

			available = true;
		}else {
			available = false;
		}

		return available;
	}

}