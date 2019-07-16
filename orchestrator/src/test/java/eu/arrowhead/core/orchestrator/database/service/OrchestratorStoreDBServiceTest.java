package eu.arrowhead.core.orchestrator.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestByIdDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class OrchestratorStoreDBServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	OrchestratorStoreDBService orchestratorStoreDBService; 
	
	@Mock
	OrchestratorStoreRepository orchestratorStoreRepository;
	
	@Mock
	SystemRepository systemRepository;
	
	@Mock
	ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Mock
	CloudRepository cloudRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	// Test getOrchestratorStoreById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdTest() {
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = Optional.of(getOrchestratorStore());
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(orchestratorStoreOptional);
		
		orchestratorStoreDBService.getOrchestratorStoreById( 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoreByIdWithInvalidIdTest() {
		
		final Optional<OrchestratorStore> orchestratorStoreOptional = Optional.of(getOrchestratorStore());
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(orchestratorStoreOptional);
		
		orchestratorStoreDBService.getOrchestratorStoreById( -1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoreByIdNotInDBTest() {
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		orchestratorStoreDBService.getOrchestratorStoreById( 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test getOrchestratorStoreById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreEntriesResponseOKTest() {
		
		when(orchestratorStoreRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(0, 10, Direction.ASC, "id");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoreEntriesResponseWithNotValidSortFieldTest() {
		
		when(orchestratorStoreRepository.findAll(any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test getAllTopPriorityOrchestratorStoreEntriesResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getAllTopPriorityOrchestratorStoreEntriesResponseOKTest() {
		
		when(orchestratorStoreRepository.findAllByPriority(anyInt(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(0, 10, Direction.ASC, "id");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getAllTopPriorityOrchestratorStoreEntriesResponseWithNotValidSortFieldTest() {
		
		when(orchestratorStoreRepository.findAllByPriority(anyInt(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test getOrchestratorStoresByConsumerResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerResponseOKTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "id", 1L, 1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoresByConsumerResponseWithNotValidSortFieldTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "notValid", 1L, 1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoresByConsumerResponseWithNotValidSystemIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "id", -1L, 1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoresByConsumerResponseWithNotValidServiceDefinitionIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "id", 1L, -1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoresByConsumerResponseWithSystemIdNotInDBTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "id", 1L, 1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoresByConsumerResponseWithServiceDefinitionIdNotInDBTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "id", 1L, 1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoresByConsumerResponseNotInDBTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreListNotInDB());
		
		orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(0, 10, Direction.ASC, "id", 1L, 1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test createOrchestratorStoresByIdResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createOrchestratorStoresByIdResponseOKTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getProviderCloudForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		when(orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemAndServiceInterface(any(), any(), any(), any())).thenReturn(Optional.ofNullable(null));	
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(getOrchestratorStoreListForTest(3));
		when(orchestratorStoreRepository.saveAndFlush(any())).thenReturn(getOrchestratorStore());
		
		orchestratorStoreDBService.createOrchestratorStoresByIdResponse(getOrchestratorStoreRequestByIdDTOListForTest(3));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoresByIdResponseWithNullRequestTest() {
		
		orchestratorStoreDBService.createOrchestratorStoresByIdResponse(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoresByIdResponseWithEmptyRequestTest() {
		
		orchestratorStoreDBService.createOrchestratorStoresByIdResponse(getOrchestratorStoreRequestByIdDTOEmptyListForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createOrchestratorStoresByIdResponseWithSomeEmptyOrchestratoreStoreTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getProviderCloudForTest()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any(), any(PageRequest.class))).thenReturn(getPageOfOrchestratorStoreList());
		when(orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemAndServiceInterface(any(), any(), any(), any())).thenReturn(Optional.ofNullable(null));	
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(getOrchestratorStoreListForTest(3));
		when(orchestratorStoreRepository.saveAndFlush(any())).thenReturn(getOrchestratorStore());
		
		orchestratorStoreDBService.createOrchestratorStoresByIdResponse(getOrchestratorStoreRequestByIdDTOListWithSomeEmptyOrchestratoreStoreForTest(3));
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test createOrchestratorStoreEntityById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithInvalidConsumerSystemIdTest() {
				
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithInvalidConsumerSystemIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithInvalidProviderSystemIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithInvalidProviderSystemIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithInvalidServiceDefinitionIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithInvalidServiceDefinitionIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithInvalidPriorityTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithInvalidPriorityForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithInvalidCloudIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithInvalidCloudIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNullServiceDefinitionIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));

		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithNullServiceDefinitionIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNullConsumerSystemIdTest() {

		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithNullConsumerSystemIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNullProviderSystemIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));

		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithNullProviderSystemIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNullPriorityTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));

		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithNullPriorityForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createOrchestratorStoreEntityByIdWithNullCloudIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));

		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithNullCloudIdForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNotInDBServiceDefinitionIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNotInDBConsumerSystemIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNotInDBProviderSystemIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithNotInDBCloudIdTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createOrchestratorStoreEntityByIdWithUniqueConstraintTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getProviderCloudForTest()));
		when(orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemAndServiceInterface(any(), any(), any(), any())).thenReturn(Optional.of(getOrchestratorStore()));
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createOrchestratorStoreEntityByIdWithPriorityCombinationNotPresentInDBTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getProviderCloudForTest()));
		when(orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemAndServiceInterface(any(), any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(new ArrayList<>());
		when(orchestratorStoreRepository.saveAndFlush(any())).thenReturn(getOrchestratorStore());
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createOrchestratorStoreEntityByIdWithPriorityNotPresentInDBEntitiesTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getProviderCloudForTest()));
		when(orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemAndServiceInterface(any(), any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(getOrchestratorStoreListForTest(3));
		when(orchestratorStoreRepository.saveAndFlush(any())).thenReturn(getOrchestratorStore());
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOWithPriorityNotPresentInDBForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createOrchestratorStoreEntityByIdWithPriorityPresentInDBEntitiesTest() {
		
		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(getSystemForTest()));
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.of(getServiceDefinitionForTest()));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(getProviderCloudForTest()));
		when(orchestratorStoreRepository.findByConsumerSystemAndServiceDefinitionAndProviderSystemAndServiceInterface(any(), any(), any(), any())).thenReturn(Optional.ofNullable(null));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(getOrchestratorStoreListForTest(3));
		when(orchestratorStoreRepository.saveAndFlush(any())).thenReturn(getOrchestratorStore());
		
		orchestratorStoreDBService.createOrchestratorStoreEntityById(getOrchestratorStoreRequestByIdDTOForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test removeOrchestratorStoreById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreByIdWithLeftPriorityCombinationPresentInDBOkTest() {
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.of(getOrchestratorStore()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(getOrchestratorStoreListForRemoveOrchestratorStoreTest(3));
		
		orchestratorStoreDBService.removeOrchestratorStoreById(getIdForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeOrchestratorStoreByIdWithLeftPriorityCombinationNotPresentInDBOkTest() {
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.of(getOrchestratorStore()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(new ArrayList<OrchestratorStore>());
		
		orchestratorStoreDBService.removeOrchestratorStoreById(getIdForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void removeOrchestratorStoreByIdWithInvalidIdTest() {
		
		orchestratorStoreDBService.removeOrchestratorStoreById(getInvalidIdForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void removeOrchestratorStoreByIdWithIdNotInDBTest() {
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		orchestratorStoreDBService.removeOrchestratorStoreById(getIdForTest());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	// Test modifyOrchestratorStorePriorityResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void modifyOrchestratorStorePriorityResponseOKTest() {
		
		final int testMapSize = 3;
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.of(getOrchestratorStore()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(getOrchestratorStoreListForTest(testMapSize));

		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(getOrchestratorStoreModifyPriorityRequestDTO(testMapSize));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void modifyOrchestratorStorePriorityResponseNullRequestTest() {

		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void modifyOrchestratorStorePriorityResponseEmptyRequestTest() {

		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(getOrchestratorStoreModifyPriorityRequestDTOWithEmptyRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void modifyOrchestratorStorePriorityResponseMapKeyStoreIDNotInDBTest() {

		final int testMapSize = 3;
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(getOrchestratorStoreModifyPriorityRequestDTO(testMapSize));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void modifyOrchestratorStorePriorityResponseMapKeyConsumerAndServiceDefinitionCombinationNotInDBTest() {

		final int testMapSize = 3;
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.of(getOrchestratorStore()));
		when(orchestratorStoreRepository.findAllByConsumerSystemAndServiceDefinition(any(), any())).thenReturn(new ArrayList<OrchestratorStore>());
		
		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(getOrchestratorStoreModifyPriorityRequestDTO(testMapSize));
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithInvalidServiceDefinitionIdForTest() {
		
		final Long serviceDefinitionId = -1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithInvalidConsumerSystemIdForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = -1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithInvalidProviderSystemIdForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = -1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithInvalidCloudIdForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = -1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithInvalidPriorityForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = -1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithNullServiceDefinitionIdForTest() {
		
		final Long serviceDefinitionId = null;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithNullConsumerSystemIdForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = null;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithNullProviderSystemIdForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = null;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithNullCloudIdForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = null;
		final Long serviceInterfaceId = 1L;
		final Integer priority = 1;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithNullPriorityForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = null;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreRequestByIdDTO getOrchestratorStoreRequestByIdDTOWithPriorityNotPresentInDBForTest() {
		
		final Long serviceDefinitionId = 1L;
		final Long consumerSystemId = 1L;
		final Long providerSystemId = 1L;
		final Long cloudId = 1L;
		final Long serviceInterfaceId = 1L;
		final Integer priority = Integer.MAX_VALUE;
		final Map<String,String> attribute = null;
		
		return new OrchestratorStoreRequestByIdDTO(
				serviceDefinitionId,
				consumerSystemId,
				providerSystemId,
				cloudId,
				serviceInterfaceId,
				priority,
				attribute);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOListForTest(final int listSize) {
		
		final List<OrchestratorStoreRequestByIdDTO> orchestratorStoreRequestByIdDTOList = new ArrayList<OrchestratorStoreRequestByIdDTO>(listSize);
		
		for (int i = 0; i < listSize; i++) {
			
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO = getOrchestratorStoreRequestByIdDTOForTest();
			orchestratorStoreRequestByIdDTO.setProviderSystemId(i + 1L);
			orchestratorStoreRequestByIdDTOList.add(orchestratorStoreRequestByIdDTO);
		}
		return orchestratorStoreRequestByIdDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOEmptyListForTest() {
		
		final List<OrchestratorStoreRequestByIdDTO> orchestratorStoreRequestByIdDTOList = new ArrayList<>();
		
		return orchestratorStoreRequestByIdDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore getOrchestratorStore() {
		
		final OrchestratorStore orchestratorStore = new OrchestratorStore(
				getServiceDefinitionForTest(),
				getConsumerSystemForTest(),
				getProviderSystemForTest(),
				getProviderCloudForTest(),
				getServiceIntefaceForTest(),
				getPriorityForTest(),
				getAttributeStringForTest(),
				getCreatedAtForTest(),
				getUpdatedAtForTest()
				);
		
		orchestratorStore.setId(getIdForTest());
		
		return orchestratorStore;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getOrchestratorStoreListForTest(final int listSize) {
		
		final List<OrchestratorStore> orchestratorStoreList = new ArrayList<>(listSize);
		
		for (int i = 0; i < listSize; i++) {
			
			final OrchestratorStore orchestratorStore = getOrchestratorStore();
			orchestratorStore.setId(i + 1L);
			orchestratorStore.getProviderSystem().setId(i + 1L);
			orchestratorStore.setPriority(i + 1);
			orchestratorStoreList.add(orchestratorStore);
		}
		return orchestratorStoreList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getOrchestratorStoreListForRemoveOrchestratorStoreTest(final int listSize) {
		
		final List<OrchestratorStore> orchestratorStoreList = new ArrayList<>(listSize);
		
		for (int i = 1; i < listSize+1; i++) {
			
			final OrchestratorStore orchestratorStore = getOrchestratorStore();
			orchestratorStore.setId(i);
			orchestratorStore.getProviderSystem().setId(i + 1L);
			orchestratorStore.setPriority(i + 1);
			orchestratorStoreList.add(orchestratorStore);
		}
		return orchestratorStoreList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreRequestByIdDTO> getOrchestratorStoreRequestByIdDTOListWithSomeEmptyOrchestratoreStoreForTest(final int listSize) {
		
		final List<OrchestratorStoreRequestByIdDTO> orchestratorStoreRequestByIdDTOList = new ArrayList<OrchestratorStoreRequestByIdDTO>(listSize);
		
		for (int i = 0; i < listSize; i++) {
			
			final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO = getOrchestratorStoreRequestByIdDTOForTest();
			orchestratorStoreRequestByIdDTO.setProviderSystemId(i + 1L);
			orchestratorStoreRequestByIdDTOList.add(orchestratorStoreRequestByIdDTO);
		}
		
		for (int i = 0; i < listSize; i++) {
			
			orchestratorStoreRequestByIdDTOList.add(new OrchestratorStoreRequestByIdDTO());
		}
		
		return orchestratorStoreRequestByIdDTOList;
	}
	//-------------------------------------------------------------------------------------------------
	private PageImpl<OrchestratorStore> getPageOfOrchestratorStoreList() {
		final List<OrchestratorStore>	orchestratorStoreList = List.of(getOrchestratorStore());
		
		return new PageImpl<OrchestratorStore>(orchestratorStoreList);
	}
	
	//-------------------------------------------------------------------------------------------------
	private PageImpl<OrchestratorStore> getPageOfOrchestratorStoreListNotInDB() {
		final List<OrchestratorStore>	orchestratorStoreList = new ArrayList<OrchestratorStore>();
		
		return new PageImpl<OrchestratorStore>(orchestratorStoreList);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreModifyPriorityRequestDTO getOrchestratorStoreModifyPriorityRequestDTO(final int size) {
		final OrchestratorStoreModifyPriorityRequestDTO request = new OrchestratorStoreModifyPriorityRequestDTO();
		
		final Map<Long, Integer> priorityMap = new HashMap<Long, Integer>(size); 
		for (int i = 0; i < size; i++) {
			priorityMap.put(i + 1L, i + 1);
		}		
		request.setPriorityMap(priorityMap);
		
		return request;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreModifyPriorityRequestDTO getOrchestratorStoreModifyPriorityRequestDTOWithEmptyRequest() {
		final OrchestratorStoreModifyPriorityRequestDTO request = new OrchestratorStoreModifyPriorityRequestDTO();
		
		final Map<Long, Integer> priorityMap = new HashMap<>(0); 
		request.setPriorityMap(priorityMap);
		
		return request;
	}
	//-------------------------------------------------------------------------------------------------
	private ZonedDateTime getUpdatedAtForTest() {
			
		return Utilities.parseUTCStringToLocalZonedDateTime("2019-07-04 14:43:19");
	}
	
	//-------------------------------------------------------------------------------------------------
	private ZonedDateTime getCreatedAtForTest() {
			
		return Utilities.parseUTCStringToLocalZonedDateTime("2019-07-04 14:43:19");
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getAttributeStringForTest() {
			
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Integer getPriorityForTest() {
			
		return 1;
	}

	//-------------------------------------------------------------------------------------------------
	private Cloud getProviderCloudForTest() {
			
		final Cloud cloud =  new Cloud(
					"operator",
					"name", 
					"address", 
					1234, 
					"gatekeeperServiceUri", 
					"authenticationInfo", 
					true, 
					true, 
					true);
		cloud.setId(getIdForTest());
		cloud.setCreatedAt(getCreatedAtForTest());
		cloud.setUpdatedAt(getUpdatedAtForTest());
		
		return cloud;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceInterface getServiceIntefaceForTest() {
		
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-XML");
		
		return serviceInterface;
	}
	
	//-------------------------------------------------------------------------------------------------
	private System getSystemForTest() {
			
		final System system = new System(
				"systemName",
				"address", 
				1234, 
				null);
		
		system.setId(getIdForTest());
		system.setCreatedAt(getCreatedAtForTest());
		system.setUpdatedAt(getUpdatedAtForTest());
		
		return system;
	}
	
	//-------------------------------------------------------------------------------------------------
	private System getProviderSystemForTest() {
		
		return getSystemForTest();
	}
	
	//-------------------------------------------------------------------------------------------------
	private System getConsumerSystemForTest() {
			
		return getSystemForTest();
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition getServiceDefinitionForTest() {
			
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		serviceDefinition.setId(getIdForTest());
		serviceDefinition.setCreatedAt(getCreatedAtForTest());
		serviceDefinition.setUpdatedAt(getUpdatedAtForTest());
		
		return serviceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	private long getIdForTest() {
		
		return 1L;
	}
	
	//-------------------------------------------------------------------------------------------------
	private long getInvalidIdForTest() {
		
		return -1L;
	}
}