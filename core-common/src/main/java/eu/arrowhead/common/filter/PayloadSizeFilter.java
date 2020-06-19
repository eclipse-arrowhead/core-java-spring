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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ExceptionType;

public class PayloadSizeFilter extends ArrowheadFilter {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			log.debug("Checking payload size...");
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			final String method = httpRequest.getMethod();
			final long contentLength = httpRequest.getContentLengthLong();
			final String requestTarget = httpRequest.getRequestURL().toString();
			if (!checkPayload(method, contentLength, requestTarget, httpResponse)) {
				return;
			}
		}
		
		chain.doFilter(request, response);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private boolean checkPayload(final String method, final long contentLength, final String requestTarget, final HttpServletResponse response) throws IOException {
		final RequestMethod requestMethod = RequestMethod.valueOf(method);
		
		switch (requestMethod) {
		case POST:
		case PUT:
		case PATCH:
			if (contentLength == 0) {
				final HttpStatus status = HttpStatus.BAD_REQUEST;
				final ErrorMessageDTO errorMessage = new ErrorMessageDTO("Message body is null (unusual for POST/PUT/PATCH request)! If you truly want to send an empty payload, " +
																		 "try sending empty brackets: {} for JSON object, [] for JSON array.", status.value(), ExceptionType.BAD_PAYLOAD,
																		 requestTarget);
				log.debug("Trying to send {} request to {} with empty payload.", method, requestTarget);
				sendError(status, errorMessage, response);
				return false;
			}
			break;
		case GET:
		case DELETE:
			if (contentLength > 0) {
				final HttpStatus status = HttpStatus.BAD_REQUEST;
				final ErrorMessageDTO errorMessage = new ErrorMessageDTO("Message body is not null (unusual for GET/DELETE request)!", status.value(), ExceptionType.BAD_PAYLOAD,
																		 requestTarget);
				log.debug("Trying to send {} request to {} with payload.", method, requestTarget);
				sendError(status, errorMessage, response);
				return false;
			}
			break;
		default:
			// do nothing
		}
		
		return true;
	}
}