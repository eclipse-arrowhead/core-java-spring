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

import static eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.CRLF;
import static eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.CRLF_PATTERN_STR;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

final class GatewayHTTPRequestCache {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(GatewayHTTPRequestCache.class);
	
	private static final String CONTENT_LENGTH_HEADER = "content-length";
	
	private StringBuffer cache;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	GatewayHTTPRequestCache(final int initialCapacity) {
		Assert.isTrue(initialCapacity > 0, "Initial capacity must be a positive number.");
		
		this.cache = new StringBuffer(initialCapacity);
	}
	
	//-------------------------------------------------------------------------------------------------
	void addBytes(final byte[] bytes) {
		logger.debug("addBytes started...");
		
		if (bytes != null && bytes.length > 0) {
			cache.append(new String(bytes, StandardCharsets.ISO_8859_1));
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	String getHTTPRequest() {
		logger.debug("getHttpRequest started...");
		
		if (cache.length() > 0) {
			final int[] requestLineBoundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries(cache);
			if (requestLineBoundaries == null) {
				logger.debug("No request returned because request line is not found");
				return null;
			}
			final int requestStartIdx = requestLineBoundaries[0];
			final int requestLineEndIdx = requestLineBoundaries[1];
			cache.delete(0, requestStartIdx);
			logger.debug("Remove {} bytes from cache.", requestStartIdx);
			
			final int headerSectionEndsIdx = cache.indexOf(CRLF + CRLF);
			if (headerSectionEndsIdx < 0) { // means cache does not contain all request
				logger.debug("No request returned because header section's end is not found");
				return null;
			} 

			final int bodyLength = getBodyLength(cache.substring(0, headerSectionEndsIdx));
			final int requestEndIdx = headerSectionEndsIdx + bodyLength + 4; // +4 because of the CRLFCRLF sequence
			if (requestEndIdx > cache.length()) { // means cache does not contain all body
				logger.debug("No request returned because cache does not contain all body bytes");
				return null;
			}
			
			final String request = cache.substring(0, requestEndIdx);
			logger.debug("Request returned: {} bytes", requestEndIdx);

			
			// We don't remove the whole request from the cache only the request-line because if a request does not finished (some bytes are missing from its body) before a new request starts the cache will
			// use the new requests starting bytes to finish the previous request. With this solution the first request will fail (as it should) but additional requests can still work.
			cache.delete(0, requestLineEndIdx + 1);
			logger.debug("Remove {} bytes from cache.", requestLineEndIdx + 1);
			
			return request;
		}
		
		logger.debug("No request returned because cache is empty");
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	byte[] getHTTPRequestBytes() {
		final String result = getHTTPRequest();
		
		return result == null ? null : result.getBytes(StandardCharsets.ISO_8859_1);
	}
	
	//-------------------------------------------------------------------------------------------------
	String getCacheContent() {
		return cache.toString();
	}
	
	//-------------------------------------------------------------------------------------------------
	byte[] getCacheContentBytes() {
		final String result = getCacheContent();
		
		return result == null ? null : result.getBytes(StandardCharsets.ISO_8859_1);
	}
	
	//-------------------------------------------------------------------------------------------------
	int getCacheLength() {
		return cache.length();
	}
	
	//-------------------------------------------------------------------------------------------------
	void resetCache() {
		logger.debug("resetCache started...");
		
		cache.delete(0, cache.length());
	}
	
	//=================================================================================================
	// assistant methods 
	
	//-------------------------------------------------------------------------------------------------
	private int getBodyLength(final String request) {
		logger.debug("getBodyLength started...");
		
		final String[] lines = request.split(CRLF_PATTERN_STR);
		
		for (final String line : lines) {
			final String[] parts = line.split(":");
			if (parts.length > 1 && CONTENT_LENGTH_HEADER.equalsIgnoreCase(parts[0].trim())) {
				try {
					final int length = Integer.parseInt(parts[1].trim());
					logger.debug("Body length: {}", length);
					
					return length;
				} catch (final NumberFormatException ex) {
					logger.error("Content-Length header contains a non-integer value: {}", parts[1].trim());
					logger.error("Stacktrace: ", ex);
					logger.debug("Body length: 0 (NFE)");
					
					return 0;
				}
			}
		}
		
		logger.debug("Body length: 0 (Header not found)");
		
		return 0;
	}
}