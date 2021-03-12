/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingResponseWrapper;

import eu.arrowhead.common.Utilities;

public class OutboundDebugFilter extends ArrowheadFilter {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			log.trace("Entering OutboundDebugFilter...");
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (response.getCharacterEncoding() == null) {
				response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			}
			
			final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
			try {
				chain.doFilter(httpRequest, responseWrapper);
			} finally {
				log.debug("Response to the {} request at: {}", httpRequest.getMethod(), httpRequest.getRequestURL().toString());
				if (responseWrapper.getContentSize() > 0) {
					final String body = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
					log.debug("Body: {}", Utilities.toPrettyJson(body));
				}
				if (!responseWrapper.isCommitted()) {
					responseWrapper.copyBodyToResponse();
				}
			}
		} else {
			chain.doFilter(request, response);
		}
	}
}