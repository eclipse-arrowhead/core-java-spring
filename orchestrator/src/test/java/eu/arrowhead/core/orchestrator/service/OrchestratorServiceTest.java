package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;

@RunWith(SpringRunner.class)
public class OrchestratorServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private OrchestratorService testingObject;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private IntraCloudProviderMatchmakingAlgorithm intraCloudProviderMatchmaker;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestRequestNull() {
		testingObject.externalServiceRequest(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testExternalServiceRequestCrossParameterConstraint1Failed() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					flag(Flag.OVERRIDE_STORE, true).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testExternalServiceRequestCrossParameterConstraint2Failed() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					flag(Flag.TRIGGER_INTER_CLOUD, true).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testExternalServiceRequestCrossParameterConstraint3Failed() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					flag(Flag.ONLY_PREFERRED, true).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestServiceFormNull() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestServiceNameNull() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(new ServiceQueryFormDTO()).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestServiceNameEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO();
		serviceForm.setServiceDefinitionRequirement(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestPreferredProviderSystemNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(new PreferredProviderDataDTO()).
																					build();
		
		testingObject.externalServiceRequest(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestPreferredProviderSystemNameNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(new SystemRequestDTO());
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestPreferredProviderSystemNameEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider= new SystemRequestDTO();
		provider.setSystemName("\t");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.externalServiceRequest(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestPreferredProviderSystemAddressNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider= new SystemRequestDTO();
		provider.setSystemName("provider");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestPreferredProviderSystemAddressEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider= new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress(" ");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExternalServiceRequestPreferredProviderSystemPortNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider= new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalServiceRequestOnlyPreferredRemoveAllResults() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider= new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.ONLY_PREFERRED, true).
																				    preferredProviders(ppData).
																					build();
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		
		final OrchestrationResponseDTO response = testingObject.externalServiceRequest(request);
		Assert.assertEquals(0, response.getResponse().size());
	}
}