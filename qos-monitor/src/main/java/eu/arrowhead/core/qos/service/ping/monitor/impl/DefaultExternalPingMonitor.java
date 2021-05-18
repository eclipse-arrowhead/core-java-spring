package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingRequest;
import eu.arrowhead.core.qos.dto.IcmpPingRequestACK;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.event.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.InterruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.core.qos.dto.event.StartedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.event.queue.FinishedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.InteruptedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.ReceivedMonitoringRequestEventQueue;
import eu.arrowhead.core.qos.service.event.queue.StartedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;

public class DefaultExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	private static final int ICMP_TTL = 255;
	private static final int OVERHEAD_MULTIPLIER = 2;

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

	private Logger logger = LogManager.getLogger(DefaultExternalPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DefaultExternalPingMonitor() {

		initPingMonitorProvider();
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {
		logger.debug("ping statred...");

		final int timeOut = calculateTimeOut();
		final UUID measurementProcessId = requestExternalMeasurement(address);

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

					logger.info("EVENT: External Ping Measurement request confirmed : " + measurementProcessId);

				}else {
					//TODO rest & continue;
				}
			}

			if(!measurmentStartedConfirmed) {
				startedMonitoringMeasurementEventDTO = checkMeasurmentStartedConfirmed(measurementProcessId);
				if(startedMonitoringMeasurementEventDTO != null) {
					measurmentStartedConfirmed = true;

					logger.info("EVENT: External Ping Measurement started : " + measurementProcessId);

				}else {
					//TODO rest & continue;
				}
			}

			final FinishedMonitoringMeasurementEventDTO measurmentResult = tryToGetMeasurementResult(measurementProcessId);
			if(measurmentResult != null) {
				logger.info("EVENT: External Ping Measurement finished: " + measurementProcessId);

				return measurmentResult.getPayload();
			}else {
				//TODO rest & continue;
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
		logger.debug("checkMeasurmentStartedConfirmed statred...");

		try {

			final StartedMonitoringMeasurementEventDTO event = startedMonitoringMeasurementEventQueue.poll();
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

		} catch (final InterruptedException ex) {

			throw new ArrowheadException("Exeption during startedMonitoringMeasurementEventQueue poll : " + ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private ReceivedMonitoringRequestEventDTO  checkMeasurmentRequestConfirmed(final UUID measurementProcessId) {
		logger.debug("checkMeasurmentRequestConfirmed statred...");

		try {

			final ReceivedMonitoringRequestEventDTO event = receivedMonitoringRequestEventQueue.poll();
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

		} catch (final InterruptedException ex) {

			throw new ArrowheadException("Exeption during receivedMonitoringRequestEventQueue poll : " + ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkInterupts(final UUID measurementProcessId) {
		logger.debug("checkInterupts statred...");

		try {

			final InterruptedMonitoringMeasurementEventDTO event = interuptedMonitoringMeasurementEventQueue.poll();
			if(event != null) {
				final UUID uuid = UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY));
				if(uuid.equals(measurementProcessId)) {

					logger.info("EVENT: External Ping Measurement interupted : " + measurementProcessId);

					final String suspectedRootCauses = event.getMetaData().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_ROOT_CAUSE_KEY);
					final String exeptionInExternalMonitoring = event.getMetaData().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_EXCEPTION_KEY);

					logger.warn("Exception in external monitoring process: " + exeptionInExternalMonitoring);
					logger.warn("Self clamed root cause of external monitoring process exception: " + suspectedRootCauses);

					throw new ArrowheadException("Interupt in external monitoring process. ");
				}else {
					return;
				}
			}else {
				return;
			}

		} catch (final InterruptedException ex) {

			throw new ArrowheadException("Exeption during interuptedMonitoringMeasurementEventQueue poll : " + ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private UUID requestExternalMeasurement(final String address) {
		logger.debug("requestExternalMeasurement started...");

		try {
			final IcmpPingRequestACK acknowledgedMeasurmentRequest = driver.requestExternalPingMonitorService(createIcmpPingRequest(address));
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
	private IcmpPingRequest createIcmpPingRequest(final String address) {
		logger.debug("createIcmpPingRequest started...");

		final IcmpPingRequest request = new IcmpPingRequest();
		request.setHost(address);
		request.setPacketSize(pingMeasurementProperties.getPacketSize());
		request.setTimeout(pingMeasurementProperties.getTimeout());
		request.setTimeToRepeat(pingMeasurementProperties.getTimeToRepeat());
		request.setTtl(ICMP_TTL);

		return request;
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

		driver.checkFixedPingMonitorProviderEchoUri();
		driver.subscribeToExternalPingMonitorEvents();
	}
}
