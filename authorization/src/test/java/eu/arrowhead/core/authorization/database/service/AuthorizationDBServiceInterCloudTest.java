package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.InterCloudAuthorization;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.InterCloudAuthorizationRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.dto.InterCloudAuthorizationCheckResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class AuthorizationDBServiceInterCloudTest {
	
	//=================================================================================================
	// members
	@InjectMocks
	private AuthorizationDBService authorizationDBService;
	
	@Mock
	private InterCloudAuthorizationRepository interCloudAuthorizationRepository;
	
	@Mock
	private CloudRepository cloudRepository;
	
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	private static final ZonedDateTime zdTime = Utilities.parseUTCStringToLocalZonedDateTime("2222-12-12 12:00:00");
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getInterCloudAuthorizationEntries
	
	@Test
	public void testGetInterCloudAuthorizationEntriesCallDB() {
		final int numOfEntries = 3;
		when(interCloudAuthorizationRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingInterCloudAuthorizationRepository(numOfEntries));
		assertEquals(numOfEntries, authorizationDBService.getInterCloudAuthorizationEntries(0, 10, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInterCloudAuthorizationEntriesWithNotValidSortField() {
		authorizationDBService.getInterCloudAuthorizationEntries(0, 10, Direction.ASC, "notValid");
	}
	

	//-------------------------------------------------------------------------------------------------
	//Tests of getInterCloudAuthorizationEntryById
	
	@Test (expected = InvalidParameterException.class)
	public void testGetInterCloudAuthorizationEntryByIdWithNotExistingId() {
		when(interCloudAuthorizationRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getInterCloudAuthorizationEntryById(-1);
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of removeInterCloudAuthorizationEntryById
	
	@Test (expected = InvalidParameterException.class)
	public void testRemoveInterCloudAuthorizationEntryByIdWithNotExistingId() {
		when(interCloudAuthorizationRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeInterCloudAuthorizationEntryById(1);
	}

	//-------------------------------------------------------------------------------------------------
	//Tests of createInterCloudAuthorization
	
	@Test (expected = InvalidParameterException.class)
	public void testCreateInterCloudAuthorizationResponseWithInvalidCloudId() {
		authorizationDBService.createInterCloudAuthorization(-1L, Set.of(1L));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateInterCloudAuthorizationResponseWithInvalidServiceDefinitionId() {
		authorizationDBService.createInterCloudAuthorization(1L, Set.of(-1L));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateInterCloudAuthorizationResponseWithNullServiceDefinitionIdSet() {
		authorizationDBService.createInterCloudAuthorization(1L, null);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateInterCloudAuthorizationResponseWithNotExistingCloud() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
		when(interCloudAuthorizationRepository.findByCloudAndServiceDefinition(any(), any())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.createInterCloudAuthorization(Long.MAX_VALUE, Set.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCreateInterCloudAuthorizationResponseWithNotExistingServiceDefintition() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(interCloudAuthorizationRepository.findByCloudAndServiceDefinition(any(), any())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.createInterCloudAuthorization(1L, Set.of(Long.MAX_VALUE));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateInterCloudAuthorizationResponseDBCall() {
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
		serviceDefinition.setId(1);
		serviceDefinition.setCreatedAt(zdTime);
		serviceDefinition.setUpdatedAt(zdTime);
		
		final List<InterCloudAuthorization> entriesToSave = createPageForMockingInterCloudAuthorizationRepository(1).getContent();
		when(interCloudAuthorizationRepository.saveAll(any())).thenReturn(entriesToSave);
		when(interCloudAuthorizationRepository.findByCloudAndServiceDefinition(any(), any())).thenReturn(Optional.ofNullable(null));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getValidTestCloud()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
		
		final Page<InterCloudAuthorization> entry = authorizationDBService.createInterCloudAuthorization(1L, Set.of(1L));
		assertEquals(getValidTestCloud().getPort(), entry.getContent().get(0).getCloud().getPort());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of checkInterCloudAuthorizationResponse
	
	@Test (expected = InvalidParameterException.class)
	public void testCheckInterCloudAuthorizationRequestResponseWithInvalidCloudId() {
		when(cloudRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkInterCloudAuthorizationResponse(0, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCheckInterCloudAuthorizationRequestResponseWithNotExistingCloud() {
		when(cloudRepository.existsById(anyLong())).thenReturn(false);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkInterCloudAuthorizationResponse(1, 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCheckInterCloudAuthorizationRequestResponseWithInvalidServiceDefintitionId() {
		when(cloudRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		authorizationDBService.checkInterCloudAuthorizationResponse(1, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void testCheckInterCloudAuthorizationRequestResponseWithNotExistingServiceDefintition() {
		when(cloudRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.checkInterCloudAuthorizationResponse(1, 1);
	}
	

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckInterCloudAuthorizationRequestResponseDBCall() {
		final Cloud cloud = getValidTestCloud();
		cloud.setId(8);
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		serviceDefinition.setId(3);
		when(cloudRepository.existsById(anyLong())).thenReturn(true);
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of( new ServiceDefinition()));
		when(interCloudAuthorizationRepository.findByCloudAndServiceDefinition(any(Cloud.class), any(ServiceDefinition.class)))
			.thenReturn(Optional.of(new InterCloudAuthorization(cloud,  serviceDefinition)));
		
		final InterCloudAuthorizationCheckResponseDTO dto = authorizationDBService.checkInterCloudAuthorizationResponse(1, 1);
		
		assertTrue(dto.getCloudIdAuthorizationState());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<InterCloudAuthorization> createPageForMockingInterCloudAuthorizationRepository(final int numberOfRequestedEntry) {
		final List<InterCloudAuthorization> entries = new ArrayList<>(numberOfRequestedEntry);
		final Cloud cloud = getValidTestCloud();
		for (int i = 1; i <= numberOfRequestedEntry; i++ ) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService"+i);
			serviceDefinition.setId(i);
			final InterCloudAuthorization entry = new InterCloudAuthorization(cloud, serviceDefinition);
			entry.setId(i);
			entries.add(entry);
		}
		return new PageImpl<InterCloudAuthorization>(entries);
	}
	
	//-------------------------------------------------------------------------------------------------
	private static Cloud getValidTestCloud() {
		
		final int port = 1234;
		final boolean secure = true;
		final boolean neighbor = true;
		final boolean ownCloud = true;
		
		final Cloud cloud = new Cloud(
				"testOperator",
				"testCloudName",
				"testcloudAddress",
				port,
				"testGatekeeperServiceUri",
				"testAuthenticationInfo", 
				 secure,
				 neighbor,
				 ownCloud
				);
		
		cloud.setId(1);
		cloud.setCreatedAt(zdTime);
		cloud.setUpdatedAt(zdTime);

		return cloud;
	}	

}

