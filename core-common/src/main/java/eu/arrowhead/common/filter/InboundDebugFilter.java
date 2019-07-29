package eu.arrowhead.common.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.filter.thirdparty.MultiReadRequestWrapper;

public class InboundDebugFilter extends ArrowheadFilter {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			log.trace("Entering InboundDebugFilter...");
			final MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper((HttpServletRequest) request);
			
			log.debug("New {} request at: {}", requestWrapper.getMethod(), requestWrapper.getRequestURL().toString());
			if (!Utilities.isEmpty(requestWrapper.getQueryString())) {
				log.debug("Query string: {}", requestWrapper.getQueryString());
			}
			
			if (!Utilities.isEmpty(requestWrapper.getCachedBody())) {
				log.debug("Body: {}", Utilities.toPrettyJson(requestWrapper.getCachedBody()));
			}
			
			chain.doFilter(requestWrapper, response);
		} else {
			chain.doFilter(request, response);
		}
	}
}