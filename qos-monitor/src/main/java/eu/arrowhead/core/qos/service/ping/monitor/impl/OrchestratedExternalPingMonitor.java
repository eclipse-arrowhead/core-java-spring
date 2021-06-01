package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

	private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

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

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {
		logger.debug("ping statred...");

		if (cachedPingMonitorProvider != null){

			logger.info("starting external ping Monitoring service measurement: " + cachedPingMonitorProvider.getProvider().toString());

		}else {
			try {
				initPingMonitorProvider();
			} catch (final Exception ex) {

				logger.warn("Unsuccesfull ping measurement: because of >> Unsuccesfull external ping Monitor provider orchestration!");

				return null;
			}
		}

		final int timeOut = calculateTimeOut();
		final UUID measurementProcessId = requestExternalMeasurement(address);

		final long startTime = System.currentTimeMillis();
		final long meausermentExpiryTime = startTime + timeOut;

		try {

			return processor.processEvents(measurementProcessId, meausermentExpiryTime);

		} catch (final Exception ex) {

			logger.debug(ex.getMessage());
		}

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public void init() {
		logger.debug("initPingMonitorProvider started...");

		try {

			final OrchestrationFormRequestDTO request = orchestrationRequestFactory.createExternalMonitorOrchestrationRequest();
			final OrchestrationResponseDTO result = driver.queryOrchestrator(request);

			cachedPingMonitorProvider = selectProvider(result);

		} catch (final Exception ex) {
			logger.warn("Exception in external ping monitor orchestration: " + ex);

			cachedPingMonitorProvider = null;

		}

		threadPool.execute(eventCollector);

		driver.unsubscribeFromPingMonitorEvents();
		driver.subscribeToExternalPingMonitorEvents(getPingMonitorSystemRequestDTO());

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private UUID requestExternalMeasurement(final String address) {
		logger.debug("requestExternalMeasurement started...");

			try {
				final IcmpPingRequestACK acknowledgedMeasurmentRequest = driver.requestExternalPingMonitorService(getPingMonitorProvidersServiceUri(), createIcmpPingRequest(address));
				validateAcknowledgedMeasurmentRequest(acknowledgedMeasurmentRequest);

				final UUID startedExternalMeasurementProcessId = acknowledgedMeasurmentRequest.getExternalMeasurementUuid();
				logger.info("IcmpPingRequestACK received, with process id: " + startedExternalMeasurementProcessId);

				return startedExternalMeasurementProcessId;

			} catch (final Exception ex) {
				logger.info(ex);

				throw new ArrowheadException("External Ping Monitor is not available at: " + address );
			}

	}

	//-------------------------------------------------------------------------------------------------
	private void validateAcknowledgedMeasurmentRequest(final IcmpPingRequestACK acknowledgedMeasurmentRequest) {
		logger.debug("validateAcknowledgedMeasurmentRequest started...");

		try {
			Assert.notNull(acknowledgedMeasurmentRequest, "IcmpPingRequestACK is null");
			Assert.notNull(acknowledgedMeasurmentRequest.getAckOk(), "IcmpPingRequestACK.ackOk is null");
			Assert.isTrue(acknowledgedMeasurmentRequest.getAckOk().equalsIgnoreCase("OK"), "IcmpPingRequestACK is null");
		
			
		} catch (final Exception ex) {
			logger.warn("External pingMonitorProvider replied invalid ack : " + ex);

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
		case TOKEN:
			throw new InvalidParameterException("TOKEN security is not supported yet, for PingMonitor Providers. ");
		default:
			throw new InvalidParameterException("Not supported ServiceSecurityType: " + securityType);
		}

	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createHttpUri(final SystemResponseDTO provider, final String path) {

		return Utilities.createURI(CommonConstants.HTTP, provider.getAddress(), provider.getPort(), path );
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents createHttpsUri(final SystemResponseDTO provider, final String path) {

		return Utilities.createURI(CommonConstants.HTTPS, provider.getAddress(), provider.getPort(), path );
	}

	//-------------------------------------------------------------------------------------------------
	private int calculateTimeOut() {

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
		final OrchestrationResponseDTO result = driver.queryOrchestrator(request);

		cachedPingMonitorProvider = selectProvider(result);

		driver.unsubscribeFromPingMonitorEvents();
		driver.subscribeToExternalPingMonitorEvents(getPingMonitorSystemRequestDTO());

	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResultDTO selectProvider(final OrchestrationResponseDTO result) {
		logger.debug("selectProvider started...");

		//TODO implement more sophisticated provider selection strategy
		return result.getResponse().get(0);
	}

}
