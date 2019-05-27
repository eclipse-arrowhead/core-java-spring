package eu.arrowhead.common;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class Utilities {
	
	public static boolean isEmpty(final String str) {
		return str == null || str.isBlank();
	}

	/**
	 * 
	 * @param scheme default: http
	 * @param host default: 127.0.0.1
	 * @param port default: 80
	 * @param queryParams default: null
	 * @param path default: null
	 * @param pathSegments default: null
	 */
	public static UriComponents createURI(final String scheme, final String host, final int port, final MultiValueMap<String, String> queryParams, final String path, final String... pathSegments) {
		final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		builder.scheme(scheme == null ? CommonConstants.HTTP : scheme)
			   .host(host == null ? CommonConstants.LOCALHOST : host)
			   .port(port <= 0 ? CommonConstants.HTTP_PORT : port);
		
		if (queryParams != null) {
			builder.queryParams(queryParams);
		}
		
		if (pathSegments != null && pathSegments.length > 0) {
			builder.pathSegment(pathSegments);
		}
		
		if (!Utilities.isEmpty(path)) {
			builder.path(path);
		}
		
		return builder.build();
	}
	
	public static UriComponents createURI(final String scheme, final String host, final int port, final String path) {
		return createURI(scheme, host, port, null, path);
	}
	
	private Utilities() {
		throw new UnsupportedOperationException();
	}
}
