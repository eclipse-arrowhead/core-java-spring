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

package eu.arrowhead.core.qos;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.RelayTestService;
import eu.arrowhead.core.qos.service.event.EventWatcherService;

@Configuration
public class QoSMonitorTestContext {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public QoSDBService mockQoSDBService() {
		return Mockito.mock(QoSDBService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public PingService mockPingService() {
		return Mockito.mock(PingService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public RelayTestService mockRelayTestService() {
		return Mockito.mock(RelayTestService.class);
	}

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public EventWatcherService mockEventWatcherService() {
		return Mockito.mock(EventWatcherService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public CommonDBService mockCommonDBService() {
		return Mockito.mock(CommonDBService.class);
	}
}
