package eu.arrowhead.core.qos.quartz.task;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;

@Component
@DisallowConcurrentExecution
public class PingTask implements Job {

	//=================================================================================================
	// members
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final int TIMES_TO_REPEAT = 35;
	private static final int PING_TIME_OUT = 5000;
	private static final int REST_BETWEEN_PINGS_MILLSEC = 1000;

	protected Logger logger = LogManager.getLogger(PingTask.class);

	@Autowired
	private QoSIntraMeasurementRepository qoSIntraMeasurementRepository;

	@Autowired
	private QoSIntraMeasurementPingRepository qoSIntraMeasurementPingRepository;

	@Autowired
	private SystemRepository systemRepository;

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

		final Set<SystemResponseDTO> systems = getSystemsToMessure();
		for (final SystemResponseDTO systemResponseDTO : systems) {

			pingSystem(systemResponseDTO);

		}

		logger.debug("Finished: ping  task");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Set<SystemResponseDTO> getSystemsToMessure() {
		logger.debug("getSystemToMessure started...");

		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = queryServiceRegistryAll();
		final Set<SystemResponseDTO> systemList = serviceRegistryListResponseDTO.getData().stream().map(ServiceRegistryResponseDTO::getProvider).collect(Collectors.toSet());

		return systemList;
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class) 
	private QoSIntraMeasurement createMeasurement(final System system,final QoSMeasurementType ping,final ZonedDateTime aroundNow) {
		logger.debug("createMeasurement started...");

		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(system, QoSMeasurementType.PING, aroundNow);
		measurement.setSystem(system);
		measurement.setMeasurementType(QoSMeasurementType.PING);

	 	qoSIntraMeasurementRepository.saveAndFlush(measurement);
	 	//qoSIntraMeasurementRepository.refresh(measurement);

	 	return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	private QoSIntraPingMeasurement createPingMeasurement(final QoSIntraMeasurement measurementParam,
	final List<IcmpPingResponse> responseList, final ZonedDateTime aroundNow) {
		logger.debug("createPingMeasurement started...");

		final boolean available = calculateAvailable(responseList);
		final int maxResponseTime = calculateMaxResponseTime(responseList);
		final int minResponseTime = calculateMinResponseTime(responseList);
		final int meanResponseTimeWithTimeout = calculateMeanResponseTimeWithTimeout(responseList);
		final int meanResponseTimeWithOutTimeout = calculateMeanResponseTimeWithoutTimeout(responseList);
		final int jitterWithTimeout = calculateJitterWithTimeout(responseList);
		final int jitterWithoutTimeout = calculateJitterWithoutTimeout(responseList);
		
		final ZonedDateTime countStartedAt = aroundNow;

		long sent = 0;
		long sentAll = 0;
		long received = 0;
		long receivedAll = 0;

		final int sentInThisPing = getSentInThisPing(responseList);
		final int receivedInThisPing = getReceivedInThisPing(responseList);

		final int lostPerMeasurementPercent = (int) (receivedInThisPing == 0 ? 1 : ((double)receivedInThisPing / (double)sentInThisPing) * 100);

		final QoSIntraMeasurement measurement ;
		final Optional<QoSIntraMeasurement> meOptional = qoSIntraMeasurementRepository.findById(measurementParam.getId());
		if (meOptional.isPresent()) {
			measurement = meOptional.get();
		}else {
			throw new ArrowheadException("Could not find measurment in DB");
		}

		final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();

		sent = pingMeasurement.getSent();
		sentAll = pingMeasurement.getSentAll();
		received = pingMeasurement.getReceived();
		receivedAll = pingMeasurement.getReceivedAll();

		pingMeasurement.setMeasurement(measurement);
		pingMeasurement.setAvailable(available);
		pingMeasurement.setMaxResponseTime(maxResponseTime);
		pingMeasurement.setMinResponseTime(minResponseTime);
		pingMeasurement.setMeanResponseTimeWithoutTimeout(meanResponseTimeWithOutTimeout);
		pingMeasurement.setMeanResponseTimeWithTimeout(meanResponseTimeWithTimeout);
		pingMeasurement.setJitterWithoutTimeout(jitterWithoutTimeout);
		pingMeasurement.setJitterWithTimeout(jitterWithTimeout);
		pingMeasurement.setLostPerMeasurementPercent(lostPerMeasurementPercent);
		pingMeasurement.setCountStartedAt(countStartedAt);
		pingMeasurement.setLastAccessAt(calculateLastAccessAt(responseList, pingMeasurement, aroundNow));
		pingMeasurement.setSent(sent + sentInThisPing);
		pingMeasurement.setSentAll(sentAll + sentInThisPing);
		pingMeasurement.setReceived(received + receivedInThisPing);
		pingMeasurement.setReceivedAll(receivedAll + getReceivedInThisPing(responseList));

		qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);

		return pingMeasurement;
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional (rollbackFor = ArrowheadException.class)
	private void updateMeasurement(ZonedDateTime aroundNow,final QoSIntraMeasurement measurement) {

	 	measurement.setLastMeasurementAt(aroundNow);
	 	qoSIntraMeasurementRepository.saveAndFlush(measurement);

	}

	//-------------------------------------------------------------------------------------------------
	private void pingSystem(final SystemResponseDTO systemResponseDTO ) {
		logger.debug("pingSystem started...");

		final ZonedDateTime aroundNow = ZonedDateTime.now();

		if (systemResponseDTO == null || Utilities.isEmpty(systemResponseDTO.getAddress())) {
			throw new InvalidParameterException("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}

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

		final String address = systemResponseDTO.getAddress();

		final List<IcmpPingResponse> responseList = getPingResponseList(address);

		final boolean available = calculateAvailable(responseList);
		final int maxResponseTime = calculateMaxResponseTime(responseList);
		final int minResponseTime = calculateMinResponseTime(responseList);
		final int meanResponseTimeWithTimeout = calculateMeanResponseTimeWithTimeout(responseList);
		final int meanResponseTimeWithOutTimeout = calculateMeanResponseTimeWithoutTimeout(responseList);
		final int jitterWithTimeout = calculateJitterWithTimeout(responseList);
		final int jitterWithoutTimeout = calculateJitterWithoutTimeout(responseList);

		final ZonedDateTime countStartedAt = aroundNow;

		final int sentInThisPing = getSentInThisPing(responseList);
		final int receivedInThisPing = getReceivedInThisPing(responseList);
		final int lostPerMeasurementPercent = (int) (receivedInThisPing == 0 ? 1 : 100 - ((double)receivedInThisPing / (double)sentInThisPing) * 100);

		final QoSIntraMeasurement measurement;
		final Optional<QoSIntraMeasurement> qoSIntraMeasurementOptional = qoSIntraMeasurementRepository.findBySystemAndMeasurementType(system, QoSMeasurementType.PING);
		if (qoSIntraMeasurementOptional.isEmpty()) {
			measurement = createMeasurement(system, QoSMeasurementType.PING, aroundNow);
		}else {
			 measurement = qoSIntraMeasurementOptional.get();
		}

		final QoSIntraPingMeasurement pingMeasurement;
		final Optional<QoSIntraPingMeasurement> pingMeasurementOptional = qoSIntraMeasurementPingRepository.findByMeasurement(measurement);
		if (pingMeasurementOptional.isEmpty()) {

			pingMeasurement = createPingMeasurement(measurement, responseList, aroundNow);
			qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);

		}else {
			pingMeasurement = pingMeasurementOptional.get();

			pingMeasurement.setMeasurement(measurement);
			pingMeasurement.setAvailable(available);
			pingMeasurement.setMaxResponseTime(maxResponseTime);
			pingMeasurement.setMinResponseTime(minResponseTime);
			pingMeasurement.setMeanResponseTimeWithoutTimeout(meanResponseTimeWithOutTimeout);
			pingMeasurement.setMeanResponseTimeWithTimeout(meanResponseTimeWithTimeout);
			pingMeasurement.setJitterWithoutTimeout(jitterWithoutTimeout);
			pingMeasurement.setJitterWithTimeout(jitterWithTimeout);
			pingMeasurement.setLostPerMeasurementPercent(lostPerMeasurementPercent);
			pingMeasurement.setCountStartedAt(countStartedAt);
			pingMeasurement.setLastAccessAt(calculateLastAccessAt(responseList, pingMeasurement, aroundNow));
			pingMeasurement.setSent(pingMeasurement.getSent() + sentInThisPing);
			pingMeasurement.setSentAll(pingMeasurement.getSentAll() + sentInThisPing);
			pingMeasurement.setReceived(pingMeasurement.getReceived() + receivedInThisPing);
			pingMeasurement.setReceivedAll(pingMeasurement.getReceivedAll() + getReceivedInThisPing(responseList));

			qoSIntraMeasurementPingRepository.saveAndFlush(pingMeasurement);

		}

		 updateMeasurement(aroundNow, measurement);
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

			for (int count = 0; count < TIMES_TO_REPEAT; count ++) {
				final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
				final String formattedResponse = IcmpPingUtil.formatResponse (response);
				logger.info(formattedResponse);

				responseList.add(response);
				Thread.sleep (REST_BETWEEN_PINGS_MILLSEC);
			}
		} catch ( final InterruptedException | IllegalArgumentException ex) {
			logger.debug("" + ex.getMessage());
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private int getReceivedInThisPing(final List<IcmpPingResponse> responseList) {
		logger.debug("getReceivedInThisPing started...");

		int countReceived = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			if (icmpPingResponse.getSuccessFlag()) {

				++ countReceived;
			}
		}
		return countReceived;
	}

	//-------------------------------------------------------------------------------------------------
	private int getSentInThisPing(final List<IcmpPingResponse> responseList) {
		logger.debug("getSentInThisPing started...");

		return responseList.size();
	}

	//-------------------------------------------------------------------------------------------------
	private ZonedDateTime calculateLastAccessAt(final List<IcmpPingResponse> responseList, final QoSIntraPingMeasurement pingMeasurement, final ZonedDateTime aroundNow) {
		logger.debug("calculateLastAccessAt started...");

		ZonedDateTime accessedAt = null;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			if (icmpPingResponse.getSuccessFlag()) {
				accessedAt = aroundNow;
				break;
			}
		}

		if (accessedAt == null) {
			if (pingMeasurement.getLastAccessAt() == null) {
				accessedAt = null;
			}else {
				accessedAt = pingMeasurement.getLastAccessAt();
			}
		}

		return accessedAt;
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateMeanResponseTimeWithoutTimeout(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateMeanResponseTime started...");

		final double mean;
		long sum = 0;
		int count = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {
			if (icmpPingResponse.getSuccessFlag()) {
				sum += icmpPingResponse.getDuration();
				++count;
			}
		}
 
		mean = (double)sum / (double)count;
		return (int) Math.round(mean);
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateMeanResponseTimeWithTimeout(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateMeanResponseTime started...");

		final double mean;
		long sum = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			if (!icmpPingResponse.getSuccessFlag()) {
				sum += PING_TIME_OUT;
			}else {
				sum += icmpPingResponse.getDuration();
			}
		}
 
		mean = (double)sum / (double)responseList.size();
		return (int) Math.round(mean);
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateMinResponseTime(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateMinResponseTime started...");

		int min = PING_TIME_OUT;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			if (icmpPingResponse.getSuccessFlag()) {
				if(icmpPingResponse.getDuration() < min) {
					min = (int) icmpPingResponse.getDuration();
				}
			}
		}
 
		return min;
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateMaxResponseTime(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateMaxResponseTime started...");

		int max = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			if (icmpPingResponse.getSuccessFlag()) {
				if(icmpPingResponse.getDuration() > max) {
					max = (int) icmpPingResponse.getDuration();
				}
			}
		}

		return max;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean calculateAvailable(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateAvailable started...");

		for (final IcmpPingResponse icmpPingResponse : responseList) {

			if (icmpPingResponse.getSuccessFlag()) {

				return true;
			}
		}

		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateJitterWithTimeout(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateJitterWithTimeout started...");

		final int meanResponseTimeWithTimeout = calculateMeanResponseTimeWithTimeout(responseList);
		int sumOfDiffs = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final int duration;
			if (!icmpPingResponse.getSuccessFlag()) {
				duration = PING_TIME_OUT + 1;
			}else {
				duration = (int) icmpPingResponse.getDuration();
			}
			sumOfDiffs += Math.pow( (duration - meanResponseTimeWithTimeout), 2);

		}

		final int jitter = (int) Math.sqrt(sumOfDiffs / responseList.size());

		return jitter;
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateJitterWithoutTimeout(final List<IcmpPingResponse> responseList) {
		logger.debug("calculateJitterWithoutTimeout started...");

		final int meanResponseTimeWithTimeout = calculateMeanResponseTimeWithoutTimeout(responseList);
		int sumOfDiffs = 0;
		int count = 0;
		for (final IcmpPingResponse icmpPingResponse : responseList) {

			final int duration;
			if (icmpPingResponse.getSuccessFlag()) {
				duration = (int) icmpPingResponse.getDuration();
				sumOfDiffs += Math.pow( (duration - meanResponseTimeWithTimeout), 2);

				++ count;
			}

		}

		final int jitter = (int) Math.sqrt(sumOfDiffs / count);

		return jitter;
	}
}