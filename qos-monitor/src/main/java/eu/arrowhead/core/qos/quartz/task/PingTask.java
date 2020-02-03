package eu.arrowhead.core.qos.quartz.task;

import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
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
import eu.arrowhead.core.qos.database.service.QoSDatabaseService;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;

@Component
@DisallowConcurrentExecution
public class PingTask implements Job {

	//=================================================================================================
	// members
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final int TIMES_TO_REPEAT = 35;
	private static final int PING_TIME_OUT = 5000;
	private static final int PING_POCKET_SIZE = 32;
	private static final int REST_BETWEEN_PINGS_MILLSEC = 1000;

	private static final boolean LOG_MEASUREMENT = true;
	private static final boolean LOG_MEASUREMENT_DETAILS = true;

	protected Logger logger = LogManager.getLogger(PingTask.class);

	@Autowired
	private QoSDatabaseService qoSDatabaseService;

	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: ping  task");

		final Set<SystemResponseDTO> systems = getSystemsToMeasure();
		for (final SystemResponseDTO systemResponseDTO : systems) {

			pingSystem(systemResponseDTO);

		}

		logger.debug("Finished: ping  task");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Set<SystemResponseDTO> getSystemsToMeasure() {
		logger.debug("getSystemToMessure started...");

		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = queryServiceRegistryAll();
		final Set<SystemResponseDTO> systemList = serviceRegistryListResponseDTO.getData().stream().map(ServiceRegistryResponseDTO::getProvider).collect(Collectors.toSet());

		return systemList;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO calculatePingMeasurementValues(List<IcmpPingResponse> responseList) {
		logger.debug("calculatePingMeasurementValues started...");

		final int sentInThisPing = responseList.size();
		Assert.isTrue(sentInThisPing > 0, "Sent in this Ping value must be greater than zero");

		boolean available = false;
		int receivedInThisPing = 0;
		long maxResponseTime = 0;
		long minResponseTime = Integer.MAX_VALUE;
		double sumOfDurationForMeanResponseTimeWithTimeout = 0;
		double sumOfDurationForMeanResponseTimeWithoutTimeout = 0;
		int meanResponseTimeWithoutTimeoutMembersCount = 0;

		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final boolean successFlag = icmpPingResponse.getSuccessFlag();
			
			if (successFlag) {
				available = true;
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
				sumOfDurationForMeanResponseTimeWithTimeout += PING_TIME_OUT;
			}
		}

		int lostPerMeasurementPercent = (int) (receivedInThisPing == 0 ? 100 : 100 - ((double)receivedInThisPing / sentInThisPing) * 100);

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
				duration = PING_TIME_OUT + 1;
			}

			sumOfDiffsForJitterWithTimeout += Math.pow( (duration - meanResponseTimeWithTimeout), 2);
		}
		double jitterWithTimeout = Math.sqrt(sumOfDiffsForJitterWithTimeout / meanResponseTimeWithoutTimeoutMembersCount);
		double jitterWithoutTimeout =  Math.sqrt(sumOfDiffsForJitterWithoutTimeout / responseList.size());

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

		final List<IcmpPingResponse> responseList = getPingResponseList(address);

		final QoSIntraMeasurement measurement = qoSDatabaseService.getMeasurement(systemResponseDTO, aroundNow);
		handelPingMeasurement(measurement, responseList, aroundNow);

		qoSDatabaseService.updateMeasurement(aroundNow, measurement);

	}

	//-------------------------------------------------------------------------------------------------
	private void handelPingMeasurement(final QoSIntraMeasurement measurement,
			final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("handelPingMeasurement started...");

		final PingMeasurementCalculationsDTO calculationsDTO = calculatePingMeasurementValues(responseList);
		final QoSIntraPingMeasurement pingMeasurement;
		final Optional<QoSIntraPingMeasurement> pingMeasurementOptional =  qoSDatabaseService.getPingMeasurementByMeasurement(measurement);

		if (pingMeasurementOptional.isEmpty()) {

			pingMeasurement = qoSDatabaseService.createPingMeasurement(measurement, responseList, calculationsDTO, aroundNow);

			if(LOG_MEASUREMENT) {

				final QoSIntraPingMeasurementLog measurementLogSaved = qoSDatabaseService.logMeasurementToDB(measurement.getSystem().getAddress(), pingMeasurement, aroundNow);

				if(LOG_MEASUREMENT_DETAILS && measurementLogSaved != null) {

					qoSDatabaseService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
				}
			}

		}else {
			pingMeasurement = qoSDatabaseService.updatePingMeasurement(measurement, calculationsDTO, pingMeasurementOptional.get(), aroundNow);

			if(LOG_MEASUREMENT) {

				final QoSIntraPingMeasurementLog measurementLogSaved = qoSDatabaseService.logMeasurementToDB(measurement.getSystem().getAddress(), pingMeasurement, aroundNow);

				if(LOG_MEASUREMENT_DETAILS && measurementLogSaved != null) {

					qoSDatabaseService.logMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);
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

		} catch (final ClassCastException ex) {
			throw new ArrowheadException("QoS Mointor can't find Service Registry Query All  URI.");
		}

	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryAllUri() {
		logger.debug("getQueryUri started...");

		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find Service Registry Query All  URI.");
			}
		}

		throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getPingResponseList(final String address) {
		logger.debug("getPingResponseList started...");

		final List<IcmpPingResponse> responseList = new ArrayList<>(TIMES_TO_REPEAT);
		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest ();
			request.setHost (address);
			request.setTimeout(PING_TIME_OUT);
			request.setPacketSize(PING_POCKET_SIZE);

			for (int count = 0; count < TIMES_TO_REPEAT; count ++) {
				IcmpPingResponse response;
				try {
					response = IcmpPingUtil.executePingRequest (request);
					final String formattedResponse = IcmpPingUtil.formatResponse (response);
					logger.debug(formattedResponse);

					responseList.add(response);
				} catch (final Exception ex) {
					response = new IcmpPingResponse();
					response.setErrorMessage(ex.getMessage());
					response.setSuccessFlag(false);
					response.setThrowable(ex);

					responseList.add(response);
				}

				Thread.sleep (REST_BETWEEN_PINGS_MILLSEC);
			}
		} catch ( final InterruptedException | IllegalArgumentException ex) {
			logger.debug("" + ex.getMessage());
		}

		return responseList;
	}

}