package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import eu.arrowhead.common.dto.IntraCloudAuthorizationListResponseDTO;
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
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getIntraCloudAuthorizationEntries
	
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
	

	//-------------------------------------------------------------------------------------------------
	//Tests of getIntraCloudAuthorizationEntryById
	
	@Test (expected = InvalidParameterException.class)
	public void testGetIntraCloudAuthorizationEntryByIdWithNotExistingId() {
		when(intraCloudAuthorizationRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getIntraCloudAuthorizationEntryById(-1);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of removeIntraCloudAuthorizationEntryById
	
	@Test (expected = InvalidParameterException.class)
	public void testRemoveIntraCloudAuthorizationEntryByIdWithNotExistingId() {
		when(intraCloudAuthorizationRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeIntraCloudAuthorizationEntryById(1);
	}

	//-------------------------------------------------------------------------------------------------
	//Tests of createIntraCloudAuthorization
	
	@Test (expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithInvalidConsumerId() {
		authorizationDBService.createIntraCloudAuthorization(0, 1, 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithInvalidProviderId() {
		authorizationDBService.createIntraCloudAuthorization(1, 0, 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithInvalidServiceDefinitionId() {
		authorizationDBService.createIntraCloudAuthorization(1, 1, 0);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithNotExistingSystem() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithNotExistingServiceDefintition() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(new System()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateIntraCloudAuthorizationResponseWithDBConstraintViolation() {
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(new IntraCloudAuthorization()));
		authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateIntraCloudAuthorizationResponseDBCall() {
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(intraCloudAuthorizationRepository.saveAndFlush(any())).thenReturn(new IntraCloudAuthorization(system, system, serviceDefinition));
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final IntraCloudAuthorization entry = authorizationDBService.createIntraCloudAuthorization(1, 1, 1);
		assertEquals(1000, entry.getConsumerSystem().getPort());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of createBulkIntraCloudAuthorizationResponse
	
	@Test (expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithInvalidConsumerId() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(0, createIdList(1, 2), createIdList(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithInvalidProviderId() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdList(-1, 2), createIdList(1, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateBulkIntraCloudAuthorizationResponseWithInvalidServiceDefinitionId() {
		authorizationDBService.createBulkIntraCloudAuthorizationResponse(3, createIdList(1, 2), createIdList(0, 2));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateBulkIntraCloudAuthorizationResponseWithDBConstraintViolation() {
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(new IntraCloudAuthorization()));
		final IntraCloudAuthorizationListResponseDTO dto = authorizationDBService.createBulkIntraCloudAuthorizationResponse(1, createIdList(1, 2), createIdList(1, 2));
		
		assertTrue(dto.getData().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateBulkIntraCloudAuthorizationResponseDBCall() {
		final System system = new System("test", "0.0.0.0", 1000, null);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		when(intraCloudAuthorizationRepository.saveAndFlush(any())).thenReturn(new IntraCloudAuthorization(system, system, serviceDefinition));
		when(intraCloudAuthorizationRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.ofNullable(null));
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		final IntraCloudAuthorizationListResponseDTO dto = authorizationDBService.createBulkIntraCloudAuthorizationResponse(1, createIdList(1, 1), createIdList(1, 1));
		
		assertEquals(1, dto.getData().size());
		assertEquals(1, dto.getCount());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<IntraCloudAuthorization> createPageForMockingIntraCloudAuthorizationRepository(final int numberOfRequestedEntry) {
		final List<IntraCloudAuthorization> entries = new ArrayList<>(numberOfRequestedEntry);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		for (int i = 1; i <= numberOfRequestedEntry; i++ ) {
			final System consumer = new System("Consumer" + i, i + "." + i +"." + i + "." + i, i * 1000, null);
			consumer.setId(i);
			final System provider = new System("Provider" + i, i + "." + i +"." + i + "." + i, i * 1000, null);
			provider.setId(i);
			final IntraCloudAuthorization entry = new IntraCloudAuthorization(consumer, provider, serviceDefinition);
			entry.setId(i);
			entries.add(entry);
		}
		return new PageImpl<IntraCloudAuthorization>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Long> createIdList(final int firstNum, final int lastNum) {
		final List<Long> idList = new ArrayList<>(lastNum);
		for (int i = firstNum; i <= lastNum; i++) {
			idList.add((long) i);
		}
		return idList;
	}
}
