package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestratorWarnings;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingParameters;

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
		final SystemRequestDTO provider = new SystemRequestDTO();
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
		final SystemRequestDTO provider = new SystemRequestDTO();
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
		final SystemRequestDTO provider = new SystemRequestDTO();
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
		final SystemRequestDTO provider = new SystemRequestDTO();
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
		final SystemRequestDTO provider = new SystemRequestDTO();
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
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationRequestNull() {
		testingObject.dynamicOrchestration(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint1Failed() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					flag(Flag.OVERRIDE_STORE, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint2Failed() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					flag(Flag.TRIGGER_INTER_CLOUD, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint3Failed() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					flag(Flag.ONLY_PREFERRED, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationServiceFormNull() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationServiceNameNull() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(new ServiceQueryFormDTO()).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationServiceNameEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO();
		serviceForm.setServiceDefinitionRequirement(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderSystemNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(new PreferredProviderDataDTO()).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderSystemNameNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(new SystemRequestDTO());
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderSystemNameEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("\t");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderSystemAddressNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderSystemAddressEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress(" ");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderSystemPortNull() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderCloudOperatorNull() {
		ReflectionTestUtils.setField(testingObject, "gateKeeperIsPresent", true);
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		ppData.setProviderCloud(new CloudRequestDTO());
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																				    flag(Flag.ENABLE_INTER_CLOUD, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderCloudOperatorEmpty() {
		ReflectionTestUtils.setField(testingObject, "gateKeeperIsPresent", true);
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setOperator("  ");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		ppData.setProviderCloud(providerCloud);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																				    flag(Flag.ENABLE_INTER_CLOUD, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderCloudNameNull() {
		ReflectionTestUtils.setField(testingObject, "gateKeeperIsPresent", true);
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setOperator("aitia");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		ppData.setProviderCloud(providerCloud);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																				    flag(Flag.ENABLE_INTER_CLOUD, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationPreferredProviderCloudNameEmpty() {
		ReflectionTestUtils.setField(testingObject, "gateKeeperIsPresent", true);
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setOperator("aitia");
		providerCloud.setName("\n\r\t");
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		ppData.setProviderCloud(providerCloud);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																				    flag(Flag.ENABLE_INTER_CLOUD, true).
																					build();
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationEmptySRResults() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(new ServiceQueryResultDTO());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request);
		Assert.assertEquals(0, result.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationAuthorizationFailed() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																					build();
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(new ServiceRegistryResponseDTO());
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(List.of());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request);
		Assert.assertEquals(0, result.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationNoPreferredIsAuthorized() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final PreferredProviderDataDTO ppData = new PreferredProviderDataDTO();
		ppData.setProviderSystem(provider);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(ppData).
																				    flag(Flag.ONLY_PREFERRED, true).
																					build();
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request);
		Assert.assertEquals(0, result.getResponse().size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationOneResultWithMatchmaking() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.MATCHMAKING, true).
																					build();
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		final ServiceRegistryResponseDTO srEntry2 = new ServiceRegistryResponseDTO();
		srEntry2.setProvider(new SystemResponseDTO(2, "d", "e", 6, null, null, null));
		srEntry2.setServiceDefinition(new ServiceDefinitionResponseDTO(3, "service", null, null));
		srEntry2.setInterfaces(List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null)));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		srResult.getServiceQueryData().add(srEntry2);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(intraCloudProviderMatchmaker.doMatchmaking(any(), any(IntraCloudProviderMatchmakingParameters.class))).thenReturn(srEntry2);
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request);
		Assert.assertEquals(1, result.getResponse().size());
		Assert.assertEquals(2, result.getResponse().get(0).getProvider().getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationTwoResultsWithoutMatchmaking() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final ServiceDefinitionResponseDTO serviceDefinition = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinition);
		srEntry.setInterfaces(interfaces);
		final ServiceRegistryResponseDTO srEntry2 = new ServiceRegistryResponseDTO();
		srEntry2.setProvider(new SystemResponseDTO(2, "d", "e", 6, null, null, null));
		srEntry2.setServiceDefinition(serviceDefinition);
		srEntry2.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		srResult.getServiceQueryData().add(srEntry2);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request);
		Assert.assertEquals(2, result.getResponse().size());
		Assert.assertEquals(1, result.getResponse().get(0).getProvider().getId());
		Assert.assertEquals(2, result.getResponse().get(1).getProvider().getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationAddedWarning() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.OVERRIDE_STORE, true).
																					build();
		final ServiceDefinitionResponseDTO serviceDefinition = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinition);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request);
		Assert.assertEquals(1, result.getResponse().size());
		Assert.assertEquals(1, result.getResponse().get(0).getProvider().getId());
		Assert.assertEquals(1, result.getResponse().get(0).getWarnings().size());
		Assert.assertEquals(OrchestratorWarnings.TTL_UNKNOWN, result.getResponse().get(0).getWarnings().get(0));
	}
}