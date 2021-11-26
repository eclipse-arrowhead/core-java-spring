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
		icmpResponse.setDuration(icmpResponseDTO.getDuration() != null ? icmpResponseDTO.getDuration() : 0 );
		icmpResponse.setErrorMessage(icmpResponseDTO.getErrorMessage());
		icmpResponse.setHost(icmpResponseDTO.getHost());
		icmpResponse.setRtt(icmpResponseDTO.getRtt() != null ? icmpResponseDTO.getRtt() : 0);
		icmpResponse.setSize(icmpResponseDTO.getSize() != null ? icmpResponseDTO.getSize() : 0);
		icmpResponse.setSuccessFlag(icmpResponseDTO.isSuccessFlag());
		icmpResponse.setThrowable(icmpResponseDTO.getThrowable());
		icmpResponse.setTimeoutFlag(icmpResponseDTO.isTimeoutFlag());
		icmpResponse.setTtl(icmpResponseDTO.getTtl() != null ? icmpResponseDTO.getTtl() : 0);

		return icmpResponse;
	}

	//-------------------------------------------------------------------------------------------------
	private static void validateIcmpResponseDTO(final IcmpPingResponseDTO icmpResponseDTO) {
		logger.debug("validateIcmpResponseDTO started...");

		try {

			Assert.notNull(icmpResponseDTO, "IcmpResponseDTO is null");

		} catch (final IllegalArgumentException ex) {

			throw new InvalidParameterException(ex.getMessage());
		}

	}
}
