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

package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.AuthorizationInterCloud;
import eu.arrowhead.common.database.entity.AuthorizationInterCloudInterfaceConnection;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.AuthorizationInterCloudInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.AuthorizationInterCloudRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class AuthorizationDBServiceInterCloudTest {
	
	//=================================================================================================
	// members
	@InjectMocks
	private AuthorizationDBService authorizationDBService;
	
	@Mock
	private AuthorizationInterCloudRepository authorizationInterCloudRepository;
	
	@Mock
	private CloudRepository cloudRepository;
	
	@Mock
	private SystemRepository systemRepository;
	
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Mock
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Mock
	private AuthorizationInterCloudInterfaceConnectionRepository authorizationInterCloudInterfaceConnectionRepository;
	
	private static final ZonedDateTime zdTime = Utilities.parseUTCStringToLocalZonedDateTime("2222-12-12T12:00:00Z");
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of getAuthorizationInterCloudEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationInterCloudEntriesCallDB() {
		final int numOfEntries = 3;
		when(authorizationInterCloudRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingAuthorizationInterCloudRepository(numOfEntries));
		assertEquals(numOfEntries, authorizationDBService.getAuthorizationInterCloudEntries(0, 10, Direction.ASC, CoreCommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationInterCloudEntriesWithNotValidSortField() {
		authorizationDBService.getAuthorizationInterCloudEntries(0, 10, Direction.ASC, "notValid");
	}

	//=================================================================================================
	// Tests of getAuthorizationInterCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationInterCloudEntryByIdWithNotExistingId() {
		when(authorizationInterCloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getAuthorizationInterCloudEntryById(-1);
	}
	
	//=================================================================================================
	// Tests of removeAuthorizationInterCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveAuthorizationInterCloudEntryByIdWithNotExistingId() {
		when(authorizationInterCloudRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeAuthorizationInterCloudEntryById(1);
	}

	//=================================================================================================
	// Tests of createBulkAuthorizationInterCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithInvalidCloudId() {
		authorizationDBService.createBulkAuthorizationInterCloud(-1L, Set.of(1L), Set.of(1L), Set.of(1L));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithInvalidProviderId() {
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(-1L), Set.of(1L), Set.of(1L));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNullProviderIdSet() {
		authorizationDBService.createBulkAuthorizationInterCloud(1L, null, Set.of(1L), Set.of(1L));	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithInvalidServiceDefinitionId() {
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), Set.of(-1L), Set.of(1L));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNullServiceDefinitionIdSet() {
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), null, Set.of(1L));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithInvalidInterfaceId() {
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), Set.of(1L), Set.of(-1L));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNullInterfaceIdSet() {
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), Set.of(1L), null);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNotExistingCloud() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(authorizationInterCloudRepository.findByCloudAndProviderAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		
		authorizationDBService.createBulkAuthorizationInterCloud(Long.MAX_VALUE, Set.of(1L), Set.of(1L), Set.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNotExistingProvder() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceInterface()));
		when(authorizationInterCloudRepository.findByCloudAndProviderAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(authorizationInterCloudInterfaceConnectionRepository.save(any())).thenReturn(new AuthorizationInterCloudInterfaceConnection());
		
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(Long.MAX_VALUE), Set.of(1L), Set.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNotExistingServiceDefintition() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(new System()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceInterface()));
		when(authorizationInterCloudRepository.findByCloudAndProviderAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(authorizationInterCloudInterfaceConnectionRepository.save(any())).thenReturn(new AuthorizationInterCloudInterfaceConnection());
		
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), Set.of(Long.MAX_VALUE), Set.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationInterCloudWithNotExistingInterface() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(new System()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(authorizationInterCloudRepository.findByCloudAndProviderAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(authorizationInterCloudInterfaceConnectionRepository.save(any())).thenReturn(new AuthorizationInterCloudInterfaceConnection());
		
		authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), Set.of(1L), Set.of(Long.MAX_VALUE));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateBulkAuthorizationInterCloudDBCall() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		serviceDefinition.setId(1);
		serviceDefinition.setCreatedAt(zdTime);
		serviceDefinition.setUpdatedAt(zdTime);
		
		final List<AuthorizationInterCloud> entriesToSave = createPageForMockingAuthorizationInterCloudRepository(1).getContent();
		when(authorizationInterCloudRepository.saveAll(any())).thenReturn(entriesToSave);
		when(authorizationInterCloudRepository.findByCloudAndProviderAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getValidTestCloud()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(entriesToSave.get(0).getProvider()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceInterface()));
		
		final List<AuthorizationInterCloud> entry = authorizationDBService.createBulkAuthorizationInterCloud(1L, Set.of(1L), Set.of(1L), Set.of(1L));
		assertEquals(getValidTestCloud().getName(), entry.get(0).getCloud().getName());
	}
	
	//=================================================================================================
	// Tests of checkAuthorizationInterCloudResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithNullCloudOperator() {
		authorizationDBService.checkAuthorizationInterCloudResponse(null, "testName", "testService", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithBlankCloudOperator() {
		authorizationDBService.checkAuthorizationInterCloudResponse("", "testName", "testService", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithNullCloudName() {
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", null, "testService", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithBlankCloudName() {
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "", "testService", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithNotExistingCloud() {
		when(cloudRepository.findByOperatorAndName(any(), any())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findByServiceDefinition(any())).thenReturn(Optional.of(new ServiceDefinition()));
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", "testService", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithNullServiceDefintition() {
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", null, createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithBlankServiceDefintition() {
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", "", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithNotExistingServiceDefintition() {
		when(cloudRepository.findByOperatorAndName(any(), any())).thenReturn(Optional.of(new Cloud()));
		when(serviceDefinitionRepository.findByServiceDefinition(any())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", "testService", createListOfIdIdLists(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithEmptyProviderInterfacesList() {
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", "testService", createListOfIdIdLists(1, 0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationInterCloudResponseWithNullProviderInterfacesList() {
		authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", "testService", null);
	}
	

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckAuthorizationInterCloudResponseDBCall() {
		final Cloud cloud = new Cloud();
		cloud.setOperator("testOperator");
		cloud.setName("testname");
		cloud.setCreatedAt(ZonedDateTime.now());
		cloud.setUpdatedAt(ZonedDateTime.now());
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition();
		serviceDefinition.setServiceDefinition("testService");
		
		when(cloudRepository.findByOperatorAndName(any(), any())).thenReturn(Optional.of(cloud));
		when(serviceDefinitionRepository.findByServiceDefinition(any())).thenReturn(Optional.of(serviceDefinition));
		when(systemRepository.existsById(any())).thenReturn(true);
		when(authorizationInterCloudRepository.findByCloudIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.ofNullable(null));
		
		final AuthorizationInterCloudCheckResponseDTO dto = authorizationDBService.checkAuthorizationInterCloudResponse("testOperator", "testName", "testService", createListOfIdIdLists(1, 2));
		assertTrue(dto.getAuthorizedProviderIdsWithInterfaceIds().isEmpty());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<AuthorizationInterCloud> createPageForMockingAuthorizationInterCloudRepository(final int numberOfRequestedEntry) {
		final List<AuthorizationInterCloud> entries = new ArrayList<>(numberOfRequestedEntry);
		final Cloud cloud = getValidTestCloud();
		final System provider = new System("testSystem", "testAddr", AddressType.HOSTNAME, 2000, "TOKEN", null);
		provider.setId(1);
		for (int i = 1; i <= numberOfRequestedEntry; ++i) {			
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService" + i);
			serviceDefinition.setId(i);
			final AuthorizationInterCloud entry = new AuthorizationInterCloud(cloud, provider, serviceDefinition);
			entry.setId(i);
			entry.setInterfaceConnections(new HashSet<>());
			entries.add(entry);
		}
		
		return new PageImpl<>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Cloud getValidTestCloud() {
		final boolean secure = true;
		final boolean neighbor = false;
		final boolean ownCloud = true;
		
		final Cloud cloud = new Cloud("testOperator", "testCloudName", secure, neighbor, ownCloud, null);
		cloud.setId(1);
		cloud.setCreatedAt(zdTime);
		cloud.setUpdatedAt(zdTime);

		return cloud;
	}	
	
	//-------------------------------------------------------------------------------------------------
	private List<IdIdListDTO> createListOfIdIdLists(final int numberOfIds, final int sizeOfIdLists) {
		final List<IdIdListDTO> ret = new ArrayList<>();
		for (long id = 1; id <= numberOfIds; ++id) {
			final List<Long> idList = new ArrayList<>();
			for (long j = 1; j <= sizeOfIdLists; ++j) {
				idList.add(j);
			}
			ret.add(new IdIdListDTO(id, idList));
		}
		return ret;
	}
}