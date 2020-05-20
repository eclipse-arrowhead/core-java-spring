package eu.arrowhead.core.gateway.thread;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpMethod;

final class GatewayHTTPUtils {
	
	//=================================================================================================
	// members
	
	private static final String CRLF = "\\r\\n";
	private static final String REQUEST_LINE_PREFIX_PATTERN_STR = "^" + createHttpMethodsRegExp() + " \\S+";
	private static final String REQUEST_LINE_PATTERN_STR = REQUEST_LINE_PREFIX_PATTERN_STR + " HTTP/" + createSupportedHttpVersionsRegExp() + CRLF;
	
	private static final Pattern REQUEST_LINE_PREFIX_PATTERN = Pattern.compile(REQUEST_LINE_PREFIX_PATTERN_STR);
	private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile(REQUEST_LINE_PATTERN_STR);
	
	private static final String TRANSFER_ENCODING_HEADER = "transfer-encoding";
	private static final String CHUNKED_VALUE = "chunked";

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	static Answer isStartOfAHttpRequest(final String messageStart) {
		if (messageStart == null) {
			return Answer.NO;
		}
		
		final Matcher matcher = REQUEST_LINE_PATTERN.matcher(messageStart);
		if (matcher.lookingAt()) {
			return Answer.YES;
		}
		
		matcher.reset();
		matcher.usePattern(REQUEST_LINE_PREFIX_PATTERN);
		if (matcher.matches()) {
			return Answer.CAN_BE;
		}
		
		return Answer.NO;
	}
	
	//-------------------------------------------------------------------------------------------------
	static Answer isChunkedHttpRequest(final String messageStart) {
		if (messageStart == null) {
			return Answer.NO;
		}
		
		final Answer isHttpRequest = isStartOfAHttpRequest(messageStart);
		switch (isHttpRequest) {
		case NO:
		case CAN_BE:
			return isHttpRequest;
		default: 
			return isChunkedTransferEncodingHeaderIsPresent(messageStart);
		}
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
		return "(1\\.1|2|3)";
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Answer isChunkedTransferEncodingHeaderIsPresent(final String messageStart) {
		final String[] lines = messageStart.split(CRLF);
		boolean headerSectionEnds = false;
		boolean chunkedFound = false;
		
		for (final String line : lines) {
			if (line.isBlank()) {
				headerSectionEnds = true;
				break;
			}
			
			final String[] parts = line.split(":");
			if (parts.length > 1 && TRANSFER_ENCODING_HEADER.equalsIgnoreCase(parts[0].trim()) && CHUNKED_VALUE.equalsIgnoreCase(parts[1].trim())) {
				chunkedFound = true;
				break;
			}
		}
		
		if (chunkedFound) {
			return Answer.YES;
		} else if (headerSectionEnds) {
			return Answer.NO;
		}
		
		return Answer.CAN_BE;
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	enum Answer {
		YES, NO, CAN_BE;
	}
}