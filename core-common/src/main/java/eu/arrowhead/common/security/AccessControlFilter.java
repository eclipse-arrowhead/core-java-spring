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

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.filter.ArrowheadFilter;
import eu.arrowhead.common.filter.thirdparty.MultiReadRequestWrapper;

public abstract class AccessControlFilter extends ArrowheadFilter {
	
	//=================================================================================================
	// members
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	protected Map<String,Object> arrowheadContext;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			log.debug("Checking access in AccessControlFilter...");
			try {
				final MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper((HttpServletRequest) request);
				final String requestTarget = Utilities.stripEndSlash(requestWrapper.getRequestURL().toString());
				final String requestJSON = requestWrapper.getCachedBody();
				final Map<String,String[]> queryParams = requestWrapper.getParameterMap();
				final String clientCN = getCertificateCNFromRequest(requestWrapper);
				if (clientCN == null) {
					log.error("Unauthorized access: {}", requestTarget);
					throw new AuthException("Unauthorized access: " + requestTarget);
				}
				
				checkClientAuthorized(clientCN, requestWrapper.getMethod(), requestTarget, requestJSON, queryParams);
				
				log.debug("Using MultiReadRequestWrapper in the filter chain from now...");
				chain.doFilter(requestWrapper, response);
			} catch (final ArrowheadException ex) {
				handleException(ex, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		if (!Utilities.isKeyStoreCNArrowheadValid(clientCN)) {
			log.debug("{} is not a valid common name, access denied!", clientCN);
	        throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}

	    // All requests from the local cloud are allowed
	    if (!Utilities.isKeyStoreCNArrowheadValid(clientCN, getServerCloudCN())) {
	        log.debug("{} is unauthorized to access {}", clientCN, requestTarget);
	        throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	protected String getServerCloudCN() {
		final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
	    final String[] serverFields = serverCN.split("\\.", 2); // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
	    Assert.isTrue(serverFields.length >= 2, "Server common name is invalid: " + serverCN);
	    
	    return serverFields[1];
	}

	//-------------------------------------------------------------------------------------------------
	@Nullable
	private String getCertificateCNFromRequest(final HttpServletRequest request) {
		final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
		if (certificates != null && certificates.length != 0) {
			final X509Certificate cert = certificates[0];
			return Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
		}
		
		return null;
	}
}