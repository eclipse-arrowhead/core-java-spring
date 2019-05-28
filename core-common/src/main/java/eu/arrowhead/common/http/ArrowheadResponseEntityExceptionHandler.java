package eu.arrowhead.common.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@ControllerAdvice
public class ArrowheadResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final String UNKNOWN = "unknown";
	private Logger logger = LogManager.getLogger(ArrowheadResponseEntityExceptionHandler.class);
	private final HttpHeaders headers = new HttpHeaders();
	{
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
	}

	@ExceptionHandler(ArrowheadException.class)
	public ResponseEntity<Object> handleArrowheadException(final ArrowheadException ex, final WebRequest request) {
		final String origin = ex.getOrigin() != null ? ex.getOrigin() : (request.getContextPath() != null ? request.getContextPath() : UNKNOWN);
		HttpStatus status = HttpStatus.resolve(ex.getErrorCode());
	    if (status == null) {
	    	switch (ex.getExceptionType()) {
	    	case AUTH:
	    		status = HttpStatus.UNAUTHORIZED;
			    break;
	        case BAD_PAYLOAD:
	        	status = HttpStatus.BAD_REQUEST;
	        	logger.error("BadPayloadException at: " + origin);
	          	break;
	        case DATA_NOT_FOUND:
	        	status = HttpStatus.NOT_FOUND;
	        	break;
	        case UNAVAILABLE:
	        	status = HttpStatus.GATEWAY_TIMEOUT;
	        	break;
	        default:
	    		status = HttpStatus.INTERNAL_SERVER_ERROR;
	    	}
	    }
		
		logger.debug(ex.getClass().getName() + " at " + ex.getOrigin() + ": " + ex.getMessage(), ex);
		final ErrorMessageDTO dto = new ErrorMessageDTO(ex);
		if (ex.getErrorCode() == 0) {
			dto.setErrorCode(status.value());
		}
		return handleExceptionInternal(ex, dto, headers, status, request);
	}
}