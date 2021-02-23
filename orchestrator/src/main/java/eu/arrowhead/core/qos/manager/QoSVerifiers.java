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

package eu.arrowhead.core.qos.manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.qos.manager.impl.PingRequirementsVerifier;
import eu.arrowhead.core.qos.manager.impl.ServiceTimeVerifier;

@Configuration
public class QoSVerifiers {
	
	//=================================================================================================
	// members
	
	public static final String SERVICE_TIME_VERIFIER = "serviceTimeVerifier";
	public static final String PING_REQUIREMENTS_VERIFIER = "pingRequirementsVerifier";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(SERVICE_TIME_VERIFIER)
	public QoSVerifier getServiceTimeVerifier() {
		return new ServiceTimeVerifier();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(PING_REQUIREMENTS_VERIFIER)
	public QoSVerifier getPingRequirementsVerifier() {
		return new PingRequirementsVerifier();
	}
}