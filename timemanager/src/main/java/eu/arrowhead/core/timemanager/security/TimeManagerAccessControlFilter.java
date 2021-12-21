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
package eu.arrowhead.core.timemanager.security;

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

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;

import java.util.Map;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class TimeManagerAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
        // members
        
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);

		if (requestTarget.endsWith(CommonConstants.ECHO_URI)) {
                        // Everybody in the local cloud can test the server => no further check is necessary
                        return;
		} 

	}

}
