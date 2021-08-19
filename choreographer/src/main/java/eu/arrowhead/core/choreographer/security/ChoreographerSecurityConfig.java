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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;

import eu.arrowhead.common.security.DefaultSecurityConfig;

@Configuration
@EnableWebSecurity
public class ChoreographerSecurityConfig extends DefaultSecurityConfig {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ApplicationContext appContext;
	
	//=================================================================================================
	// assistant methods
	
    //-------------------------------------------------------------------------------------------------
	@Override
    protected void configure(final HttpSecurity http) throws Exception {
		super.configure(http);
		
    	if (sslEnabled) {
    		final ChoreographerAccessControlFilter baseFilter = appContext.getAutowireCapableBeanFactory().createBean(ChoreographerAccessControlFilter.class);
    		http.addFilterAfter(baseFilter, X509AuthenticationFilter.class);
    		final ChoreographerExecutorNotifyAccessControlFilter specialFilter = appContext.getAutowireCapableBeanFactory().createBean(ChoreographerExecutorNotifyAccessControlFilter.class);
    		http.addFilterAfter(specialFilter, ChoreographerAccessControlFilter.class);
    	}
    }
}