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
import eu.arrowhead.common.database.entity.IntraCloudAuthorization;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.IntraCloudAuthorizationRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class AuthorizationDBServiceIntraCloudTest {
	
	//=================================================================================================
	// members
	@InjectMocks
	private AuthorizationDBService authorizationDBService;
	
	@Mock
	private IntraCloudAuthorizationRepository intraCloudAuthorizationRepository;
	
	@Mock
	private SystemRepository systemRepository;
	
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of getIntraCloudAuthorizationEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetIntraCloudAuthorizationEntriesCallDB() {
		final int numOfEntries = 3;
		when(intraCloudAuthorizationRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingIntraCloudAuthorizationRepository(numOfEntries));
		assertEquals(numOfEntries, authorizationDBService.getIntraCloudAuthorizationEntries(0, 10, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetIntraCloudAuthorizationEntriesWithNotValidSortField() {
		authorizationDBService.getIntraCloudAuthorizationEntries(0, 10, Direction.ASC, "notValid");
	}

	//=================================================================================================
	//Tests of getIntraCloudAuthorizationEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetIntraCloudAuthorizationEntryByIdWithNotExistingId() {
		when(intraCloudAuthorizationRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getIntraCloudAuthorizationEntryById(-1);
	}
	
	//=================================================================================================
	//Tests of removeIntraCloudAuthorizationEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveIntraCloudAuthorizationEntryByIdWithNotExistingId() {
		when(intraCloudAuthorizationRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeIntraCloudAuthorizationEntryById(1);
	}

	//=================================================================================================
	//Tests of createIntraCloudAuthorization
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithInvalidConsumerId() {
		authorizationDBService.createIntraCloudAuthorization(0, 1, 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithInvalidProviderId() {
		authorizationDBService.createIntraCloudAuthorization(1, 0, 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithInvalidServiceDefinitionId() {
		authorizationDBService.createIntraCloudAuthorization(1, 1, 0);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithNotExistingSystem() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		
		authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithNotExistingServiceDefintition() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(new System()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		
		authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithDBConstraintViolation() {
		when(intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.of(new IntraCloudAuthorization()));
		authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateIntraCloudAuthorizationResponseDBCall() {
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(intraCloudAuthorizationRepository.saveAndFlush(any())).thenReturn(new IntraCloudAuthorization(system, system, serviceDefinition));
		when(intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final IntraCloudAuthorization entry = authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
		assertEquals(1000, entry.getConsumerSystem().getPort());
	}
	
	//=================================================================================================
	//Tests of createBulkIntraCloudAuthorization
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithInvalidConsumerId() {
		authorizationDBService.createBulkIntraCloudAuthorization(0, createIdSet(1, 1), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithInvalidProviderId() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdSet(-1, 2), createIdSet(1, 1));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithInvalidServiceDefinitionId() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdSet(1, 1), createIdSet(0, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithEmptyProviderIds() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, new HashSet<Long>(), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithNullProviderIds() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, null, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithEmptyServiceDefinitionIds() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdSet(1, 2), new HashSet<Long>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithNullServiceDefinitionIds() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdSet(1, 2), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithMultipleEmlementsInBothSet() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdSet(1, 2), createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateBulkIntraCloudAuthorizationResponseWithDBConstraintViolation() {
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.of(new IntraCloudAuthorization()));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final List<IntraCloudAuthorization> entries = authorizationDBService.createBulkIntraCloudAuthorization(1, createIdSet(1, 1), createIdSet(1, 2));
		assertTrue(entries.isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testCreateBulkIntraCloudAuthorizationResponseDBCall() {
		final int numOfEntriesToBeSaved = 1;
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(intraCloudAuthorizationRepository.saveAll(any())).thenReturn(List.of(new IntraCloudAuthorization(system, system, serviceDefinition)));
		when(intraCloudAuthorizationRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final List<IntraCloudAuthorization> entries = authorizationDBService.createBulkIntraCloudAuthorization(1, createIdSet(1, numOfEntriesToBeSaved), createIdSet(1, numOfEntriesToBeSaved));
		assertEquals(numOfEntriesToBeSaved, entries.size());
	}
	
	//=================================================================================================
	//Tests of checkIntraCloudAuthorizationRequest
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithInvalidConsumerId() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkIntraCloudAuthorizationRequest(0, 1, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithNotExistingConsumer() {
		when(systemRepository.existsById(anyLong())).thenReturn(false);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkIntraCloudAuthorizationRequest(1, 1, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithInvalidServiceDefintitionId() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkIntraCloudAuthorizationRequest(1, 0, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithNotExistingServiceDefintition() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.checkIntraCloudAuthorizationRequest(1, 1, createIdSet(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithEmptyProviderIdList() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkIntraCloudAuthorizationRequest(1, 1, new HashSet<Long>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithNullProviderIdList() {
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkIntraCloudAuthorizationRequest(1, 1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCheckIntraCloudAuthorizationRequestResponseWithNotExistingProvider() {
		final long consumerId = 1;
		final long providerId = 1;
		when(systemRepository.existsById(consumerId)).thenReturn(true);
		when(systemRepository.existsById(providerId)).thenReturn(false);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		
		authorizationDBService.checkIntraCloudAuthorizationRequest(1, 1, createIdSet((int) providerId, (int) providerId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckIntraCloudAuthorizationRequestResponseDBCall() {
		final System consumer = new System("testConsumer", "address", 1000, null);
		consumer.setId(8);
		final System provider = new System("testProvider", "address", 2000, null);
		provider.setId(6);		
		when(systemRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).
			 thenReturn(Optional.of(new IntraCloudAuthorization(consumer, provider, new ServiceDefinition())));
		
		final IntraCloudAuthorizationCheckResponseDTO dto = authorizationDBService.checkIntraCloudAuthorizationRequest(1, 1, createIdSet(6, 6));
		assertTrue(dto.getProviderIdAuthorizationState().get(6L));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<IntraCloudAuthorization> createPageForMockingIntraCloudAuthorizationRepository(final int numberOfRequestedEntry) {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		final List<IntraCloudAuthorization> entries = new ArrayList<>(numberOfRequestedEntry);
		for (int i = 1; i <= numberOfRequestedEntry; ++i) {
			final System consumer = new System("Consumer" + i, i + "." + i + "." + i + "." + i, i * 1000, null);
			consumer.setId(i);
			final System provider = new System("Provider" + i, i + "." + i + "." + i + "." + i, i * 1000, null);
			provider.setId(i);
			final IntraCloudAuthorization entry = new IntraCloudAuthorization(consumer, provider, serviceDefinition);
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
