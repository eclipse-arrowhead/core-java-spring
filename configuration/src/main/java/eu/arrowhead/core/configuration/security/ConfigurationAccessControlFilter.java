/********************************************************************************
 * Copyright (c) 2021 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/
package eu.arrowhead.core.configuration.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class ConfigurationAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
    // members
	
    private final String configurationConfURI = CommonConstants.CONFIGURATION_URI + CommonConstants.OP_CONFIGURATION_CONF;
    private final String configurationRawConfURI = CommonConstants.CONFIGURATION_URI + CommonConstants.OP_CONFIGURATION_RAWCONF;
        
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
        } else if (requestTarget.contains(configurationConfURI) || requestTarget.contains(configurationRawConfURI)) {
             final String client = getSystemNameFromURI(requestTarget);
             checkIfRequesterSystemNameisEqualsWithClientNameFromCN(client, clientCN);
        }		
	}

    //-------------------------------------------------------------------------------------------------
    private String getSystemNameFromURI(final String requestTarget) {
        int sysNameStartPosition = requestTarget.indexOf(configurationConfURI);
        int sysNameStopPosition = -1;
        if (sysNameStartPosition != -1) {
            sysNameStopPosition = requestTarget.lastIndexOf("/");
            final String requestTargetSystemName = requestTarget.substring(sysNameStopPosition + 1);

            return requestTargetSystemName;
        }

        sysNameStartPosition = requestTarget.indexOf(configurationRawConfURI);
        if (sysNameStartPosition != -1) {
            sysNameStopPosition = requestTarget.lastIndexOf("/");
            final String requestTargetSystemName = requestTarget.substring(sysNameStopPosition + 1);                       
               
            return requestTargetSystemName;
        } else {
            throw new AuthException("Illegal request", HttpStatus.UNAUTHORIZED.value());
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