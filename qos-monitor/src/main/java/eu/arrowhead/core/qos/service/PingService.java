package eu.arrowhead.core.qos.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

@Service
public class PingService {

	//=================================================================================================
	// members

	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";

	@Autowired
	private PingMeasurementProperties pingMeasurementProperties;

	protected Logger logger = LogManager.getLogger(PingMeasurementProperties.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public List<IcmpPingResponse> getPingResponseList(final String address) {
		logger.debug("getPingResponseList started...");

		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}

		final List<IcmpPingResponse> responseList = new ArrayList<>(pingMeasurementProperties.getTimeToRepeat());
		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
			request.setHost(address);
			request.setTimeout(pingMeasurementProperties.getTimeout());
			request.setPacketSize(pingMeasurementProperties.getPacketSize());

			for (int count = 0; count < pingMeasurementProperties.getTimeToRepeat(); count ++) {
				IcmpPingResponse response;
				try {
					response = IcmpPingUtil.executePingRequest (request);
					final String formattedResponse = IcmpPingUtil.formatResponse(response);
					logger.debug(formattedResponse);

					responseList.add(response);
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
}