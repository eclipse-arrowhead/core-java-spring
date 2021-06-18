package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventCollectorTask;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventProcessor;

public class DefaultExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	private static final int ICMP_TTL = 255;
	private static final int OVERHEAD_MULTIPLIER = 2;
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String PING_EVENT_COLLECTOR_THREAD_NAME = "Ping-Event-Collector-Thread";

	@Autowired
	private QoSMonitorDriver driver;

	@Autowired
	private PingEventProcessor processor;

	@Resource(name = QosMonitorConstants.EVENT_COLLECTOR)
	private PingEventCollectorTask eventCollector; 

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_NAME_WD)
	private String externalPingMonitorName;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_ADDRESS_WD)
	private String externalPingMonitorAddress;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_PORT_WD)
	private int externalPingMonitorPort;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_PATH_WD)
	private String externalPingMonitorPath;

	@Value(CoreCommonConstants.$QOS_MONITOR_PROVIDER_SECURE_WD)
	private boolean pingMonitorSecure;

	private SystemRequestDTO pingMonitorSystem;

	private boolean initialized;

	private final Logger logger = LogManager.getLogger(DefaultExternalPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {
		logger.debug("ping statred...");

		if(!initialized) {

			throw new ArrowheadException("DefaultExternalPingMonitor is not initialized.");
		}

		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		final int timeOut = calculateTimeOut();
		final UUID measurementProcessId = requestExternalMeasurement(address);

		final long startTime = System.currentTimeMillis();
		final long measurementExpiryTime = startTime + timeOut;

		try {

			return processor.processEvents(measurementProcessId, measurementExpiryTime);

		} catch (final Exception ex) {

			logger.debug(ex.getMessage());
		}

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public void init() {
		logger.debug("initPingMonitorProvider started...");

		if (initialized) {
			logger.debug("DefaultExternalPingMonitor is already initialized.");

			return;
		}

		pingMonitorSystem = getPingMonitorSystemRequestDTO();

		driver.checkPingMonitorProviderEchoUri(createPingMonitorProviderEchoUri());
		driver.subscribeToExternalPingMonitorEvents(pingMonitorSystem);

		final Thread eventCollectorThread = new Thread(eventCollector);
		eventCollectorThread.setName(PING_EVENT_COLLECTOR_THREAD_NAME);
		eventCollectorThread.start();

		initialized = true;

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private UUID requestExternalMeasurement(final String address) {
		logger.debug("requestExternalMeasurement started...");

		try {

			final IcmpPingRequestACK acknowledgedMeasurementRequest = driver.requestExternalPingMonitorService(createPingMonitorProviderUri(), createIcmpPingRequest(address));
			validateAcknowledgedMeasurementRequest(acknowledgedMeasurementRequest);

			final UUID startedExternalMeasurementProcessId = acknowledgedMeasurementRequest.getExternalMeasurementUuid();
			if (startedExternalMeasurementProcessId == null) {
				throw new ArrowheadException("External Ping Monitor returned ack without processId.");
			}
			logger.debug("IcmpPingRequestACK received, with process id: " + startedExternalMeasurementProcessId);

			return startedExternalMeasurementProcessId;

		} catch (final ArrowheadException ex) {
			logger.debug(ex);

			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex);

			throw new ArrowheadException("External Ping Monitor is not available at: " + address );
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void validateAcknowledgedMeasurementRequest(final IcmpPingRequestACK acknowledgedMeasurementRequest) {
		logger.debug("validateAcknowledgedMeasurementRequest started...");

		try {
			Assert.notNull(acknowledgedMeasurementRequest, "IcmpPingRequestACK is null");
			Assert.notNull(acknowledgedMeasurementRequest.getAckOk(), "IcmpPingRequestACK.ackOk is null");
			Assert.isTrue(acknowledgedMeasurementRequest.getAckOk().equalsIgnoreCase("OK"), "IcmpPingRequestACK is not valid");

		} catch (final Exception ex) {
			logger.debug("External pingMonitorProvider replied invalid ack : " + ex);

			throw new ArrowheadException("External pingMonitorProvider replied invalid ack", ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private IcmpPingRequestDTO createIcmpPingRequest(final String address) {
		logger.debug("createIcmpPingRequest started...");

		final IcmpPingRequestDTO request = new IcmpPingRequestDTO();
		request.setHost(address);
		request.setPacketSize(pingMeasurementProperties.getPacketSize());
		request.setTimeout(Long.valueOf(pingMeasurementProperties.getTimeout()));
		request.setTimeToRepeat(pingMeasurementProperties.getTimeToRepeat());
		request.setTtl(ICMP_TTL);

		return request;
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateTimeOut() {
		logger.debug("calculateTimeOut started...");

		final int singlePingTimeOut = pingMeasurementProperties.getTimeout();
		final int timesToRepeatPing = pingMeasurementProperties.getTimeToRepeat();

		return singlePingTimeOut * timesToRepeatPing * OVERHEAD_MULTIPLIER;
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createPingMonitorProviderUri() {
		logger.debug("createPingMonitorProviderUri started...");

		return Utilities.createURI(pingMonitorSecure ? CommonConstants.HTTPS : CommonConstants.HTTP, externalPingMonitorAddress, externalPingMonitorPort, externalPingMonitorPath);
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createPingMonitorProviderEchoUri() {
		logger.debug("createPingMonitorProviderEchoUri started...");

		return Utilities.createURI(pingMonitorSecure ? CommonConstants.HTTPS : CommonConstants.HTTP, externalPingMonitorAddress, externalPingMonitorPort, CommonConstants.ECHO_URI);
	}

	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getPingMonitorSystemRequestDTO() {
		logger.debug("getPingMonitorSystemRequestDTO started...");

		if (pingMonitorSystem == null) {
			pingMonitorSystem = new SystemRequestDTO();
			pingMonitorSystem.setSystemName(externalPingMonitorName);
			pingMonitorSystem.setAddress(externalPingMonitorAddress);
			pingMonitorSystem.setPort(externalPingMonitorPort);
			pingMonitorSystem.setMetadata(null);
		}

		return pingMonitorSystem;
	}

}
