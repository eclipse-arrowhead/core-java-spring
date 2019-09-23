package eu.arrowhead.common.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;

public abstract class ArrowheadFilter extends GenericFilterBean {
	
	//=================================================================================================
	// members
	
	protected final Logger log = LogManager.getLogger(ArrowheadFilter.class);
	protected final ObjectMapper mapper = new ObjectMapper();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	protected ArrowheadFilter() {
		log.info("{} is active", this.getClass().getSimpleName());
	}

	//-------------------------------------------------------------------------------------------------
	protected void handleException(final ArrowheadException ex, final ServletResponse response) throws IOException {
		final HttpStatus status = Utilities.calculateHttpStatusFromArrowheadException(ex);
		final String origin = ex.getOrigin() == null ? CommonConstants.UNKNOWN_ORIGIN : ex.getOrigin();
		log.debug("{} at {}: {}", ex.getClass().getName(), origin, ex.getMessage());
		log.debug("Exception", ex);
		final ErrorMessageDTO dto = new ErrorMessageDTO(ex);
		if (ex.getErrorCode() <= 0) {
			dto.setErrorCode(status.value());
		}
		sendError(status, dto, (HttpServletResponse) response);
	}

	//-------------------------------------------------------------------------------------------------
	protected void sendError(final HttpStatus status, final ErrorMessageDTO dto, final HttpServletResponse response) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(status.value());
		response.getWriter().print(mapper.writeValueAsString(dto));
		response.getWriter().flush();
	}
}