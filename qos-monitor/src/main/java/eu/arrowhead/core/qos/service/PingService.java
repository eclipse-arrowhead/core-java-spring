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
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

@Service
public class PingService {

	//=================================================================================================
	// members

	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";

	@Autowired
	private PingMeasurementProperties pingMeasurementProperties;
	
	@Autowired
	private QoSDBService qosDBService;

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
	
	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementResponseDTO getMedianIntraPingMeasurement(final QoSMeasurementAttribute attribute) {
		logger.debug("getMedianIntraPingMeasurement started...");
		
		if (attribute == null) {
			throw new InvalidParameterException("QoSMeasurementAttribute is null");
		}
		
		final List<QoSIntraPingMeasurementResponseDTO> data = qosDBService.getIntraPingMeasurementResponse(-1, -1, null, null).getData();
		switch (attribute) {
		case MIN_RESPONSE_TIME:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getMinResponseTime() - m2.getMinResponseTime());
			break;
		case MAX_RESPONSE_TIME:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getMaxResponseTime() - m2.getMaxResponseTime());
			break;
		case MEAN_RESPONSE_TIME_WITH_TIMEOUT:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getMeanResponseTimeWithTimeout() - m2.getMeanResponseTimeWithTimeout());
			break;
		case MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getMeanResponseTimeWithoutTimeout() - m2.getMeanResponseTimeWithoutTimeout());
			break;
		case JITTER_WITH_TIMEOUT:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getJitterWithTimeout() - m2.getJitterWithTimeout());
			break;
		case JITTER_WITHOUT_TIMEOUT:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getJitterWithoutTimeout() - m2.getJitterWithoutTimeout());
			break;
		case LOST_PER_MEASUREMENT_PERCENT:
			data.sort((final QoSIntraPingMeasurementResponseDTO m1, final QoSIntraPingMeasurementResponseDTO m2) -> m1.getLostPerMeasurementPercent() - m2.getLostPerMeasurementPercent());
			break;
		}
		
		return data.get(data.size() / 2);
	}
}