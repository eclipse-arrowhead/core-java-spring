package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import eu.arrowhead.common.database.entity.AuthorizationInterCloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.AuthorizationInterCloudRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckResponseDTO;
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
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	private static final ZonedDateTime zdTime = Utilities.parseUTCStringToLocalZonedDateTime("2222-12-12 12:00:00");
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of getAuthorizationInterCloudEntries
	
	//-------------------------------------------------------------------------------------------------
//	@Test
//	public void testGetAuthorizationInterCloudEntriesCallDB() {
//		final int numOfEntries = 3;
//		when(authorizationInterCloudRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingAuthorizationInterCloudRepository(numOfEntries));
//		assertEquals(numOfEntries, authorizationDBService.getAuthorizationInterCloudEntries(0, 10, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testGetAuthorizationInterCloudEntriesWithNotValidSortField() {
//		authorizationDBService.getAuthorizationInterCloudEntries(0, 10, Direction.ASC, "notValid");
//	}
//
//	//=================================================================================================
//	//Tests of getAuthorizationInterCloudEntryById
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testGetAuthorizationInterCloudEntryByIdWithNotExistingId() {
//		when(authorizationInterCloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
//		authorizationDBService.getAuthorizationInterCloudEntryById(-1);
//	}
//	
//	//=================================================================================================
//	//Tests of removeAuthorizationInterCloudEntryById
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testRemoveAuthorizationInterCloudEntryByIdWithNotExistingId() {
//		when(authorizationInterCloudRepository.existsById(anyLong())).thenReturn(false);
//		authorizationDBService.removeAuthorizationInterCloudEntryById(1);
//	}
//
//	//=================================================================================================
//	//Tests of createAuthorizationInterCloud
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCreateAuthorizationInterCloudWithInvalidCloudId() {
//		authorizationDBService.createAuthorizationInterCloud(-1L, Set.of(1L));		
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCreateAuthorizationInterCloudWithInvalidServiceDefinitionId() {
//		authorizationDBService.createAuthorizationInterCloud(1L, Set.of(-1L));		
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCreateAuthorizationInterCloudWithNullServiceDefinitionIdSet() {
//		authorizationDBService.createAuthorizationInterCloud(1L, null);		
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCreateAuthorizationInterCloudWithNotExistingCloud() {
//		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
//		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(new ServiceDefinition()));
//		when(authorizationInterCloudRepository.findByCloudAndServiceDefinition(any(), any())).thenReturn(Optional.ofNullable(null));
//		
//		authorizationDBService.createAuthorizationInterCloud(Long.MAX_VALUE, Set.of(1L));
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCreateAuthorizationInterCloudWithNotExistingServiceDefintition() {
//		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
//		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
//		when(authorizationInterCloudRepository.findByCloudAndServiceDefinition(any(), any())).thenReturn(Optional.ofNullable(null));
//		
//		authorizationDBService.createAuthorizationInterCloud(1L, Set.of(Long.MAX_VALUE));
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test
//	public void testCreateAuthorizationInterCloudDBCall() {
//		final ServiceDefinition serviceDefinition = new ServiceDefinition("testService");
//		serviceDefinition.setId(1);
//		serviceDefinition.setCreatedAt(zdTime);
//		serviceDefinition.setUpdatedAt(zdTime);
//		
//		final List<AuthorizationInterCloud> entriesToSave = createPageForMockingAuthorizationInterCloudRepository(1).getContent();
//		when(authorizationInterCloudRepository.saveAll(any())).thenReturn(entriesToSave);
//		when(authorizationInterCloudRepository.findByCloudAndServiceDefinition(any(), any())).thenReturn(Optional.ofNullable(null));
//		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getValidTestCloud()));
//		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(serviceDefinition));
//		
//		final Page<AuthorizationInterCloud> entry = authorizationDBService.createAuthorizationInterCloud(1L, Set.of(1L));
//		assertEquals(getValidTestCloud().getPort(), entry.getContent().get(0).getCloud().getPort());
//	}
//	
//	//=================================================================================================
//	//Tests of checkAuthorizationInterCloudResponse
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCheckAuthorizationInterCloudResponseWithInvalidCloudId() {
//		when(cloudRepository.existsById(anyLong())).thenReturn(true);
//		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
//		authorizationDBService.checkAuthorizationInterCloudResponse(0, 1);
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCheckAuthorizationInterCloudResponseWithNotExistingCloud() {
//		when(cloudRepository.existsById(anyLong())).thenReturn(false);
//		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
//		authorizationDBService.checkAuthorizationInterCloudResponse(1, 1);
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCheckAuthorizationInterCloudResponseWithInvalidServiceDefintitionId() {
//		when(cloudRepository.existsById(anyLong())).thenReturn(true);
//		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
//		authorizationDBService.checkAuthorizationInterCloudResponse(1, 0);
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test(expected = InvalidParameterException.class)
//	public void testCheckAuthorizationInterCloudResponseWithNotExistingServiceDefintition() {
//		when(cloudRepository.existsById(anyLong())).thenReturn(true);
//		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
//		authorizationDBService.checkAuthorizationInterCloudResponse(1, 1);
//	}
//	
//
//	//-------------------------------------------------------------------------------------------------
//	@Test
//	public void testCheckAuthorizationInterCloudResponseNotAuthorizedDBCall() {
//		final Cloud cloud = getValidTestCloud();
//		cloud.setId(8);
//		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
//		serviceDefinition.setId(3);
//		when(cloudRepository.existsById(anyLong())).thenReturn(true);
//		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
//		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
//		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of( new ServiceDefinition()));
//		when(authorizationInterCloudRepository.findByCloudAndServiceDefinition(any(Cloud.class), any(ServiceDefinition.class))).thenReturn(Optional.ofNullable(null));
//		
//		final AuthorizationInterCloudCheckResponseDTO dto = authorizationDBService.checkAuthorizationInterCloudResponse(1, 1);
//		assertFalse(dto.getCloudIdAuthorizationState());
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	@Test
//	public void testCheckAuthorizationInterCloudResponseAuthorizedDBCall() {
//		final Cloud cloud = getValidTestCloud();
//		cloud.setId(8);
//		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
//		serviceDefinition.setId(3);
//		when(cloudRepository.existsById(anyLong())).thenReturn(true);
//		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(true);
//		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(new Cloud()));
//		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of( new ServiceDefinition()));
//		when(authorizationInterCloudRepository.findByCloudAndServiceDefinition(any(Cloud.class), any(ServiceDefinition.class))).thenReturn(Optional.of(new AuthorizationInterCloud(cloud,
//																																												   serviceDefinition)));
//		
//		final AuthorizationInterCloudCheckResponseDTO dto = authorizationDBService.checkAuthorizationInterCloudResponse(1, 1);
//		assertTrue(dto.getCloudIdAuthorizationState());
//	}
//	
//	//=================================================================================================
//	// assistant methods
//	
//	//-------------------------------------------------------------------------------------------------
//	private Page<AuthorizationInterCloud> createPageForMockingAuthorizationInterCloudRepository(final int numberOfRequestedEntry) {
//		final List<AuthorizationInterCloud> entries = new ArrayList<>(numberOfRequestedEntry);
//		final Cloud cloud = getValidTestCloud();
//		for (int i = 1; i <= numberOfRequestedEntry; ++i) {
//			final ServiceDefinition serviceDefinition = new ServiceDefinition("testService" + i);
//			serviceDefinition.setId(i);
//			final AuthorizationInterCloud entry = new AuthorizationInterCloud(cloud, serviceDefinition);
//			entry.setId(i);
//			entries.add(entry);
//		}
//		
//		return new PageImpl<>(entries);
//	}
//	
//	//-------------------------------------------------------------------------------------------------
//	private static Cloud getValidTestCloud() {
//		final int port = 1234;
//		final boolean secure = true;
//		final boolean neighbor = false;
//		final boolean ownCloud = true;
//		
//		final Cloud cloud = new Cloud("testOperator", "testCloudName", "testcloudAddress", port, "testGatekeeperServiceUri", "testAuthenticationInfo", secure, neighbor, ownCloud);
//		cloud.setId(1);
//		cloud.setCreatedAt(zdTime);
//		cloud.setUpdatedAt(zdTime);
//
//		return cloud;
//	}	
}