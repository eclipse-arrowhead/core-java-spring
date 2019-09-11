package eu.arrowhead.common.dto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.exception.UnavailableServerException;

public class DTOUtilities {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(DTOUtilities.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static boolean equalsSystemInResponseAndRequest(final SystemResponseDTO response, final SystemRequestDTO request) {
		if (response == null) {
			return request == null;
		}
		
		if (request == null) {
			return false;
		}
		
		final SystemRequestDTO converted = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(response);
		normalizeSystemRequestDTO(converted);
		
		final SystemRequestDTO requestCopy = copySystemRequestDTO(request);
		normalizeSystemRequestDTO(requestCopy);
		
		return converted.equals(requestCopy);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static boolean equalsCloudInResponseAndRequest(final CloudResponseDTO response, final CloudRequestDTO request) {
		if (response == null) {
			return request == null;
		}
		
		if (request == null) {
			return false;
		}
		
		final CloudRequestDTO converted = DTOConverter.convertCloudResponseDTOToCloudRequestDTO(response);
		normalizeCloudRequestDTO(converted);
		
		final CloudRequestDTO requestCopy = copyCloudRequestDTO(request);
		normalizeCloudRequestDTO(requestCopy);
		
		return converted.equals(requestCopy);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static void createExceptionFromErrorMessageDTO(final ErrorMessageDTO dto) {
		Assert.notNull(dto, "Error message object is null.");
		Assert.notNull(dto.getExceptionType(), "Exception type is null.");
		
		switch (dto.getExceptionType()) {
	    case ARROWHEAD:
	    	throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case AUTH:
	        throw new AuthException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case BAD_PAYLOAD:
	        throw new BadPayloadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case INVALID_PARAMETER:
	    	throw new InvalidParameterException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case DATA_NOT_FOUND:
            throw new DataNotFoundException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case GENERIC:
            throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case TIMEOUT:
        	throw new TimeoutException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case UNAVAILABLE:
	        throw new UnavailableServerException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    default:
	    	logger.error("Unknown exception type: {}", dto.getExceptionType());
	    	throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        }
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private DTOUtilities() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static SystemRequestDTO copySystemRequestDTO(final SystemRequestDTO orig) {
		if (orig == null) {
			return null;
		}
		
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName(orig.getSystemName());
		result.setAddress(orig.getAddress());
		result.setPort(orig.getPort());
		result.setAuthenticationInfo(orig.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void normalizeSystemRequestDTO(final SystemRequestDTO dto) {
		final String systemName = dto.getSystemName();
		if (systemName != null) {
			dto.setSystemName(systemName.toLowerCase().trim());
		}
		
		final String address = dto.getAddress();
		if (address != null) {
			dto.setAddress(address.toLowerCase().trim());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private static CloudRequestDTO copyCloudRequestDTO(final CloudRequestDTO orig) {
		if (orig == null) {
			return null;
		}
		
		final CloudRequestDTO result = new CloudRequestDTO();
		result.setName(orig.getName());
		result.setOperator(orig.getOperator());
		result.setSecure(orig.getSecure());
		result.setNeighbor(orig.getNeighbor());
		result.setAuthenticationInfo(orig.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private static void normalizeCloudRequestDTO(final CloudRequestDTO dto) {
		final String cloudName = dto.getName();
		if (cloudName != null) {
			dto.setName(cloudName.toLowerCase().trim());
		}
		
		final String operator = dto.getOperator();
		if (operator != null) {
			dto.setOperator(operator.toLowerCase().trim());
		}
	}
}