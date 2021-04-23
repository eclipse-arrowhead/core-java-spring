package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;

public class ExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
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
			//TODO request pingMonitoringService from external provider
			// TODO send request with pingMeasurementProperties, expect http 200 "ok - measurement uuid" as answer
			// listen on eventQueue for event sequence: 
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
	private void initPingMonitorProvider() {
		logger.debug("initPingMonitorProvider started...");

		
		//TODO try sr with timeout, trz orch with time out -> exit if uncussess
		//orch service pingprovider with time - finish int if uncucesss, 
		//in orhcestrator flags
		// metadatasearch true
		//matchmaking true
		//
		// matadatareq.. fill with shema
		// enableIntercloud false

		//if service orchestrated subscribe to all events from pingmonitor
		// as monitoringRequestReceivedEvent, monitoringStratedEvent, monitoringFinishedEvent, serviceErrorEvent 
	}
}
