package eu.arrowhead.common.processor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.model.AddressPortDetectionResult;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@Component
public class AddressPortDetector {
	
	//If the core system is running behind a load balancer proxy for example, than it would detect the IP-Port of the proxy

	//=================================================================================================
	// methods
	
	@Value(CoreCommonConstants.$USE_ADDRESS_PORT_DETECTOR_WD)
	private boolean useDetector;
	
	@Value(CoreCommonConstants.$FILTER_PROXY_ADDRESSES)
	private String[] proxyAddressList;
	private final Set<String> proxyAddressSet = new HashSet<>();
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	private static final String HEADER_FORWARDED = "forwarded";
	private static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";
	
	private static final String COMMA = ",";
	private static final String COLON = ":";
	private static final String SQUARE_BRACKET_OPEN = "[";
	private static final String SQUARE_BRACKET_CLOSE = "]";
	private static final int MAX_PORT_LENGTH = 5;
	
	private final Logger logger = LogManager.getLogger(AddressPortDetector.class);
	
	//-------------------------------------------------------------------------------------------------
	public AddressPortDetectionResult detect(final HttpServletRequest servletRequest) { // TODO junit
		final AddressPortDetectionResult result = new AddressPortDetectionResult();
		
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
			} catch (final InvalidParameterException ex) {
				result.setDetectionSuccess(false);
				result.setDetectionMessage("Detected address is invalid! " + ex.getMessage());
				return result;
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void init() {
		for (final String addr : proxyAddressList) {
			proxyAddressSet.add(networkAddressPreProcessor.normalize(addr));
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean retrieveFromConnector(final HttpServletRequest servletRequest, final AddressPortDetectionResult result) {
		final String remoteAddr = networkAddressPreProcessor.normalize(servletRequest.getRemoteAddr());
		final int remotePort = servletRequest.getRemotePort();
		
		if (Utilities.isEmpty(remoteAddr) || proxyAddressSet.contains(remoteAddr)) {
			return false;
		} else {
			result.setDetectedAddress(remoteAddr);
		}
		
		if (remotePort <= 0) {
			return false;
		} else {
			result.setDetectedPort(remotePort);
		}
		
		result.setDetectionSuccess(true);
		return true;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderForwarded(final HttpServletRequest servletRequest, final AddressPortDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
		//https://tools.ietf.org/html/rfc7230#section-3.2
		final List<String> headerValues = Collections.list(servletRequest.getHeaders(HEADER_FORWARDED));
		for (int i = headerValues.size() - 1; i >= 0; i--) {
			final String[] subValues = headerValues.get(i).split(COMMA);
			for (int j = subValues.length - 1; j >= 0; j--) {
				
			}
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderXForwardedFor(final HttpServletRequest servletRequest, final AddressPortDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
		final List<String> headerValues = Collections.list(servletRequest.getHeaders(HEADER_X_FORWARDED_FOR));
		for (int i = headerValues.size() - 1; i >= 0; i--) {
			final String[] subValues = headerValues.get(i).split(COMMA);
			for (int j = subValues.length - 1; j >= 0; j--) {
				if (Utilities.isEmpty(subValues[j])) {
					return false;
				}
				
				final Entry<String, Integer> addressPort = processAddressAndPort(subValues[j]);
				final String address = networkAddressPreProcessor.normalize(addressPort.getKey());
				final Integer port = addressPort.getValue();
				
				if (port == null) {
					if (proxyAddressSet.contains(address)) {
						continue;
					}
					return false;
					
				}
				
				if (!proxyAddressSet.contains(address)) {
					result.setDetectionSuccess(true);
					result.setDetectedAddress(address);
					result.setDetectedPort(addressPort.getValue());
					return true;
				}				
			}
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Entry<String,Integer> processAddressAndPort(final String candidate) {
		String strPort = "";
		int strPortStartIdx = candidate.length();
		for (int i = candidate.length() - 1 ; i >= 0 ; i--) {
			final String character = String.valueOf(candidate.charAt(i));
			if (!NumberUtils.isCreatable(character)) {
				break;
			} else {
				strPort = character + strPort;
				strPortStartIdx = i;
			}
		}
		
		Integer port = null;
		if (!Utilities.isEmpty(strPort) && strPort.length() <= MAX_PORT_LENGTH) {		
			try {
				port = NumberUtils.createInteger(strPort);				
			} catch (final NumberFormatException ex) {
				logger.debug(ex);
			}
		}
		
		String address = candidate.substring(0, Math.max(0, strPortStartIdx - 1));
		address = address.replace(SQUARE_BRACKET_OPEN, "").replace(SQUARE_BRACKET_CLOSE, "");
		return Map.entry(address, port);
	}
}
