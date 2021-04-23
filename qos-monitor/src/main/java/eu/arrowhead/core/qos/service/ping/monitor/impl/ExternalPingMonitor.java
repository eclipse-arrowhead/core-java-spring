package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;

public class ExternalPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	private final OrchestrationResponseDTO cachedPingMonitorProvider = null;

	@Autowired
	private QoSMonitorDriver driver;

	protected Logger logger = LogManager.getLogger(ExternalPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(final String address) {
		
		if (cachedPingMonitorProvider != null && providerIsAlive()){

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
	private boolean providerIsAlive() {
		logger.debug("providerIsAlive started...");
		try {
			final UriComponents providerEchoUri = getProviderEchoUri();
			//TODO implement driver.echoProvider()
		} catch (final ArrowheadException ex) {
			logger.debug("providerIsAlive thow: " + ex);

			return false;
		}
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getProviderEchoUri() {
		logger.debug("getProviderEchoUri started...");

		
		Utilities.createURI(null, null, 0, null);
		return null;
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

	}
}
