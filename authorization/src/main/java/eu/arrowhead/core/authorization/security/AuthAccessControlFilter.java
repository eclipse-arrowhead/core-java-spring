package eu.arrowhead.core.authorization.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class AuthAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForChecks = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER };
	private static final CoreSystem[] allowedCoreSystemsForSubscriptionChecks = { CoreSystem.EVENT_HANDLER };
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		if (requestTarget.endsWith(CommonConstants.OP_AUTH_KEY_URI) || requestTarget.endsWith(CommonConstants.ECHO_URI)) {
			// Everybody in the local cloud can get the Authorization public key (because it is PUBLIC) or test the server => no further check is necessary
		} else if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_AUTH_TOKEN_URI) || requestTarget.endsWith(CommonConstants.OP_AUTH_INTRA_CHECK_URI) ||
				   requestTarget.endsWith(CommonConstants.OP_AUTH_INTER_CHECK_URI)) {
			// Only the specified core systems can use all the other methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForChecks, requestTarget);
		}else if (requestTarget.endsWith(CommonConstants.OP_AUTH_SUBSCRIPTION_CHECK_URI)) {
			// Only the specified core systems can use all the other methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForSubscriptionChecks, requestTarget);
		}
	}
}