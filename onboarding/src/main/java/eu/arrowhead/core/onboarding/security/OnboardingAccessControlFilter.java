/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.onboarding.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.filter.thirdparty.MultiReadRequestWrapper;
import eu.arrowhead.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true)
public class OnboardingAccessControlFilter extends CoreSystemAccessControlFilter {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            log.debug("Checking access in OnboardingAccessControlFilter...");
            try {
                final MultiReadRequestWrapper requestWrapper = new MultiReadRequestWrapper((HttpServletRequest) request);
                final String requestTarget = Utilities.stripEndSlash(requestWrapper.getRequestURL().toString());

                if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
                    // Only the local System Operator can use these methods
                    final String cloudCN = getServerCloudCN();
                    final String clientCN = SecurityUtilities.getCertificateCNFromRequest(requestWrapper);

                    checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
                }

                log.debug("Using MultiReadRequestWrapper in the filter chain from now...");
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
    @Override
    protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String, String[]> queryParams) {
        final String cloudCN = getServerCloudCN();
        if (requestTarget.contains(CoreCommonConstants.MGMT_URI)) {
            // Only the local System Operator can use these methods
            checkIfLocalSystemOperator(clientCN, cloudCN, requestTarget);
        }
    }
}