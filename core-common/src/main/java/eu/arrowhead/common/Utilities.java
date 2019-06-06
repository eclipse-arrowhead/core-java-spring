package eu.arrowhead.common;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.exception.ArrowheadException;

public class Utilities {
	
	private static Logger logger = LogManager.getLogger(Utilities.class);
	
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
	
	public static HttpStatus calculateHttpStatusFromArrowheadException(final ArrowheadException ex) {
		Assert.notNull(ex, "Exception is null.");
		
		HttpStatus status = HttpStatus.resolve(ex.getErrorCode());
	    if (status == null) {
	    	switch (ex.getExceptionType()) {
	    	case AUTH:
	    		status = HttpStatus.UNAUTHORIZED;
			    break;
	        case BAD_PAYLOAD:
	        	status = HttpStatus.BAD_REQUEST;
	          	break;
	        case INVALID_PARAMETER:
	        	status = HttpStatus.BAD_REQUEST;
	          	break;
	        case DATA_NOT_FOUND:
	        	status = HttpStatus.NOT_FOUND;
	        	break;
	        case UNAVAILABLE:
	        	status = HttpStatus.GATEWAY_TIMEOUT;
	        	break;
	        default:
	    		status = HttpStatus.INTERNAL_SERVER_ERROR;
	    	}
	    }
	    
		return status;
	}
	
	@Nullable
	public static String getCertCNFromSubject(final String subjectName) {
		if (subjectName == null) {
			return null;
		}
		
	    try {
	    	// Subject is in LDAP format, we can use the LdapName object for parsing
	    	LdapName ldapname = new LdapName(subjectName);
	    	for (final Rdn rdn : ldapname.getRdns()) {
	    		// Find the data after the CN field
	    		if (CommonConstants.COMMON_NAME_FIELD_NAME.equalsIgnoreCase(rdn.getType())) {
	    			return (String) rdn.getValue();
	    		}
	    	}
	    } catch (final InvalidNameException ex) {
	    	logger.warn("InvalidNameException in getCertCNFromSubject: " + ex.getMessage(), ex);
	    }

	    return null;
	}
	
	@Nullable
	public static String stripEndSlash(final String uri) {
	    if (uri != null && uri.endsWith("/")) {
	    	return uri.substring(0, uri.length() - 1);
	    }
	    
	    return uri;
	}
	
	private Utilities() {
		throw new UnsupportedOperationException();
	}
}