package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.AuthorizationIntraCloudCheckResponseDTO;
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
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of getAuthorizationIntraCloudEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationIntraCloudEntriesCallDB() {
		final int numOfEntries = 3;
		when(authorizationIntraCloudRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingAuthorizationIntraCloudRepository(numOfEntries));
		assertEquals(numOfEntries, authorizationDBService.getAuthorizationIntraCloudEntries(0, 10, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationIntraCloudEntriesWithNotValidSortField() {
		authorizationDBService.getAuthorizationIntraCloudEntries(0, 10, Direction.ASC, "notValid");
	}

	//=================================================================================================
	//Tests of getAuthorizationIntraCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationIntraCloudEntryByIdWithNotExistingId() {
		when(authorizationIntraCloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getAuthorizationIntraCloudEntryById(-1);
	}
	
	//=================================================================================================
	//Tests of removeAuthorizationIntraCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveAuthorizationIntraCloudEntryByIdWithNotExistingId() {
		when(authorizationIntraCloudRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeAuthorizationIntraCloudEntryById(1);
	}

	//=================================================================================================
	//Tests of createAuthorizationIntraCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateAuthorizationIntraCloudWithInvalidConsumerId() {
		authorizationDBService.createAuthorizationIntraCloud(0, 1, 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateAuthorizationIntraCloudWithInvalidProviderId() {
		authorizationDBService.createAuthorizationIntraCloud(1, 0, 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateAuthorizationIntraCloudWithInvalidServiceDefinitionId() {
		authorizationDBService.createAuthorizationIntraCloud(1, 1, 0);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testreateAuthorizationIntraCloudWithNotExistingSystem() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		
		authorizationDBService.createAuthorizationIntraCloud(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateAuthorizationIntraCloudWithNotExistingServiceDefintition() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(new System()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		
		authorizationDBService.createAuthorizationIntraCloud(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateAuthorizationIntraCloudWithDBConstraintViolation() {
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.of(new AuthorizationIntraCloud()));
		authorizationDBService.createAuthorizationIntraCloud(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateAuthorizationIntraCloudDBCall() {
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(authorizationIntraCloudRepository.saveAndFlush(any())).thenReturn(new AuthorizationIntraCloud(system, system, serviceDefinition));
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final AuthorizationIntraCloud entry = authorizationDBService.createAuthorizationIntraCloud(1, 1, 1);
		assertEquals(1000, entry.getConsumerSystem().getPort());
	}
	
	//=================================================================================================
	//Tests of createBulkAuthorizationIntraCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidConsumerId() {
		authorizationDBService.createBulkAuthorizationIntraCloud(0, createIdSet(1, 1), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidProviderId() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(-1, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithInvalidServiceDefinitionId() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 1), createIdSet(0, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithEmptyProviderIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, new HashSet<Long>(), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithNullProviderIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, null, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithEmptyServiceDefinitionIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), new HashSet<Long>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithNullServiceDefinitionIds() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkAuthorizationIntraCloudWithMultipleEmlementsInBothSet() {
		authorizationDBService.createBulkAuthorizationIntraCloudResponse(3, createIdSet(1, 2), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateBulkAuthorizationIntraCloudWithDBConstraintViolation() {
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.of(new AuthorizationIntraCloud()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final List<AuthorizationIntraCloud> entries = authorizationDBService.createBulkAuthorizationIntraCloud(1, createIdSet(1, 1), createIdSet(1, 2));
		assertTrue(entries.isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testCreateBulkAuthorizationIntraCloudDBCall() {
		final int numOfEntriesToBeSaved = 1;
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(authorizationIntraCloudRepository.saveAll(any())).thenReturn(List.of(new AuthorizationIntraCloud(system, system, serviceDefinition)));
		when(authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final List<AuthorizationIntraCloud> entries = authorizationDBService.createBulkAuthorizationIntraCloud(1, createIdSet(1, numOfEntriesToBeSaved), createIdSet(1, numOfEntriesToBeSaved));
		assertEquals(numOfEntriesToBeSaved, entries.size());
	}
	
	//=================================================================================================
	//Tests of checkAuthorizationIntraCloudRequest
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithInvalidConsumerId() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(0, 1, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNotExistingConsumer() {
		when(systemRepository.existsById(anyLong())).thenReturn(false);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(1, 1, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithInvalidServiceDefintitionId() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(1, 0, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNotExistingServiceDefintition() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.checkAuthorizationIntraCloudRequest(1, 1, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithEmptyProviderIdList() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(1, 1, new HashSet<Long>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testheckAuthorizationIntraCloudRequestWithNullProviderIdList() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkAuthorizationIntraCloudRequest(1, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckAuthorizationIntraCloudRequestWithNotExistingProvider() {
		final long consumerId = 1;
		final long providerId = 1;
		when(systemRepository.existsById(consumerId)).thenReturn(true);
		when(systemRepository.existsById(providerId)).thenReturn(false);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		
		authorizationDBService.checkAuthorizationIntraCloudRequest(1, 1, createIdSet((int) providerId, (int) providerId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckAuthorizationIntraCloudRequestDBCall() {
		final System consumer = new System("testConsumer", "address", 1000, null);
		consumer.setId(8);
		final System provider = new System("testProvider", "address", 2000, null);
		provider.setId(6);		
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		when(authorizationIntraCloudRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).
			 thenReturn(Optional.of(new AuthorizationIntraCloud(consumer, provider, new ServiceDefinition())));
		
		final AuthorizationIntraCloudCheckResponseDTO dto = authorizationDBService.checkAuthorizationIntraCloudRequest(1, 1, createIdSet(6, 6));
		assertTrue(dto.getProviderIdAuthorizationState().get(6L));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<AuthorizationIntraCloud> createPageForMockingAuthorizationIntraCloudRepository(final int numberOfRequestedEntry) {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		final List<AuthorizationIntraCloud> entries = new ArrayList<>(numberOfRequestedEntry);
		for (int i = 1; i <= numberOfRequestedEntry; ++i) {
			final System consumer = new System("Consumer" + i, i + "." + i + "." + i + "." + i, i * 1000, null);
			consumer.setId(i);
			final System provider = new System("Provider" + i, i + "." + i + "." + i + "." + i, i * 1000, null);
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
}
