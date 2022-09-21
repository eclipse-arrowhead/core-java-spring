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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class AuthorizationDBServiceIntraCloudTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private AuthorizationDBService authorizationDBService;
	
	@Mock
	private AuthorizationIntraCloudRepository authorizationIntraCloudRepository;
	
	@Mock
	private SystemRepository systemRepository;
	
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Mock
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Mock
	private AuthorizationIntraCloudInterfaceConnectionRepository authorizationIntraCloudInterfaceConnectionRepository;
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of getAuthorizationIntraCloudEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationIntraCloudEntriesCallDB() {
		final int numOfEntries = 3;
		when(authorizationIntraCloudRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingAuthorizationIntraCloudRepository(numOfEntries));
		assertEquals(numOfEntries, authorizationDBService.getAuthorizationIntraCloudEntries(0, 10, Direction.ASC, CoreCommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationIntraCloudEntriesWithNotValidSortField() {
		authorizationDBService.getAuthorizationIntraCloudEntries(0, 10, Direction.ASC, "notValid");
	}

	//=================================================================================================
	// Tests of getAuthorizationIntraCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationIntraCloudEntryByIdWithNotExistingId() {
		when(authorizationIntraCloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getAuthorizationIntraCloudEntryById(-1);
	}
	
	//=================================================================================================
	// Tests of removeAuthorizationIntraCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveAuthorizationIntraCloudEntryByIdWithNotExistingId() {
		when(authorizationIntraCloudRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeAuthorizationIntraCloudEntryById(1);
	}
	
	//=================================================================================================
	// Tests of createBulkAuthorizationIntraCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidConsumerId() {
		authorizationDBService.createBulkAuthorizationIntraCloud(0, createIdSet(1, 1), createIdSet(1, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidProviderId() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(-1, 2), createIdSet(1, 1), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidServiceDefinitionId() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 1), createIdSet(0, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidInterfaceId() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 1), createIdSet(1, 1), createIdSet(0, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithEmptyProviderIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, new HashSet<Long>(), createIdSet(1, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithNullProviderIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, null, createIdSet(1, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithEmptyServiceDefinitionIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), new HashSet<Long>(), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithNullServiceDefinitionIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), null, createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithEmptyInterfaceIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), createIdSet(1, 1), new HashSet<Long>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithNullInterfaceIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), createIdSet(1, 1), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithMultipleEmlementsInProviderAndServiceDefinitionSets() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), createIdSet(1, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithMultipleEmlementsInServiceDefinitionAndInterfaceSets() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 1), createIdSet(1, 2), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateBulkAuthorizationIntraCloudWithDBConstraintViolation() {
		final System system = new System("test", "0.0.0.0", AddressType.IPV4, 1000, null, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.of(new AuthorizationIntraCloud()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.of(serviceInterface));
		
		final List<AuthorizationIntraCloud> entries = authorizationDBService.createBulkAuthorizationIntraCloud(1, createIdSet(1, 1), createIdSet(1, 2), createIdSet(1, 1));
		assertTrue(entries.isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testCreateBulkAuthorizationIntraCloudDBCall() {
		final int numOfEntriesToBeSaved = 1;
		final System system = new System("test", "0.0.0.0", AddressType.IPV4, 1000, null, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		when(authorizationIntraCloudRepository.saveAll(any())).thenReturn(List.of(new AuthorizationIntraCloud(system, system, serviceDefinition)));
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		when(serviceInterfaceRepository.findById(anyLong())).thenReturn(Optional.of(serviceInterface));
		
		final List<AuthorizationIntraCloud> entries = authorizationDBService.createBulkAuthorizationIntraCloud(1, createIdSet(1, numOfEntriesToBeSaved), createIdSet(1, numOfEntriesToBeSaved),
																											   createIdSet(1, 1));
		assertEquals(numOfEntriesToBeSaved, entries.size());
	}
	
	//=================================================================================================
	// Tests of checkAuthorizationIntraCloudRequest
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithInvalidServiceDefintitionId() {
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 4200, 0, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNotExistingServiceDefintition() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 4200, 0, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testheckAuthorizationIntraCloudRequestWithNullProviderIdList() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 4200, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithEmptyProviderIdList() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 4200, 1, createSetOfIdIdLists(2, 2));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNullConsumerName() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(null, "127.0.0.1", 4200, 1, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithEmptyConsumerName() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(" ", "127.0.0.1", 4200, 1, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNullConsumerAddress() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", null, 4200, 1, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithEmptyConsumerAddress() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "", 4200, 1, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithTooLowConsumerPort() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", -4200, 1, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithTooHighConsumerPort() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 420000, 1, createSetOfIdIdLists(2, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNotExistedConsumer() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.empty());
		authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 4200, 1, createSetOfIdIdLists(2, 2));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckAuthorizationIntraCloudRequestDBCall() {
		final System provider = new System("testProvider", "address", AddressType.HOSTNAME, 2000, null, null);
		final long providerId = 6;
		provider.setId(providerId);		
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		final long consumerId = 8;
		final System consumer = new System("consumer", "127.0.0.1", AddressType.IPV4, 4200, null, null);
		consumer.setId(consumerId);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(String.class), any(String.class), anyInt())).thenReturn(Optional.of(consumer));
		when(authorizationIntraCloudRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).
			 																						thenReturn(Optional.of(new AuthorizationIntraCloud(consumer, provider, new ServiceDefinition())));
		
		final AuthorizationIntraCloudCheckResponseDTO dto = authorizationDBService.checkAuthorizationIntraCloudRequest("consumer", "127.0.0.1", 4200, 1, createSetOfIdIdLists(6, 2));
		assertEquals(consumerId, (long) dto.getConsumer().getId()); 
		assertEquals(0, dto.getAuthorizedProviderIdsWithInterfaceIds().size());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<AuthorizationIntraCloud> createPageForMockingAuthorizationIntraCloudRepository(final int numberOfRequestedEntry) {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		final List<AuthorizationIntraCloud> entries = new ArrayList<>(numberOfRequestedEntry);
		for (int i = 1; i <= numberOfRequestedEntry; ++i) {
			final System consumer = new System("Consumer" + i, i + "." + i + "." + i + "." + i, AddressType.IPV4, i * 1000, null, null);
			consumer.setId(i);
			final System provider = new System("Provider" + i, i + "." + i + "." + i + "." + i, AddressType.IPV4, i * 1000, null, null);
			provider.setId(i);
			final AuthorizationIntraCloud entry = new AuthorizationIntraCloud(consumer, provider, serviceDefinition);
			entry.setId(i);
			entries.add(entry);
		}
		
		return new PageImpl<>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<Long> createIdSet(final int firstNum, final int lastNum) {
		final Set<Long> idSet = new HashSet<>();
		for (int i = firstNum; i <= lastNum; ++i) {
			idSet.add((long) i);
		}
		
		return idSet;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<IdIdListDTO> createSetOfIdIdLists(final int numberOfIds, final int sizeOfIdLists) {
		final Set<IdIdListDTO> ret = new HashSet<>();
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