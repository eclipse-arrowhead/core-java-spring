package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;
import java.util.UUID;

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
import eu.arrowhead.core.qos.dto.IcmpPingRequest;
import eu.arrowhead.core.qos.dto.IcmpPingRequestACK;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
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

	protected Logger logger = LogManager.getLogger(ExternalPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {

		if (cachedPingMonitorProvider != null){

			final int timeOut = calculateTimeOut();
			final UUID measurementProcessId = requestExternalMeasurement();

			final long startTime = System.currentTimeMillis();
			final long meausermentExpiryTime = startTime + timeOut;

			boolean measurmentRequestConfirmed = false;
			boolean measurmentStartedConfirmed = false;

			while(System.currentTimeMillis() < meausermentExpiryTime) {

				checkInterupts(measurementProcessId);

				if(!measurmentRequestConfirmed) {
					if(!checkMeasurmentRequestConfirmed(measurementProcessId)) {
						break;
					}
				}

				if(!measurmentStartedConfirmed) {
					if(!checkMeasurmentStartedConfirmed(measurementProcessId)) {
						break;
					}
				}

				//TODO implement measurment finish check and handling ...
			}
			//TODO request pingMonitoringService from external provider
			// TODO send request with pingMeasurementProperties, expect http 200 "ok - measurement uuid" as answer

			//TODO calculate timeOut 
			// 
			
			// in while loop listen on eventQueue for event sequence: 
			// 1.) mesaurmentRequestReceived payload = measurement uuid,
			// 2.) mesaurmentStarted payload = measurement uuid,
			// 3.) mesurementFinished payload = measurment uuid, List<IcmpPingResponse>
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
	private boolean checkMeasurmentStartedConfirmed(final UUID measurementProcessId) {
		// TODO Implement method logic
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean checkMeasurmentRequestConfirmed(final UUID measurementProcessId) {
		// TODO Implement method logic
		return false;
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
