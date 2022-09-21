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

package eu.arrowhead.core.serviceregistry.quartz.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith (SpringRunner.class)
public class ProviderReachabilityTaskTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ProvidersReachabilityTask providersReachabilityTask = new ProvidersReachabilityTask();
	
	@Mock
	private ServiceRegistryDBService serviceRegistryDBService;
	
	private final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
	private final System testSystem = new System("testSystem", "testAddress*/$", null, 1, "testAuthenticationInfo", null);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		final List<ServiceRegistry> serviceRegistryEntries = new ArrayList<>();
		serviceRegistryEntries.add(new ServiceRegistry(serviceDefinition, testSystem, "testUri", ZonedDateTime.now(), ServiceSecurityType.TOKEN, "", 1));
		final Page<ServiceRegistry> sreviceRegistryEntriesPage = new PageImpl<ServiceRegistry>(serviceRegistryEntries);
		
		when(serviceRegistryDBService.getServiceRegistryEntries(anyInt(), anyInt(), eq(Direction.ASC), eq(CoreCommonConstants.COMMON_FIELD_NAME_ID))).thenReturn(sreviceRegistryEntriesPage);
		doNothing().when(serviceRegistryDBService).removeBulkOfServiceRegistryEntries(anyIterable());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckProvidersReachabilityConnectionFailure() {
		final List<ServiceRegistry> removedEntries = providersReachabilityTask.checkProvidersReachability();
		
		assertEquals(1, removedEntries.size());
	}
}