package eu.arrowhead.common.processor;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

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
	private Set<String> proxyAddressSet = new HashSet<>();
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	private static final String HEADER_FORWARDED = "forwarded";
	
	//-------------------------------------------------------------------------------------------------
	public AddressPortDetectionResult detect(final HttpServletRequest servletRequest) { // TODO junit
		AddressPortDetectionResult result = new AddressPortDetectionResult();
		
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
			detected = detectByHeaderXForwarded(servletRequest, result);
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
		for (String addr : proxyAddressList) {
			proxyAddressSet.add(networkAddressPreProcessor.normalize(addr));
		}
	}

	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderForwarded(final HttpServletRequest servletRequest, final AddressPortDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
		//https://tools.ietf.org/html/rfc7230#section-3.2
		final Enumeration<String> headers = servletRequest.getHeaders(HEADER_FORWARDED);
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderXForwarded(final HttpServletRequest servletRequest, final AddressPortDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Host
		return false;
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
		
		return true;
	}	
}
