package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.IcmpPingRequestDTO;
import eu.arrowhead.common.dto.shared.InterruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.StartedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingDTOConverter;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.externalMonitor.ExternalMonitorOrchestrationRequestFactory;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;

public class OrchestratedExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	private static final int ICMP_TTL = 255;
	private static final int OVERHEAD_MULTIPLIER = 2;
	private static final long SLEEP_PERIOD = TimeUnit.SECONDS.toMillis(1);

	private OrchestrationResultDTO cachedPingMonitorProvider = null;

	@Autowired
	private QoSMonitorDriver driver;

	@Autowired
	protected SSLProperties sslProperties;

	@Resource(name = QosMonitorConstants.RECEIVED_MONITORING_REQUEST_QUEUE)
	private LinkedBlockingQueue<ReceivedMonitoringRequestEventDTO> receivedMonitoringRequestEventQueue;

	@Resource(name = QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_QUEUE)
	private LinkedBlockingQueue<StartedMonitoringMeasurementEventDTO> startedMonitoringMeasurementEventQueue;

	@Resource(name = QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_QUEUE)
	private LinkedBlockingQueue<FinishedMonitoringMeasurementEventDTO> finishedMonitoringMeasurementEventQueue;

	@Resource(name = QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_QUEUE)
	private LinkedBlockingQueue<InterruptedMonitoringMeasurementEventDTO> interuptedMonitoringMeasurementEventQueue;

	@Autowired
	private ExternalMonitorOrchestrationRequestFactory orchestrationRequestFactory;

	private final Logger logger = LogManager.getLogger(OrchestratedExternalPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestratedExternalPingMonitor() {

		init();

	}

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

		boolean measurmentRequestConfirmed = false;
		boolean measurmentStartedConfirmed = false;

		ReceivedMonitoringRequestEventDTO receivedMonitoringRequestEventDTO;
		StartedMonitoringMeasurementEventDTO startedMonitoringMeasurementEventDTO;

		//TODO externalize block to class
		while(System.currentTimeMillis() < meausermentExpiryTime) {

			checkInterupts(measurementProcessId);

			if(!measurmentRequestConfirmed) {
				receivedMonitoringRequestEventDTO = checkMeasurmentRequestConfirmedEvents(measurementProcessId);
				if( receivedMonitoringRequestEventDTO != null) {
					measurmentRequestConfirmed = true;

					logger.debug("EVENT: External Ping Measurement request confirmed : " + measurementProcessId);

				}else {
					rest();
					continue;
				}
			}

			if(!measurmentStartedConfirmed) {
				startedMonitoringMeasurementEventDTO = checkMeasurmentStartedConfirmedEvents(measurementProcessId);
				if(startedMonitoringMeasurementEventDTO != null) {
					measurmentStartedConfirmed = true;

					logger.info("EVENT: External Ping Measurement started : " + measurementProcessId);

				}else {
					rest();
					continue;
				}
			}

			final FinishedMonitoringMeasurementEventDTO measurmentResult = checkMeasurementResultEvents(measurementProcessId);
			if(measurmentResult != null) {
				logger.info("EVENT: External Ping Measurement finished: " + measurementProcessId);

				clearAllQueues();
				return IcmpPingDTOConverter.convertPingMeasurementResult(measurmentResult.getPayload());
			}else {
				rest();
				continue;
			}

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

		driver.unsubscribeFromPingMonitorEvents();
		driver.subscribeToExternalPingMonitorEvents(getPingMonitorSystemRequestDTO());

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private FinishedMonitoringMeasurementEventDTO checkMeasurementResultEvents(final UUID measurementProcessId) {
		logger.debug("checkMeasurementResultEvents statred...");

		final List<FinishedMonitoringMeasurementEventDTO> uncheckedEvents = new ArrayList<>();
		finishedMonitoringMeasurementEventQueue.drainTo(uncheckedEvents);

		if (uncheckedEvents.isEmpty()) {
			return null;
		}else {
			for (final FinishedMonitoringMeasurementEventDTO event : uncheckedEvents) {

				final FinishedMonitoringMeasurementEventDTO validEvent = checkMeasurementResult(measurementProcessId, event);

				if (validEvent != null) {
					return validEvent;
				}
			}
		}

		return null;

	}

	//-------------------------------------------------------------------------------------------------
	private FinishedMonitoringMeasurementEventDTO checkMeasurementResult(final UUID measurementProcessId, final FinishedMonitoringMeasurementEventDTO event) {
		logger.debug("checkMeasurementResult statred...");

		try {

			if(event != null) {
				final UUID uuid = UUID.fromString(event.getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY));
				if(uuid.equals(measurementProcessId)) {
					return event;
				}else {
					logger.debug("Invalid measurementProcessId : " + event.toString());

					return null;
				}
			}else {
				return null;
			}

		} catch (final ArrowheadException ex) {

			throw new InvalidParameterException("Invalid finished ping monitoring measurement : " + ex);
		} catch (final Exception ex) {

			throw new ArrowheadException("Exeption during finishedMonitoringMeasurementEventQueue poll : " + ex);
		}

	}

	//-------------------------------------------------------------------------------------------------
	private StartedMonitoringMeasurementEventDTO checkMeasurmentStartedConfirmedEvents(final UUID measurementProcessId) {
		logger.debug("checkMeasurmentStartedConfirmedEvents statred...");

		final List<StartedMonitoringMeasurementEventDTO> uncheckedEvents = new ArrayList<>();
		startedMonitoringMeasurementEventQueue.drainTo(uncheckedEvents);

		if (uncheckedEvents.isEmpty()) {
			return null;
		}else {
			for (final StartedMonitoringMeasurementEventDTO event : uncheckedEvents) {

				final StartedMonitoringMeasurementEventDTO validEvent = checkMeasurmentStartedConfirmed(measurementProcessId, event);

				if (validEvent != null) {
					return validEvent;
				}
			}
		}

		return null;

	}

	//-------------------------------------------------------------------------------------------------
	private StartedMonitoringMeasurementEventDTO checkMeasurmentStartedConfirmed(final UUID measurementProcessId, final StartedMonitoringMeasurementEventDTO event) {
		logger.debug("checkMeasurmentStartedConfirmed statred...");

		if(event != null) {
			final UUID uuid = UUID.fromString(event.getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY));
			if(uuid.equals(measurementProcessId)) {
				return event;
			}else {
				throw new ArrowheadException("Invalid measurementProcessId. ");
			}
		}else {
			return null;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private ReceivedMonitoringRequestEventDTO  checkMeasurmentRequestConfirmedEvents(final UUID measurementProcessId) {
		logger.debug("checkMeasurmentRequestConfirmedEvents statred...");

		final List<ReceivedMonitoringRequestEventDTO> uncheckedEvents = new ArrayList<>();
		receivedMonitoringRequestEventQueue.drainTo(uncheckedEvents);

		if (uncheckedEvents.isEmpty()) {
			return null;
		}else {
			for (final ReceivedMonitoringRequestEventDTO event : uncheckedEvents) {

				final ReceivedMonitoringRequestEventDTO validEvent = checkMeasurmentRequestConfirmed(measurementProcessId, event);

				if (validEvent != null) {
					return validEvent;
				}
			}
		}

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private ReceivedMonitoringRequestEventDTO  checkMeasurmentRequestConfirmed(final UUID measurementProcessId, final ReceivedMonitoringRequestEventDTO event) {
		logger.debug("checkMeasurmentRequestConfirmed statred...");

		if(event != null) {
			final UUID uuid = UUID.fromString(event.getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY));
			if(uuid.equals(measurementProcessId)) {
				return event;
			}else {
				logger.debug("Invalid measurementProcessId: " + event.toString());

				return null;
			}
		}else {
			return null;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkInterupts(final UUID measurementProcessId) {
		logger.debug("checkInterupts statred...");

		final List<InterruptedMonitoringMeasurementEventDTO> uncheckedEvents = new ArrayList<>();
		interuptedMonitoringMeasurementEventQueue.drainTo(uncheckedEvents);

		if (uncheckedEvents.isEmpty()) {
			return;
		}else {

			for (final InterruptedMonitoringMeasurementEventDTO event : uncheckedEvents) {

				checkInteruptEvent(measurementProcessId, event);

			}
		}

	}

	//-------------------------------------------------------------------------------------------------
	private void checkInteruptEvent(final UUID measurementProcessId, final InterruptedMonitoringMeasurementEventDTO event) {
		logger.debug("checkInteruptEvent statred...");

		final List<InterruptedMonitoringMeasurementEventDTO> uncheckedEvents = new ArrayList<>();
		interuptedMonitoringMeasurementEventQueue.drainTo(uncheckedEvents);

		if(event != null) {
			final UUID uuid = UUID.fromString(event.getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY));
			if(uuid.equals(measurementProcessId)) {

				logger.debug("EVENT: External Ping Measurement interupted : " + measurementProcessId);

				final String suspectedRootCauses = event.getMetadata().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_ROOT_CAUSE_KEY);
				final String exeptionInExternalMonitoring = event.getMetadata().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_EXCEPTION_KEY);

				logger.debug("Exception in external monitoring process: " + exeptionInExternalMonitoring);
				logger.debug("Self clamed root cause of external monitoring process exception: " + suspectedRootCauses);

				throw new ArrowheadException("Interupt in external monitoring process. ");
			}else {
				return;
			}
		}else {
			return;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void clearAllQueues() {
		logger.debug("clearAllQueues started...");

		interuptedMonitoringMeasurementEventQueue.clear();
		receivedMonitoringRequestEventQueue.clear();
		startedMonitoringMeasurementEventQueue.clear();
		finishedMonitoringMeasurementEventQueue.clear();

	}

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

	//-------------------------------------------------------------------------------------------------
	private void rest() {
		try {
			Thread.sleep(SLEEP_PERIOD);
		} catch (final InterruptedException e) {
			logger.warn(e.getMessage());
		}
	}
}
