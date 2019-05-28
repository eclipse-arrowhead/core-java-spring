package eu.arrowhead.common.http;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.UnavailableServerException;

@Component
public class ArrowheadHttpClientResponseErrorHandler extends DefaultResponseErrorHandler {
	
	private final Logger logger = LogManager.getLogger(HttpService.class);
	private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	@Override
	public void handleError(final URI url, final HttpMethod method, final ClientHttpResponse response) throws IOException {
		ErrorMessageDTO dto;
		try {
			dto = mapper.readValue(response.getBody(), ErrorMessageDTO.class);
		} catch (final IOException e) {
			logger.debug("Unable to deserialize error message: " + e.getMessage(), e);
		    logger.error("Request failed at " + url + ", response status code: " + response.getRawStatusCode());
		    throw new RuntimeException("Unknown error occurred at " + url + ". Check log for possibly more information.", e);
		}
		
		if (dto.getExceptionType() == null) {
		    logger.error("Request failed at " + url + ", response status code: " + response.getRawStatusCode());
		    logger.error("Request failed, error message: " + dto.getErrorMessage());
		    throw new RuntimeException("Unknown error occurred at " + url + ". Check log for possibly more information.");
		}
		
		logger.debug("Error occured at " + url + ". Returned with " + response.getRawStatusCode());
		logger.error("Request returned with " + dto.getExceptionType() + ": " + dto.getErrorMessage());
		switch (dto.getExceptionType()) {
	    case ARROWHEAD:
	    	throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case AUTH:
	        throw new AuthException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case BAD_PAYLOAD:
	        throw new BadPayloadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case DATA_NOT_FOUND:
            throw new DataNotFoundException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case GENERIC:
            throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case UNAVAILABLE:
	        throw new UnavailableServerException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    default:
	    	logger.error("Unknown exception type: " + dto.getExceptionType());
	    	throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        }
	}
}