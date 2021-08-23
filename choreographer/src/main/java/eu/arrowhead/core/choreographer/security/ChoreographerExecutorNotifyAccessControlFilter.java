/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.filter.ArrowheadFilter;
import eu.arrowhead.common.filter.thirdparty.MultiReadRequestWrapper;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;

public class ChoreographerExecutorNotifyAccessControlFilter extends ArrowheadFilter {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ChoreographerSessionDBService sessionDBService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof MultiReadRequestWrapper) { // this filter runs AFTER the ChoreographerAccessControlFilter which has already done the request conversion 
			log.debug("Checking access in ChoreographerExecutorNotifyAccessControlFilter...");
			try {
				final MultiReadRequestWrapper requestWrapper = (MultiReadRequestWrapper) request;
				final String requestTarget = Utilities.stripEndSlash(requestWrapper.getRequestURL().toString());
				
				if (requestTarget.endsWith(CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE)) {
					final String clientCN = getCertificateCNFromRequest(requestWrapper);
					if (clientCN == null) {
						log.error("Unauthorized access: {}", requestTarget);
						throw new AuthException("Unauthorized access: " + requestTarget);
					}
					
					final String requestJSON = requestWrapper.getCachedBody();
					checkClientAuthorized(clientCN, requestJSON, requestTarget);
				}
				
				chain.doFilter(requestWrapper, response);
			} catch (final ArrowheadException ex) {
				handleException(ex, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkClientAuthorized(final String clientCN, final String body, final String requestTarget) {
		log.debug("checkClientAuthorized started...");
		
		final ChoreographerExecutedStepResultDTO payload = Utilities.fromJson(body, ChoreographerExecutedStepResultDTO.class);
		if (payload.getSessionStepId() == null || payload.getSessionStepId() <= 0) {
			log.debug("Session step id is not set in the payload when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		try {
			final ChoreographerSessionStep sessionStep = sessionDBService.getSessionStepById(payload.getSessionStepId());
			final ChoreographerExecutor executor = sessionStep.getExecutor();
			final String clientName = getClientNameFromCN(clientCN);
			
			if (!executor.getName().equalsIgnoreCase(clientName)) {
				log.debug("Executor system name ({}) and certificate common name ({}) do not match!", executor.getName(), clientName);
				throw new AuthException("Executor system name (" + executor.getName() + ") and certificate common name (" + clientName + ") do not match!", HttpStatus.UNAUTHORIZED.value());
			}
		} catch (final AuthException ex) {
			throw ex;
		} catch (final Exception ex) {
			log.error("Unauthorized access: {}", requestTarget);
			log.debug(ex);
			throw new AuthException("Unauthorized access: " + requestTarget);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Nullable
	private String getCertificateCNFromRequest(final HttpServletRequest request) {
		return SecurityUtilities.getCertificateCNFromRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}