package eu.arrowhead.core.authorization.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import eu.arrowhead.common.CommonConstants;
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
	private SystemRepository systemRepository;
	
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Mock
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Mock
	private AuthorizationInterCloudInterfaceConnectionRepository authorizationInterCloudInterfaceConnectionRepository;
	
	private static final ZonedDateTime zdTime = Utilities.parseUTCStringToLocalZonedDateTime("2222-12-12 12:00:00");
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	//Tests of getAuthorizationInterCloudEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizationInterCloudEntriesCallDB() {
		final int numOfEntries = 3;
		when(authorizationInterCloudRepository.findAll(any(PageRequest.class))).thenReturn(createPageForMockingAuthorizationInterCloudRepository(numOfEntries));
		assertEquals(numOfEntries, authorizationDBService.getAuthorizationInterCloudEntries(0, 10, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID).getNumberOfElements());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationInterCloudEntriesWithNotValidSortField() {
		authorizationDBService.getAuthorizationInterCloudEntries(0, 10, Direction.ASC, "notValid");
	}

	//=================================================================================================
	//Tests of getAuthorizationInterCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAuthorizationInterCloudEntryByIdWithNotExistingId() {
		when(authorizationInterCloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		authorizationDBService.getAuthorizationInterCloudEntryById(-1);
	}
	
	//=================================================================================================
	//Tests of removeAuthorizationInterCloudEntryById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveAuthorizationInterCloudEntryByIdWithNotExistingId() {
		when(authorizationInterCloudRepository.existsById(anyLong())).thenReturn(false);
		authorizationDBService.removeAuthorizationInterCloudEntryById(1);
	}

	//=================================================================================================
	//Tests of createBulkAuthorizationInterCloud
	
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
		assertEquals(getValidTestCloud().getPort(), entry.get(0).getCloud().getPort());
	}
	
	//=================================================================================================
	//Tests of checkAuthorizationInterCloudResponse
	
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
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Page<AuthorizationInterCloud> createPageForMockingAuthorizationInterCloudRepository(final int numberOfRequestedEntry) {
		final List<AuthorizationInterCloud> entries = new ArrayList<>(numberOfRequestedEntry);
		final Cloud cloud = getValidTestCloud();
		final System provider = new System("testSystem", "testAddr", 2000, "TOKEN");
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
		final int port = 1234;
		final boolean secure = true;
		final boolean neighbor = false;
		final boolean ownCloud = true;
		
		final Cloud cloud = new Cloud("testOperator", "testCloudName", "testcloudAddress", port, "testGatekeeperServiceUri", "testAuthenticationInfo", secure, neighbor, ownCloud);
		cloud.setId(1);
		cloud.setCreatedAt(zdTime);
		cloud.setUpdatedAt(zdTime);

		return cloud;
	}	
}