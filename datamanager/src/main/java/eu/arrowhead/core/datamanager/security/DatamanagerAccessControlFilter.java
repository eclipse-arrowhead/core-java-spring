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

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class DatamanagerAccessControlFilter extends CoreSystemAccessControlFilter {


	//=================================================================================================
    // members
	
	private final Logger logger = LogManager.getLogger(DatamanagerAccessControlFilter.class);
	
	@Autowired
	private DatamanagerACLFilter dataManagerACLFilter;
        
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

		try {
            if (dataManagerACLFilter.checkRequest(clientCN, method, requestTarget)) {
            	logger.debug("Authorized");
            } else {
                logger.debug("Unauthorized!");
                throw new AuthException("Not authorized");
            }
		} catch (final AuthException e) {
			throw e;
        } catch (final Exception e) {
        	throw new AuthException("Error during authorization");
        }
	}
}