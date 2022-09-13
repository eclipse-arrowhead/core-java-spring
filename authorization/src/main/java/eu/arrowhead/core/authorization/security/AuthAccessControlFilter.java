/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

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

	private static final String AUTHORIZATION_INTRA_CLOUD_MGMT_URI = CoreCommonConstants.MGMT_URI + "/intracloud";
	private static final CoreSystem[] allowedCoreSystemsForChecks = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER };
	private static final CoreSystem[] allowedCoreSystemsForTokenGenerations = { CoreSystem.ORCHESTRATOR, CoreSystem.CHOREOGRAPHER };
	private static final CoreSystem[] allowedCoreSystemsForSubscriptionChecks = { CoreSystem.EVENTHANDLER };
	private static final CoreSystem[] allowedCoreSystemsForRuleMgmt = { CoreSystem.ONBOARDINGCONTROLLER, CoreSystem.MSCV };

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		if (requestTarget.endsWith(CommonConstants.OP_AUTH_KEY_URI) || requestTarget.endsWith(CommonConstants.ECHO_URI)) {
			// Everybody in the local cloud can get the Authorization public key (because it is PUBLIC) or test the server => no further check is necessary
		} else if (requestTarget.endsWith(AUTHORIZATION_INTRA_CLOUD_MGMT_URI)) {
			// onboarding controller may use this method (TODO: should be a dedicated endpoint for that)
			if (!checkIfClientIsAnAllowedCoreSystemNoException(clientCN, cloudCN, allowedCoreSystemsForRuleMgmt, requestTarget)) {
				checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
			}
		} else if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_AUTH_TOKEN_URI) || requestTarget.endsWith(CommonConstants.OP_AUTH_TOKEN_MULTI_SERVICE_URI)) {
			// Only the specified core systems can use this methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForTokenGenerations, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_AUTH_INTRA_CHECK_URI) || requestTarget.endsWith(CommonConstants.OP_AUTH_INTER_CHECK_URI)) {
			// Only the specified core systems can use all these methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForChecks, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_AUTH_SUBSCRIPTION_CHECK_URI)) {
			// Only the specified core systems can use this method
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForSubscriptionChecks, requestTarget);
		}
	}
}