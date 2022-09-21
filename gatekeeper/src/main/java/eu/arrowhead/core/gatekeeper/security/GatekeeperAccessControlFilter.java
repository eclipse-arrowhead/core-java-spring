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

package eu.arrowhead.core.gatekeeper.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class GatekeeperAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForOrchestrationTasks = { CoreSystem.ORCHESTRATOR };
	private static final CoreSystem[] allowedCoreSystemsForChoreographerTasks = { CoreSystem.CHOREOGRAPHER };
	private static final CoreSystem[] allowedCoreSystemsForQoSTasks = { CoreSystem.QOSMONITOR };
	private static final CoreSystem[] allowedCoreSystemsForGeneralTasks = { CoreSystem.ORCHESTRATOR, CoreSystem.QOSMONITOR };
	
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
		} else if (requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_GSD_SERVICE) || requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_ICN_SERVICE)) {
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForOrchestrationTasks, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_MULTI_GSD_SERVICE)) {
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForChoreographerTasks, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_PULL_CLOUDS_SERVICE) || 
				   requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_SERVICE) ||
				   requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_COLLECT_ACCESS_TYPES_SERVICE) ||
				   requestTarget.endsWith(CommonConstants.OP_GATEKEEPER_RELAY_TEST_SERVICE)) {
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForQoSTasks, requestTarget);
		} else if (requestTarget.contains(CommonConstants.OP_GATEKEEPER_GET_CLOUD_SERVICE)) {
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForGeneralTasks, requestTarget);
		}
	}
}