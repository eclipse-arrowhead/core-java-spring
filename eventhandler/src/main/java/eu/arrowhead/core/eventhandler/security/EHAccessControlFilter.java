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

package eu.arrowhead.core.eventhandler.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class EHAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final CoreSystem[] allowedCoreSystemsForPublishAuthUpdate = { CoreSystem.AUTHORIZATION };
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		
		if (requestTarget.endsWith(CommonConstants.ECHO_URI)) {
			// Everybody in the local cloud can test the server => no further check is necessary
		} else if ( requestTarget.contains( CoreCommonConstants.MGMT_URI ) ) {
			// Only the local System Operator can use these methods
			checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_EVENTHANDLER_PUBLISH_AUTH_UPDATE)) {
			// Only the specified core systems can use these methods
			checkIfClientIsAnAllowedCoreSystem(clientCN, cloudCN, allowedCoreSystemsForPublishAuthUpdate, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_EVENTHANDLER_SUBSCRIBE)) {
			final SubscriptionRequestDTO subscriptionRequestDTO = Utilities.fromJson(requestJSON, SubscriptionRequestDTO.class);
			checkIfRequesterSystemNameisEqualsWithClientNameFromCN(subscriptionRequestDTO.getSubscriberSystem().getSystemName(), clientCN);				
		} else if (requestTarget.endsWith(CommonConstants.OP_EVENTHANDLER_PUBLISH)) {
			final EventPublishRequestDTO eventPublishRequestDTO = Utilities.fromJson(requestJSON, EventPublishRequestDTO.class);
			checkIfRequesterSystemNameisEqualsWithClientNameFromCN(eventPublishRequestDTO.getSource().getSystemName(), clientCN);				
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkIfRequesterSystemNameisEqualsWithClientNameFromCN(final String requesterSystemName, final String clientCN) {
		final String clientNameFromCN = getClientNameFromCN(clientCN);
		
		if (Utilities.isEmpty(requesterSystemName) || Utilities.isEmpty(clientNameFromCN)) {
			log.debug("Requester system name and client name from certificate do not match!");
			throw new AuthException("Requester system name or client name from certificate is null or blank!", HttpStatus.UNAUTHORIZED.value());
		}
		
		if (!requesterSystemName.equalsIgnoreCase(clientNameFromCN)) {
			log.debug("Requester system name and client name from certificate do not match!");
			throw new AuthException("Requester system name(" + requesterSystemName + ") and client name from certificate (" + clientNameFromCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}