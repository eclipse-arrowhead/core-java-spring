package eu.arrowhead.common.security;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.annotation.Resource;
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
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.thirdparty.MultiReadRequestWrapper;

public abstract class AccessControlFilter extends GenericFilterBean {
	
	protected Logger log = LogManager.getLogger(AccessControlFilter.class);
	protected ObjectMapper mapper = new ObjectMapper();
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	protected Map<String,String> arrowheadContext;
	
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			try {
				final MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper((HttpServletRequest) request);
				final String requestTarget = Utilities.stripEndSlash(requestWrapper.getRequestURL().toString());
				final String requestJSON = requestWrapper.getCachedBody();
				final String clientCN = getCertificateCNFromRequest(requestWrapper);
				if (clientCN == null) {
					log.error("Unauthorized access: {}", requestTarget);
					throw new AuthException("Unauthorized access: " + requestTarget);
				}
				
				if (!isClientAuthorized(clientCN, requestWrapper.getMethod(), requestTarget, requestJSON)) {
			        log.error("{} is unauthorized to access {}", clientCN, requestTarget);
			        throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
			    }
				
				chain.doFilter(requestWrapper, response);
			} catch (final ArrowheadException ex) {
				handleException(ex, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	protected boolean isClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON) {
		final String serverCN = arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
	    final String[] serverFields = serverCN.split("\\.", 2); // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
	    Assert.isTrue(serverFields.length >= 2, "Server common name is invalid: " + serverCN);
	    
	    // All requests from the local cloud are allowed
	    return Utilities.isKeyStoreCNArrowheadValid(clientCN, serverFields[1]);
	}

	@Nullable
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
		if (ex.getErrorCode() <= 0) {
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