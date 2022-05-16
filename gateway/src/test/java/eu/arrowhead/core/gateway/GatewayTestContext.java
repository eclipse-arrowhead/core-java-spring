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

package eu.arrowhead.core.gateway;


import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.gateway.service.GatewayService;

@Configuration
public class GatewayTestContext {
	

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public GatewayService mockGatewayService() {
		return Mockito.mock(GatewayService.class);
    }
    
	//-------------------------------------------------------------------------------------------------

	@Bean
	@Primary // This bean is primary only in test context
	public CommonDBService mockCommonDBService() {
		return Mockito.mock(CommonDBService.class);
	}
}