package eu.arrowhead.common.security;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;

public class AccessControlFilter extends GenericFilterBean {
	
	protected Logger log = LogManager.getLogger(AccessControlFilter.class);
	protected ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			try {
				final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
//				final String requestTarget = Utilities.stripEndSlash(httpServletRequest.getRequestURL().toString());
				final String clientCN = getCertificateCNFromRequest(httpServletRequest);
				if (clientCN == null) {
					//TODO: better exception
					throw new AuthException("Do you see this?");
				}
			} catch (final ArrowheadException ex) {
				handleException(ex, response);
				return;
			}
		}
		
		chain.doFilter(request, response);
	}

	private String getCertificateCNFromRequest(final HttpServletRequest request) {
		final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
		if (certificates != null && certificates.length != 0) {
			final X509Certificate cert = certificates[0];
			return Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
		}
		
		return null;
	}
	
	private void handleException(final ArrowheadException ex, final ServletResponse response) throws IOException {
		final HttpStatus status = Utilities.calculateHttpStatusFromArrowheadException(ex);
		final String origin = ex.getOrigin() == null ? CommonConstants.UNKNOWN_ORIGIN : ex.getOrigin();
		log.debug("{} at {}: {}", ex.getClass().getName(), origin, ex.getMessage());
		log.debug("Exception", ex);
		final ErrorMessageDTO dto = new ErrorMessageDTO(ex);
		if (ex.getErrorCode() == 0) {
			dto.setErrorCode(status.value());
		}
		sendError(status, dto, (HttpServletResponse) response);
	}

	private void sendError(final HttpStatus status, final ErrorMessageDTO dto, final HttpServletResponse response) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(status.value());
		response.getWriter().print(mapper.writeValueAsString(dto));
		response.getWriter().flush();
	}
}