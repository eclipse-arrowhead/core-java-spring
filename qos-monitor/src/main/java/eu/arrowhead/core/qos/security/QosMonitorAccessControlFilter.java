/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class QosMonitorAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForGetMeasurements = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER };
	private static final CoreSystem[] allowedCoreSystemsForExecuteRelayTests = { CoreSystem.GATEKEEPER };
	
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
		} else if (requestTarget.contains(CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT) ||
				   requestTarget.contains(CommonConstants.OP_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT) ||
				   requestTarget.contains(CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT) ||
				   requestTarget.contains(CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT)) {
			// Only the specified core systems can use these methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForGetMeasurements, requestTarget);
		} else if (requestTarget.contains(CommonConstants.OP_QOSMONITOR_INIT_RELAY_TEST_URI) ||
				   requestTarget.contains(CommonConstants.OP_QOSMONITOR_JOIN_RELAY_TEST_URI)) {
			// Only the specified core systems can use these methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForExecuteRelayTests, requestTarget);
		}
	}
}