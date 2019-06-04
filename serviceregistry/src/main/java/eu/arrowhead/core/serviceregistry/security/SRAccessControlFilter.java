package eu.arrowhead.core.serviceregistry.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.AccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class SRAccessControlFilter extends AccessControlFilter {
	
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON) {
		if (!Utilities.isKeyStoreCNArrowheadValid(clientCN)) {
			log.debug("{} is not a valid common name, access denied!", clientCN);
	        throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
		}
		
		final String cloudCN = getServerCloudCN();

		
	}
}