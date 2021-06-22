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

package eu.arrowhead.core.gateway.thread;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;

final class GatewayHTTPUtils {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(GatewayHTTPUtils.class);
	
	static final String CRLF = "\r\n";
	static final String CRLF_PATTERN_STR = "\\r\\n";
	
	private static final String REQUEST_LINE_PREFIX_PATTERN_STR = "^" + createHttpMethodsRegExp() + " \\S+";
	private static final String REQUEST_LINE_PATTERN_STR = REQUEST_LINE_PREFIX_PATTERN_STR + " HTTP/" + createSupportedHttpVersionsRegExp() + CRLF_PATTERN_STR;
	private static final String BOUNDLESS_REQUEST_LINE_PATTERN_STR = REQUEST_LINE_PATTERN_STR.substring(1, REQUEST_LINE_PATTERN_STR.length());
	
	private static final Pattern REQUEST_LINE_PREFIX_PATTERN = Pattern.compile(REQUEST_LINE_PREFIX_PATTERN_STR);
	private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile(REQUEST_LINE_PATTERN_STR);
	private static final Pattern BOUNDLESS_REQUEST_LINE_PATTERN = Pattern.compile(BOUNDLESS_REQUEST_LINE_PATTERN_STR);
	
	private static final String TRANSFER_ENCODING_HEADER = "transfer-encoding";
	private static final String CHUNKED_VALUE = "chunked";

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	static Answer isStartOfAHttpRequest(final CharSequence messageStart) {
		logger.debug("isStartOfAHttpRequest started...");
		
		if (messageStart == null) {
			logger.debug("Answer: NO");
			return Answer.NO;
		}
		
		final Matcher matcher = REQUEST_LINE_PATTERN.matcher(messageStart);
		if (matcher.lookingAt()) {
			logger.debug("Answer: YES");
			return Answer.YES;
		}
		
		matcher.reset();
		matcher.usePattern(REQUEST_LINE_PREFIX_PATTERN);
		if (matcher.matches()) {
			logger.debug("Answer: CAN_BE");
			return Answer.CAN_BE;
		}
		
		logger.debug("Answer: NO");
		return Answer.NO;
	}
	
	//-------------------------------------------------------------------------------------------------
	static Answer isStartOfAHttpRequest(final byte[] messageStart) {
		if (messageStart == null) {
			logger.debug("Answer: NO");
			return Answer.NO;
		}
		
		return isStartOfAHttpRequest(new String(messageStart, StandardCharsets.ISO_8859_1));
	}
	
	//-------------------------------------------------------------------------------------------------
	static Answer isChunkedHttpRequest(final CharSequence messageStart) {
		logger.debug("isChunkedHttpRequest started...");

		if (messageStart == null) {
			logger.debug("Answer: NO");
			return Answer.NO;
		}
		
		final Answer isHttpRequest = isStartOfAHttpRequest(messageStart);
		switch (isHttpRequest) {
		case NO:
		case CAN_BE:
			logger.debug("Answer: {}", isHttpRequest.name());
			return isHttpRequest;
		default: 
			return isChunkedTransferEncodingHeaderIsPresent(messageStart.toString());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	static Answer isChunkedHttpRequest(final byte[] messageStart) {
		if (messageStart == null) {
			logger.debug("Answer: NO");
			return Answer.NO;
		}
		
		return isChunkedHttpRequest(new String(messageStart, StandardCharsets.ISO_8859_1));
	}
	
	//-------------------------------------------------------------------------------------------------
	static int[] getIndicesOfHttpRequestLineBoundaries(final CharSequence request) {
		logger.debug("getIndicesOfHttpRequestLineBoundaries started...");

		if (request != null) {
			final Matcher matcher = BOUNDLESS_REQUEST_LINE_PATTERN.matcher(request);
			if (matcher.find()) {
				logger.debug("Indices found.");
				return new int[] { matcher.start(), matcher.end() -1 };
			}
		}
		
		logger.debug("Indices not found.");
		return null; 
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GatewayHTTPUtils() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static String createHttpMethodsRegExp() {
		String result = "(?:";
		
		for (final HttpMethod method : HttpMethod.values()) {
			result += method.name() + "|";
		}
		
		return result.substring(0, result.length() - 1) + ")";
	}
	
	//-------------------------------------------------------------------------------------------------
	private static String createSupportedHttpVersionsRegExp() {
		return "(?:1\\.1|2|3)";
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Answer isChunkedTransferEncodingHeaderIsPresent(final String messageStart) {
		logger.debug("isChunkedTransferEncodingHeaderIsPresent started...");

		final int idxHeaderSectionEnds = messageStart.indexOf(CRLF +  CRLF);
		final boolean headerSectionEnds = idxHeaderSectionEnds > -1; // means parameter contains all headers (or no headers and no body)
		
		logger.debug("Header section ends: {}", headerSectionEnds);
		
		final String message = headerSectionEnds ? messageStart.substring(0, idxHeaderSectionEnds) : messageStart; // remove body
		final String[] lines = message.split(CRLF_PATTERN_STR);
		boolean chunkedFound = false;
		
		for (final String line : lines) {
			final String[] parts = line.split(":");
			if (parts.length > 1 && TRANSFER_ENCODING_HEADER.equalsIgnoreCase(parts[0].trim()) && CHUNKED_VALUE.equalsIgnoreCase(parts[1].trim())) {
				chunkedFound = true;
				break;
			}
		}
		
		if (chunkedFound) {
			logger.debug("Answer: YES");
			return Answer.YES;
		} else if (headerSectionEnds) {
			logger.debug("Answer: NO");
			return Answer.NO;
		}
		
		logger.debug("Answer: CAN_BE");
		return Answer.CAN_BE;
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	enum Answer {
		YES, NO, CAN_BE;
	}
}