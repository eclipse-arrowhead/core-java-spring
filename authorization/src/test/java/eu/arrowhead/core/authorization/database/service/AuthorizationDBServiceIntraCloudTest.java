package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
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
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class AuthorizationDBServiceIntraCloudTest {
	
	//=================================================================================================
	// members
	@InjectMocks
	private AuthorizationDBService authorizationDBService;
	
	@Mock
	private IntraCloudAuthorizationRepository intraCloudAuthorizationRepository;
	
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
}
