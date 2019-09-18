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

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
public class ArrowheadHttpClientResponseErrorHandler extends DefaultResponseErrorHandler {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(HttpService.class);
	private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void handleError(final URI url, final HttpMethod method, final ClientHttpResponse response) throws IOException {
		ErrorMessageDTO dto;
		try {
			dto = mapper.readValue(response.getBody(), ErrorMessageDTO.class);
		} catch (final IOException ex) {
			logger.debug("Unable to deserialize error message: {}", ex.getMessage());
			logger.debug("Exception: ", ex);
		    logger.error("Request failed at {}, response status code: {}", url, response.getRawStatusCode());
		    throw new ArrowheadException("Unknown error occurred at " + url + ". Check log for possibly more information.", ex);
		}
		
		if (dto.getExceptionType() == null) {
			logger.error("Request failed at {}, response status code: {}", url, response.getRawStatusCode());
			logger.error("Request failed, error message: {}", dto.getErrorMessage());
		    throw new ArrowheadException("Unknown error occurred at " + url + ". Check log for possibly more information.");
		}
		
		logger.debug("Error occured at {}. Returned with {}", url, response.getRawStatusCode());
		logger.error("Request returned with {}: {}", dto.getExceptionType(), dto.getErrorMessage());
		Utilities.createExceptionFromErrorMessageDTO(dto);
	}
}