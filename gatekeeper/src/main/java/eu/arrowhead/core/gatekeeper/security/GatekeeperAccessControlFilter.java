package eu.arrowhead.core.gatekeeper.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.security.AccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class GatekeeperAccessControlFilter extends AccessControlFilter {

	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		//TODO: implement gatekeeper specific logic here
		// DO NOT USE super.checkClientAuthorized because that implementation rejects any request outside of the cloud
	}
}