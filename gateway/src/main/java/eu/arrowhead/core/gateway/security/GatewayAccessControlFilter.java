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

package eu.arrowhead.core.gateway.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class GatewayAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForChecks = { CoreSystem.GATEKEEPER };
	private static final CoreSystem[] allowedCoreSystemsForCloseActiveSessions = { CoreSystem.CHOREOGRAPHER };
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		if (requestTarget.endsWith(CommonConstants.ECHO_URI)) {
			// Everybody in the local cloud can test the server => no further check is necessary
		} else if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_GATEWAY_KEY_URI) || requestTarget.endsWith(CommonConstants.OP_GATEWAY_CONNECT_PROVIDER_URI) ||
				   requestTarget.endsWith(CommonConstants.OP_GATEWAY_CONNECT_CONSUMER_URI)) {
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForChecks, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_GATEWAY_CLOSE_SESSIONS)) {
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForCloseActiveSessions, requestTarget);
		}
	}
}