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

package eu.arrowhead.core.choreographer.security;

import java.util.Map;

import org.springframework.http.HttpStatus;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

public class ChoreographerAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		
		if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_REGISTER)) {
			// An executor only register itself
			checkExecutorAccessToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER)) {
			// An executor only unregister itself
			checkExecutorAccessToDeregister(clientCN, queryParams, requestTarget);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkExecutorAccessToRegister(final String clientCN, final String requestJSON, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		final ChoreographerExecutorRequestDTO requestBody = Utilities.fromJson(requestJSON, ChoreographerExecutorRequestDTO.class);
		final String executorName = requestBody.getSystem() != null ? requestBody.getSystem().getSystemName() : "";
		if (Utilities.isEmpty(executorName)) {
			log.debug("Executor name is not set in the body when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
	
		if (!executorName.equalsIgnoreCase(clientName)) {
			log.debug("Executor system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Executor system name (" + executorName + ") and certificate common name (" + clientName + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkExecutorAccessToDeregister(final String clientCN, final Map<String,String[]> queryParams, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		
		final String executorName = queryParams.getOrDefault(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_NAME, new String[] { "" })[0];
		if (Utilities.isEmpty(executorName)) {
			log.debug("Executor name is not set in the query parameters when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!executorName.equalsIgnoreCase(clientName)) {
			log.debug("Executor name and certificate common name do not match! Unregistering denied!");
			throw new AuthException("Executor name (" + executorName + ") and certificate common name (" + clientName + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}