package eu.arrowhead.common.processor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.model.IpPortDetectionResult;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@Component
public class IpPortDetector {

	//=================================================================================================
	// methods
	
	@Value(CoreCommonConstants.$USE_IP_PORT_DETECTOR_WD)
	private boolean useDetector;
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	private static final String HEADER_FORWARDED = "forwarded";
	
	//-------------------------------------------------------------------------------------------------
	public IpPortDetectionResult detect(final HttpServletRequest servletReuest) { // TODO junit
		IpPortDetectionResult result = new IpPortDetectionResult();
		
		if (!useDetector || servletReuest == null) {
			result.setSkipped(true);
			result.setDetectionMessage("IP-Port detection process was skipped");
			return result;
		}		
		final Map<String, String> headerMap = createHeaderMap(servletReuest);
		
		boolean detected = detectByHeaderForwarded(headerMap, result);
		if (!detected) {
			detected = detectByHeaderXForwardedFor(headerMap, result);
		}
		
		if (detected) {
			result.setDetectedAddress(networkAddressPreProcessor.normalize(result.getDetectedAddress()));
			try {
				networkAddressVerifier.verify(result.getDetectedAddress());				
			} catch (final InvalidParameterException ex) {
				result.setDetectedSuccess(false);
				result.setDetectionMessage("Detected address is invalid! " + ex.getMessage());
				return result;
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderForwarded(final Map<String, String> headerMap, final IpPortDetectionResult result) {
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
		//https://tools.ietf.org/html/rfc7230#section-3.2
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean detectByHeaderXForwardedFor(final Map<String, String> headerMap, final IpPortDetectionResult result) {
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<String, String> createHeaderMap(final HttpServletRequest request) {
		final Map<String, String> headerMap = new HashMap<>();
		
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String key = (String) headerNames.nextElement();
            final String value = request.getHeader(key);
            headerMap.put(key.toLowerCase(), value);
        }
        
        return headerMap;
	}
}
