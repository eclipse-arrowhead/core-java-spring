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
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingRequest;
import eu.arrowhead.core.qos.dto.IcmpPingRequestACK;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.event.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.core.qos.dto.event.StartedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.event.queue.FinishedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.InteruptedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.ReceivedMonitoringRequestEventQueue;
import eu.arrowhead.core.qos.service.event.queue.StartedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;

public class ExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	private static final int ICMP_TTL = 255;
	private static final int OVERHEAD_MULTIPLIER = 2;

	private final OrchestrationResultDTO cachedPingMonitorProvider = null;

	@Autowired
	private QoSMonitorDriver driver;

	@Autowired
	protected SSLProperties sslProperties;

	@Resource
	private ReceivedMonitoringRequestEventQueue receivedMonitoringRequestEventQueue;

	@Resource
	private StartedMonitoringMeasurementEventQueue startedMonitoringMeasurementEventQueue;

	@Resource
	private FinishedMonitoringMeasurementEventQueue finishedMonitoringMeasurementEventQueue;

	@Resource
	private InteruptedMonitoringMeasurementEventQueue interuptedMonitoringMeasurementEventQueue;

	protected Logger logger = LogManager.getLogger(ExternalPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {
		logger.debug("ping statred...");

		if (cachedPingMonitorProvider != null){

			final int timeOut = calculateTimeOut();
			final UUID measurementProcessId = requestExternalMeasurement();

			final long startTime = System.currentTimeMillis();
			final long meausermentExpiryTime = startTime + timeOut;

			boolean measurmentRequestConfirmed = false;
			boolean measurmentStartedConfirmed = false;

			ReceivedMonitoringRequestEventDTO receivedMonitoringRequestEventDTO;
			StartedMonitoringMeasurementEventDTO startedMonitoringMeasurementEventDTO;

			while(System.currentTimeMillis() < meausermentExpiryTime) {

				checkInterupts(measurementProcessId);

				if(!measurmentRequestConfirmed) {
					receivedMonitoringRequestEventDTO = checkMeasurmentRequestConfirmed(measurementProcessId);
					if( receivedMonitoringRequestEventDTO != null) {
						measurmentRequestConfirmed = true;
					}else {
						break;
					}
				}

				if(!measurmentStartedConfirmed) {
					startedMonitoringMeasurementEventDTO = checkMeasurmentStartedConfirmed(measurementProcessId);
					if(startedMonitoringMeasurementEventDTO != null) {
						measurmentStartedConfirmed = true;
					}else {
						break;
					}
				}

				final FinishedMonitoringMeasurementEventDTO measurmentResult = tryToGetMeasurementResult(measurementProcessId);
				if(measurmentResult != null) {
					logger.info("External Ping Measurement finished: " + measurementProcessId);

					return measurmentResult.getPayload();
				}

			}
			// --- feature TODO create pingMonitorProvider profile table, base on the events above
		}else {
			try {
				initPingMonitorProvider();
			} catch (final Exception ex) {
				// TODO: handle exception
			}
		}

		return null;
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private FinishedMonitoringMeasurementEventDTO tryToGetMeasurementResult(final UUID measurementProcessId) {
		logger.debug("tryToGetMeasurementResult statred...");

		try {

			final FinishedMonitoringMeasurementEventDTO event = finishedMonitoringMeasurementEventQueue.poll();
			if(event != null) {
				final UUID uuid = UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY));
				if(uuid.equals(measurementProcessId)) {
					return event;
				}else {
					throw new ArrowheadException("Invalid measurementProcessId. ");
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
	private StartedMonitoringMeasurementEventDTO checkMeasurmentStartedConfirmed(final UUID measurementProcessId) {
		// TODO Implement method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private ReceivedMonitoringRequestEventDTO  checkMeasurmentRequestConfirmed(final UUID measurementProcessId) {
		// TODO Implement method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private void checkInterupts(final UUID measurementProcessId) {
		/// TODO Implement method logic
		
	}

	//-------------------------------------------------------------------------------------------------
	private UUID requestExternalMeasurement() {
		logger.debug("requestExternalMeasurement started...");

		UUID startedExternalMeasurementProcessId = null;
		do {

			try {
				final IcmpPingRequestACK acknowledgedMeasurmentRequest = driver.requestExternalPingMonitorService(getPingMonitorProvidersServiceUri(), createIcmpPingRequest());
				validateAcknowledgedMeasurmentRequest(acknowledgedMeasurmentRequest);

				// TODO persist ack event
				startedExternalMeasurementProcessId = acknowledgedMeasurmentRequest.getExternalMeasurementUuid();

			} catch (final ArrowheadException ex) {
				logger.info(ex);
			}

		}while(startedExternalMeasurementProcessId != null);

		return startedExternalMeasurementProcessId;
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
	private IcmpPingRequest createIcmpPingRequest() {
		logger.debug("createIcmpPingRequest started...");

		final IcmpPingRequest request = new IcmpPingRequest();
		request.setHost(cachedPingMonitorProvider.getProvider().getAddress());
		request.setPacketSize(pingMeasurementProperties.getPacketSize());
		request.setTimeout(pingMeasurementProperties.getTimeout());
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
	private void initPingMonitorProvider() {
		logger.debug("initPingMonitorProvider started...");

		
		//TODO try sr with timeout, trz orch with time out -> exit if uncussess
		//orch service pingprovider with time - finish int if unscucesss, 
		//in orhcestrator flags
		// metadatasearch true
		//matchmaking true
		//
		// matadatareq.. fill with shema
		// enableIntercloud false

		//if service orchestrated subscribe to all events from pingmonitor
		// as monitoringRequestReceivedEvent, monitoringStratedEvent, monitoringFinishedEvent, interuptedMonitoringProcessEvent 
	}
}
