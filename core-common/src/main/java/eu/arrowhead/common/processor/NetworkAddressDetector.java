package eu.arrowhead.common.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@Component
public class NetworkAddressDetector {
	
	//=================================================================================================
	// methods
	
	@Value(CoreCommonConstants.$USE_NETWORK_ADDRESS_DETECTOR_WD)
	private boolean useDetector;
	
	@Value(CoreCommonConstants.$FILTER_PROXY_ADDRESSES_WD)
	private String[] filterProxyAddressList;
	private final Set<String> filterProxyAddressSet = new HashSet<>();
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	private static final String HEADER_FORWARDED = "forwarded";
	private static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";
	
	private static final String DOT = ".";
	private static final String COMMA = ",";
	private static final String COLON = ":";
	private static final String SEMI_COLON = ";";
	private static final String DOUBLE_QUOTE = "\"";
	private static final String SQUARE_BRACKET_OPEN = "[";
	private static final String SQUARE_BRACKET_CLOSE = "]";
	private static final String EQUAL_SIGN = "=";
	private static final int IPV4_DOT_NUMBER = 3;
	
	private final Logger logger = LogManager.getLogger(NetworkAddressDetector.class);
	
	//-------------------------------------------------------------------------------------------------
	public AddressDetectionResult detect(final HttpServletRequest servletRequest) { // TODO junit
		logger.debug("detect started...");
		final AddressDetectionResult result = new AddressDetectionResult();
		
		if (!useDetector || servletRequest == null) {
			result.setSkipped(true);
			result.setDetectionMessage("Address-Port detection process was skipped");
			return result;
		}
		boolean detected = retrieveFromConnector(servletRequest, result);
		
		if (!detected) {
			detected = detectByHeaderForwarded(servletRequest, result);			
		}
		if (!detected) {
			detected = detectByHeaderXForwardedFor(servletRequest, result);
		}
		
		if (detected) {
			try {
				networkAddressVerifier.verify(result.getDetectedAddress()); // already normalized with NetworkAddressPreProcessor
				return result;
			} catch (final InvalidParameterException ex) {
				result.setDetectionSuccess(false);
				result.setDetectionMessage("Detected address is invalid! " + ex.getMessage());
				return result;
			}
		} else {
			result.setDetectionMessage("Network address detection was unsuccessful.");
			return result;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void init() {
		for (final String addr : filterProxyAddressList) {
			filterProxyAddressSet.add(networkAddressPreProcessor.normalize(addr));
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean retrieveFromConnector(final HttpServletRequest servletRequest, final AddressDetectionResult result) {
		String remoteAddr = networkAddressPreProcessor.normalize(servletRequest.getRemoteAddr());
		
		if (remoteAddr.equals(NetworkAddressVerifier.IPV4_PLACEHOLDER) ||
			remoteAddr.startsWith(NetworkAddressVerifier.IPV4_LOOPBACK_1ST_OCTET) ||
			remoteAddr.equals(NetworkAddressVerifier.IPV6_LOOPBACK) ||
			remoteAddr.equals(NetworkAddressVerifier.IPV6_UNSPECIFIED)) {
			
			remoteAddr = networkAddressPreProcessor.normalize(servletRequest.getLocalAddr());
		}
		
		if (Utilities.isEmpty(remoteAddr) || filterProxyAddressSet.contains(remoteAddr)) {
			return false;
		} else {
			result.setDetectedAddress(remoteAddr);
			result.setDetectionSuccess(true);
			return true;
		}		
	}

	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderForwarded(final HttpServletRequest servletRequest, final AddressDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
		//https://tools.ietf.org/html/rfc7230#section-3.2
		final List<String> headerValues = Collections.list(servletRequest.getHeaders(HEADER_FORWARDED));
		for (int i = headerValues.size() - 1; i >= 0; i--) {
			
			String[] subValues = new String[1];
			if (headerValues.get(i).contains(SEMI_COLON)) {
				subValues = headerValues.get(i).split(SEMI_COLON);
			} else if (headerValues.get(i).contains(COMMA)) {
				subValues = headerValues.get(i).split(COMMA);
			} else {
				subValues[0] = headerValues.get(i);
			}
			
			for (int j = subValues.length - 1; j >= 0; j--) {
				final String[] pair = subValues[j].split(EQUAL_SIGN);
				if (pair[0].toLowerCase().trim().startsWith("for")) {
					if (Utilities.isEmpty(pair[1])) {
						return false;
					}
					
					final String address = processAddress(pair[1]);
					if (!filterProxyAddressSet.contains(address)) {
						result.setDetectionSuccess(true);
						result.setDetectedAddress(address);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderXForwardedFor(final HttpServletRequest servletRequest, final AddressDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
		final List<String> headerValues = Collections.list(servletRequest.getHeaders(HEADER_X_FORWARDED_FOR));
		for (int i = headerValues.size() - 1; i >= 0; i--) {
			final String[] subValues = headerValues.get(i).split(COMMA);
			for (int j = subValues.length - 1; j >= 0; j--) {
				if (Utilities.isEmpty(subValues[j])) {
					return false;
				}
				
				final String address = processAddress(subValues[j]);
				if (!filterProxyAddressSet.contains(address)) {
					result.setDetectionSuccess(true);
					result.setDetectedAddress(address);
					return true;
				}				
			}
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String processAddress(final String value) {
		final StringBuilder sb = new StringBuilder();
		int dotCtn = 0;
		final List<Integer> colonIdxList = new ArrayList<>();
		for (int i = 0; i < value.length(); i++) {
			final String ch = String.valueOf(value.charAt(i));
			
			if (DOUBLE_QUOTE.equals(ch) || SQUARE_BRACKET_OPEN.equals(ch)) {
				continue;
			}
			if (SQUARE_BRACKET_CLOSE.equals(ch)) {
				break;
			}
			if (DOT.equals(ch)) {
				dotCtn++;
			}
			if (COLON.equals(ch)) {
				colonIdxList.add(i);
			}
			sb.append(ch);
		}
		
		String address;
		if (dotCtn == IPV4_DOT_NUMBER && colonIdxList.size() == 1) {
			address = sb.substring(0, Math.max(0, colonIdxList.get(0)));
		} else {
			address = sb.toString();			
		}
		
		return networkAddressPreProcessor.normalize(address);
	}
}
