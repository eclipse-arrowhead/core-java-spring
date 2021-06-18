package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.service.ping.monitor.AbstractPingMonitor;

public class DummyPingMonitor extends AbstractPingMonitor{

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------

	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String DUMMY_PING_PROVIDER_ERROR_MESSAGE = "This is the dummy ping provider's normal ping response";

	private Logger logger = LogManager.getLogger(DummyPingMonitor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(String address) {

		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		 final int timeToRepeat = pingMeasurementProperties.getTimeToRepeat();

		final List<IcmpPingResponse> responseList = new ArrayList<>(timeToRepeat);
		for (int count = 0; count < timeToRepeat; count ++) {
			final IcmpPingResponse response;
			response = new IcmpPingResponse();
			response.setErrorMessage(DUMMY_PING_PROVIDER_ERROR_MESSAGE);
			response.setSuccessFlag(false);
			response.setThrowable(new ArrowheadException(DUMMY_PING_PROVIDER_ERROR_MESSAGE).toString());

			responseList.add(response);
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	public void init() {
		logger.debug("initPingMonitorProvider started...");

	}
}
