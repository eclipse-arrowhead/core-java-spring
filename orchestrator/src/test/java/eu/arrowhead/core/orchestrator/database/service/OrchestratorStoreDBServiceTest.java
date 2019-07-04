package eu.arrowhead.core.orchestrator.database.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;
import eu.arrowhead.common.dto.CloudResponseDTO;
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
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrchestratorStoreByIdTest() {
		Optional<OrchestratorStore> orchestratorStoreOptional = Optional.of(getOrchestratorStore());
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(orchestratorStoreOptional);
		
		//when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		orchestratorStoreDBService.getOrchestratorStoreById( 1);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrchestratorStoreByIdWithInvalidIdTest() {
		
		when(orchestratorStoreRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		orchestratorStoreDBService.getOrchestratorStoreById( -1);		
	}
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestratorStoreResponseDTO getOrchestratorStoreResponseDTOForTest() {
		
		return new OrchestratorStoreResponseDTO(
				getIdForTest(),
				getServiceDefinitionResponseDTOForTest(),
				getConsumerSystemResponseDTOForTest(),
				getProviderSystemResponseDTOForTest(),
				getProviderCloudResponseDTOForTest(),
				getPriorityForTest(),
				getAttributeForTest(),
				getCreatedAtForTest(),
				getUpdatedAtForTest());
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestratorStore getOrchestratorStore() {
		
		OrchestratorStore orchestratorStore = new OrchestratorStore(
				getServiceDefinitionForTest(),
				getConsumerSystemForTest(),
				getProviderSystemForTest(),
				getProviderCloudForTest(),
				getPriorityForTest(),
				getAttributeForTest(),
				getCreatedAtForTest(),
				getUpdatedAtForTest()
				);
		
		orchestratorStore.setId(getIdForTest());
		
		return orchestratorStore;
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
	private String getAttributeForTest() {
			
			return null;
		}
	
	//-------------------------------------------------------------------------------------------------
	private Integer getPriorityForTest() {
			
			return 1;
		}
	
	//-------------------------------------------------------------------------------------------------
	private CloudResponseDTO getProviderCloudResponseDTOForTest() {
			
			return null;
		}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getProviderSystemResponseDTOForTest() {
			// TODO Auto-generated method stub
			return null;
		}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getConsumerSystemResponseDTOForTest() {
			// TODO Auto-generated method stub
			return null;
		}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinitionResponseDTO getServiceDefinitionResponseDTOForTest() {
			// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private Cloud getProviderCloudForTest() {
			
		Cloud cloud =  new Cloud(
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
	private System getProviderSystemForTest() {
			
			System system = new System(
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
	private System getConsumerSystemForTest() {
			
			return getProviderSystemForTest();
		}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceDefinition getServiceDefinitionForTest() {
			
		ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		serviceDefinition.setId(getIdForTest());
		serviceDefinition.setCreatedAt(getCreatedAtForTest());
		serviceDefinition.setUpdatedAt(getUpdatedAtForTest());
		
		return serviceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	private long getIdForTest() {
		
		return 1;
	}
}
