package eu.arrowhead.common.processor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.model.AddressPortDetectionResult;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@Component
public class AddressPortDetector {

	//=================================================================================================
	// methods
	
	@Value(CoreCommonConstants.$USE_ADDRESS_PORT_DETECTOR_WD)
	private boolean useDetector;
	
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
		boolean detected = detectByHeaderForwarded(servletRequest, result);
		if (!detected) {
			detected = detectByHeaderXForwarded(servletRequest, result);
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
}
