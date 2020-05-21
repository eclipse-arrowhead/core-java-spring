package eu.arrowhead.core.gateway.thread;

import static eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.CRLF;
import static eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.CRLF_PATTERN_STR;

import java.nio.charset.StandardCharsets;

import org.springframework.util.Assert;

class GatewayHTTPRequestCache {

	//=================================================================================================
	// members
	
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
		if (bytes != null && bytes.length > 0) {
			cache.append(new String(bytes, StandardCharsets.ISO_8859_1));
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	String getHTTPRequest() {
		if (cache.length() > 0) {
			final int[] requestLineBoundaries = GatewayHTTPUtils.getIndicesOfHttpRequestLineBoundaries(cache);
			final int requestStartIdx = requestLineBoundaries[0];
			final int requestLineEndIdx = requestLineBoundaries[1];
			if (requestStartIdx < 0) {
				return null;
			}
			
			cache.delete(0, requestStartIdx);
			
			final int headerSectionEndsIdx = cache.indexOf(CRLF +  CRLF);
			if (headerSectionEndsIdx < 0) { // means cache does not contain all request
				return null;
			} 

			final int bodyLength = getBodyLength(cache.substring(0, headerSectionEndsIdx));
			final int requestEndIdx = headerSectionEndsIdx + bodyLength + 3; // +3 because headerSectionEndsIdx is the first charachter of the CRLFCRLF sequence
			if (requestEndIdx > cache.length() - 1) { // means cache does not contain all body
				return null;
			}
			
			
			final String request = cache.substring(0, requestEndIdx);
			
			// We don't remove the whole request from the cache only the request-line because if a request does not finished (some bytes are missing from its body) before a new request starts the cache will
			// use the new requests starting bytes to finish the previous request. With this solution the first request will fail (as it should) but additional requests can still work.
			cache.delete(0, requestLineEndIdx + 1);
			
			return request;
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	byte[] getHTTPRequestBytes() {
		final String result = getHTTPRequest();
		
		return result == null ? null : result.getBytes(StandardCharsets.ISO_8859_1);
	}
	
	//=================================================================================================
	// assistant methods 
	
	//-------------------------------------------------------------------------------------------------
	private int getBodyLength(final String request) {
		final String[] lines = request.split(CRLF_PATTERN_STR);
		
		for (final String line : lines) {
			final String[] parts = line.split(":");
			if (parts.length > 1 && CONTENT_LENGTH_HEADER.equalsIgnoreCase(parts[0].trim())) {
				try {
					return Integer.parseInt(parts[1].trim());
				} catch (final NumberFormatException ex) {
					//TODO: log
					return 0;
				}
			}
		}
		
		return 0;
	}
}