package eu.arrowhead.core.qos.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

public class IcmpPingDTOConverter {
	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(IcmpPingDTOConverter.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static List<IcmpPingResponse> convertPingMeasurementResult(final List<IcmpPingResponseDTO> payload) {
		logger.debug("convertPingMeasurementResult started...");

		Assert.notNull(payload, "Payload is null");
		Assert.isTrue(!payload.isEmpty(), "Payload is empty");

		final List<IcmpPingResponse> result = new ArrayList<>(payload.size());
		for (final IcmpPingResponseDTO icmpResponseDTO : payload) {

			result.add(convertIcmpResponseDTOToIcmpResponse(icmpResponseDTO));
		}

		return result;
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private static IcmpPingResponse convertIcmpResponseDTOToIcmpResponse(final IcmpPingResponseDTO icmpResponseDTO) {
		logger.debug("convertPingMeasurementResult started...");

		validateIcmpResponseDTO(icmpResponseDTO);

		final IcmpPingResponse icmpResponse = new IcmpPingResponse();
		icmpResponse.setDuration(icmpResponseDTO.getDuration());
		icmpResponse.setErrorMessage(icmpResponseDTO.getErrorMessage());
		icmpResponse.setHost(icmpResponseDTO.getHost());
		icmpResponse.setRtt(icmpResponseDTO.getRtt());
		icmpResponse.setSize(icmpResponseDTO.getSize());
		icmpResponse.setSuccessFlag(icmpResponseDTO.isSuccessFlag());
		icmpResponse.setThrowable(icmpResponseDTO.getThrowable());
		icmpResponse.setTimeoutFlag(icmpResponseDTO.isTimeoutFlag());
		icmpResponse.setTtl(icmpResponseDTO.getTtl());

		return icmpResponse;
	}

	//-------------------------------------------------------------------------------------------------
	private static void validateIcmpResponseDTO(final IcmpPingResponseDTO icmpResponseDTO) {
		logger.debug("validateIcmpResponseDTO started...");

		try {

			Assert.notNull(icmpResponseDTO, "IcmpResponseDTO is null");
			Assert.notNull(icmpResponseDTO.getDuration(), "Duration is null");
			Assert.notNull(icmpResponseDTO.getHost(), "Host is null");
			Assert.notNull(icmpResponseDTO.getRtt(), "Rtt is null");
			Assert.notNull(icmpResponseDTO.getSize(), "Size is null");
			Assert.notNull(icmpResponseDTO.isSuccessFlag(), "SuccessFlag is null");
			Assert.notNull(icmpResponseDTO.isTimeoutFlag(), "TimeOutFlag is null");
			Assert.notNull(icmpResponseDTO.getTtl(), "Ttl is null");

		} catch (final IllegalArgumentException ex) {

			throw new InvalidParameterException(ex.getMessage());
		}

	}
}
