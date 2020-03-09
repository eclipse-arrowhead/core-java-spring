package eu.arrowhead.core.qos.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResultListDTO;

@Service
public class RelayEchoService {
	
	//=================================================================================================
	// members
	
	private Logger logger = LogManager.getLogger(RelayEchoService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementResultListDTO calculateInterRelayEchoMeasurements(final CloudSystemFormDTO request) {
		//TODO
		return null;
	}
}
