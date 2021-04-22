package eu.arrowhead.core.qos.service.ping.monitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.IcmpPingRequest;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.ping.monitor.PingMonitorManager;

public class DummyPingProvider implements PingMonitorManager{

	//=================================================================================================
	// members
	
	//-------------------------------------------------------------------------------------------------
	
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String DUMMY_PING_PROVIDER_ERROR_MESSAGE = "This is the dummy ping provider's normal ping response";

	@Autowired
	private PingMeasurementProperties pingMeasurementProperties;

	protected Logger logger = LogManager.getLogger(DummyPingProvider.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<IcmpPingResponse> ping(String address) {

		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		final List<IcmpPingResponse> responseList = new ArrayList<>(pingMeasurementProperties.getTimeToRepeat());
		try {
			final IcmpPingRequest request = new IcmpPingRequest();
			request.setHost(address);
			request.setTimeout(pingMeasurementProperties.getTimeout());
			request.setPacketSize(pingMeasurementProperties.getPacketSize());

			for (int count = 0; count < pingMeasurementProperties.getTimeToRepeat(); count ++) {
				IcmpPingResponse response;
				try {
					//TODO REMOVE IT or RETURN 0 or SET DEFAULT DUMMY VALUE and  ERROR and SUCCESS TRUE
					if(1>0) {
						throw new Exception(DUMMY_PING_PROVIDER_ERROR_MESSAGE);
					}

				} catch (final Exception ex) {
					response = new IcmpPingResponse();
					response.setErrorMessage(ex.getMessage());
					response.setSuccessFlag(false);
					response.setThrowable(ex);

					responseList.add(response);
				}

				Thread.sleep(pingMeasurementProperties.getRest());
			}
		} catch (final InterruptedException | IllegalArgumentException ex) {
			logger.debug("" + ex.getMessage());
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
}
