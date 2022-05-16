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

package eu.arrowhead.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.filter.InboundDebugFilter;
import eu.arrowhead.common.filter.OutboundDebugFilter;
import eu.arrowhead.common.filter.PayloadSizeFilter;

public class DefaultSecurityConfig extends WebSecurityConfigurerAdapter {
	
	//=================================================================================================
	// members

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	protected boolean sslEnabled;
	
	@Value(CoreCommonConstants.$LOG_ALL_REQUEST_AND_RESPONSE_WD)
	protected boolean debugMode;
	
	//=================================================================================================
	// assistant methods

    //-------------------------------------------------------------------------------------------------
	@Override
    protected void configure(final HttpSecurity http) throws Exception {
    	http.httpBasic().disable()
    	    .csrf().disable()
    	    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
    		.addFilterAfter(new PayloadSizeFilter(), SecurityContextHolderAwareRequestFilter.class);
    	
    	if (sslEnabled) {
    		http.requiresChannel().anyRequest().requiresSecure();
    	}
    	
    	if (debugMode) {
    		http.addFilterBefore(new OutboundDebugFilter(), ChannelProcessingFilter.class);
    		http.addFilterAfter(new InboundDebugFilter(), X509AuthenticationFilter.class);
    	}
    }
}