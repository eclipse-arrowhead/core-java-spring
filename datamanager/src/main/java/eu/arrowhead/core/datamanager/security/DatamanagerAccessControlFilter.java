/********************************************************************************
 * Copyright (c) 2020 {Lulea University of Technology}
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
package eu.arrowhead.core.datamanager.security;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

//import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.Utilities;
//import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.AuthException;

import java.util.Map;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class DatamanagerAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
        // members
        
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);

		final String cloudCN = getServerCloudCN();

		if (requestTarget.endsWith(CommonConstants.ECHO_URI)) {
      // Everybody in the local cloud can test the server => no further check is necessary
      return;
		} 

    // only the system named $SysName is allowed to write to <historian or proxy>/$SysName/$SrvName
    if (!method.toLowerCase().equals("get")) {
      final String dataManagerHistorianURI = CommonConstants.DATAMANAGER_URI + CommonConstants.OP_DATAMANAGER_HISTORIAN+"/";
      int sysNameStartPosition = requestTarget.indexOf(dataManagerHistorianURI);
      int sysNameStopPosition = -1;
      if ( sysNameStartPosition != -1) {
        sysNameStopPosition = requestTarget.indexOf("/", sysNameStartPosition + dataManagerHistorianURI.length());
        String requestTargetSystemName = requestTarget.substring(sysNameStartPosition + dataManagerHistorianURI.length(), sysNameStopPosition);

        checkIfRequesterSystemNameisEqualsWithClientNameFromCN(requestTargetSystemName, clientCN);
        return;
      }

      if ( sysNameStartPosition == -1) {
        final String dataManagerProxyURI = CommonConstants.DATAMANAGER_URI + CommonConstants.OP_DATAMANAGER_PROXY+"/";
        sysNameStartPosition = requestTarget.indexOf(dataManagerProxyURI);
        sysNameStopPosition = requestTarget.indexOf("/", sysNameStartPosition + dataManagerProxyURI.length());
        String requestTargetSystemName = requestTarget.substring(sysNameStartPosition + dataManagerProxyURI.length(), sysNameStopPosition);

        checkIfRequesterSystemNameisEqualsWithClientNameFromCN(requestTargetSystemName, clientCN);
        return;
      } else {
        throw new AuthException("Illegal request");
      }
    }

	}

	//-------------------------------------------------------------------------------------------------
        private void checkIfRequesterSystemNameisEqualsWithClientNameFromCN(final String requesterSystemName, final String clientCN) {
                final String clientNameFromCN = getClientNameFromCN(clientCN);
                
                if (Utilities.isEmpty(requesterSystemName) || Utilities.isEmpty(clientNameFromCN)) {
                        log.debug("Requester system name and client name from certificate do not match!");
                        throw new AuthException("Requester system name or client name from certificate is null or blank!", HttpStatus.UNAUTHORIZED.value());
                }
                
                if (!requesterSystemName.equalsIgnoreCase(clientNameFromCN) && !requesterSystemName.replaceAll("_", "").equalsIgnoreCase(clientNameFromCN)) {
                        log.debug("Requester system name and client name from certificate do not match!");
                        throw new AuthException("Requester system name(" + requesterSystemName + ") and client name from certificate (" + clientNameFromCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
                }
        }
        
        //-------------------------------------------------------------------------------------------------
        private String getClientNameFromCN(final String clientCN) {
                return clientCN.split("\\.", 2)[0];
        }
}
