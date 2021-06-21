package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.externalMonitor.ExternalMonitorOrchestrationRequestFactory;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventCollectorTask;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventProcessor;

public class OrchestratedExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	private static final int ICMP_TTL = 255;
	private static final int OVERHEAD_MULTIPLIER = 2;
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String PING_EVENT_COLLECTOR_THREAD_NAME = "Ping-Event-Collector-Thread";

	private OrchestrationResultDTO cachedPingMonitorProvider = null;

	@Autowired
	private QoSMonitorDriver driver;

	@Autowired
	private PingEventProcessor processor;

	@Resource(name = QosMonitorConstants.EVENT_COLLECTOR)
	private PingEventCollectorTask eventCollector; 

	@Autowired
	protected SSLProperties sslProperties;

	@Autowired
	private ExternalMonitorOrchestrationRequestFactory orchestrationRequestFactory;

	private final Logger logger = LogManager.getLogger(OrchestratedExternalPingMonitor.class);

	private boolean initialized;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {
		logger.debug("ping statred...");

		if(!initialized) {

			throw new ArrowheadException("OrchestratedExternalPingMonitor is not initialized.");
		}

		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		if (cachedPingMonitorProvider != null && cachedPingMonitorProvider.getProvider() != null){

			logger.debug("starting external ping Monitoring service measurement: " + cachedPingMonitorProvider.getProvider().toString());

		}else {
			try {
				initPingMonitorProvider();
			} catch (final Exception ex) {

				logger.debug("unsuccessful ping measurement: because of >> unsuccessful external ping Monitor provider orchestration!");

				return null;
			}
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
		logger.debug("init started...");

		if (initialized) {
			logger.debug("OrchestratedExternalPingMonitor is already initialized.");

			return;
		}

		try {

			final OrchestrationFormRequestDTO request = orchestrationRequestFactory.createExternalMonitorOrchestrationRequest();
			final OrchestrationResponseDTO result = driver.queryOrchestrator(request);

			cachedPingMonitorProvider = selectProvider(result);

			driver.unsubscribeFromPingMonitorEvents();
			driver.subscribeToExternalPingMonitorEvents(getPingMonitorSystemRequestDTO());

		} catch (final Exception ex) {
			logger.debug("Exception in external ping monitor orchestration: " + ex);

			cachedPingMonitorProvider = null;

		}

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
				final IcmpPingRequestACK acknowledgedMeasurementRequest = driver.requestExternalPingMonitorService(getPingMonitorProvidersServiceUri(), createIcmpPingRequest(address));
				validateAcknowledgedMeasurementRequest(acknowledgedMeasurementRequest);

				final UUID startedExternalMeasurementProcessId = acknowledgedMeasurementRequest.getExternalMeasurementUuid();
				if (startedExternalMeasurementProcessId == null) {
					throw new ArrowheadException("External Ping Monitor returned ack without processId.");
				}

				logger.debug("IcmpPingRequestACK received, with process id: " + startedExternalMeasurementProcessId);

				return startedExternalMeasurementProcessId;

			} catch (final ArrowheadException ex) {
				logger.debug(ex);

				cachedPingMonitorProvider = null;

				throw ex;
			} catch (final Exception ex) {
				logger.debug(ex);

				cachedPingMonitorProvider = null;
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

			cachedPingMonitorProvider = null;
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
	private UriComponents getPingMonitorProvidersServiceUri() {
		logger.debug("getPingMonitorProvidersServiceUri started...");

		final SystemResponseDTO provider = cachedPingMonitorProvider.getProvider();
		final ServiceSecurityType securityType = cachedPingMonitorProvider.getSecure();
		final String path = cachedPingMonitorProvider.getServiceUri();

		switch (securityType) {
		case NOT_SECURE:
			return createHttpUri(provider, path);
		case CERTIFICATE:
			return createHttpsUri(provider, path);
		default:
			throw new InvalidParameterException("Not supported ServiceSecurityType: " + securityType);
		}

	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createHttpUri(final SystemResponseDTO provider, final String path) {
		logger.debug("createHttpUri started...");

		return Utilities.createURI(CommonConstants.HTTP, provider.getAddress(), provider.getPort(), path );
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createHttpsUri(final SystemResponseDTO provider, final String path) {
		logger.debug("createHttpsUri started...");

		return Utilities.createURI(CommonConstants.HTTPS, provider.getAddress(), provider.getPort(), path );
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateTimeOut() {
		logger.debug("calculateTimeOut started...");

		final int singlePingTimeOut = pingMeasurementProperties.getTimeout();
		final int timesToRepeatPing = pingMeasurementProperties.getTimeToRepeat();

		return singlePingTimeOut * timesToRepeatPing * OVERHEAD_MULTIPLIER;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getPingMonitorSystemRequestDTO() {
		logger.debug("getPingMonitorSystemRequestDTO started...");

		Assert.notNull(cachedPingMonitorProvider.getProvider(), "Cached Provider is null");

		final SystemResponseDTO provider = cachedPingMonitorProvider.getProvider();

		final SystemRequestDTO system = new SystemRequestDTO();
		system.setSystemName(provider.getSystemName());
		system.setAddress(provider.getAddress());
		system.setPort(provider.getPort());
		system.setMetadata(provider.getMetadata());
		system.setAuthenticationInfo(provider.getAuthenticationInfo());

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private void initPingMonitorProvider() {
		logger.debug("initPingMonitorProvider started...");

		final OrchestrationFormRequestDTO request = orchestrationRequestFactory.createExternalMonitorOrchestrationRequest();
		if (request == null) {
			throw new ArrowheadException("Orchestration request form is null");
		}

		final OrchestrationResponseDTO result = driver.queryOrchestrator(request);
		if (result == null || result.getResponse().isEmpty()) {
			throw new ArrowheadException("Orchestration result " + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		cachedPingMonitorProvider = selectProvider(result);

		driver.unsubscribeFromPingMonitorEvents();
		driver.subscribeToExternalPingMonitorEvents(getPingMonitorSystemRequestDTO());

	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResultDTO selectProvider(final OrchestrationResponseDTO result) {
		logger.debug("selectProvider started...");

		if (result == null || result.getResponse() == null || result.getResponse().isEmpty()) {

			throw new ArrowheadException("PingMonitor orchestration result is empty or null.");
		}
		//TODO implement more sophisticated provider selection strategy
		return result.getResponse().get(0);
	}

}
