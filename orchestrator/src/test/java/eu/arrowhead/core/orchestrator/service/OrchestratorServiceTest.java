package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestrationFlags;
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
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
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
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
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
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.OVERRIDE_STORE, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testExternalServiceRequestCrossParameterConstraint2Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.TRIGGER_INTER_CLOUD, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.externalServiceRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testExternalServiceRequestCrossParameterConstraint3Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_PREFERRED, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
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
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.OVERRIDE_STORE, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint2Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.TRIGGER_INTER_CLOUD, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.dynamicOrchestration(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint3Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_PREFERRED, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
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
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithSystemIdParameterByIdOk() {
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
		  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final OrchestratorStore orchestratorStore = new OrchestratorStore();
		orchestratorStore.setConsumerSystem(consumerSystem);
		orchestratorStore.setForeign(false);
		orchestratorStore.setServiceDefinition(serviceDefinition);
		orchestratorStore.setProviderSystemId(1L);
		orchestratorStore.setServiceInterface(serviceInterface);
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStore);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithSystemIdParameterByNullIdOk() {
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
		  		build();
		
		serviceForm.setInterfaceRequirements(List.of("HTTP-SECURE-JSON"));
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = null;
			
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final OrchestratorStore orchestratorStore = new OrchestratorStore();
		orchestratorStore.setConsumerSystem(consumerSystem);
		orchestratorStore.setForeign(false);
		orchestratorStore.setServiceDefinition(serviceDefinition);
		orchestratorStore.setProviderSystemId(1L);
		orchestratorStore.setServiceInterface(serviceInterface);
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStore);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithSystemIdParameterByNullRequest() {
		
		final OrchestrationFormRequestDTO request = null;
		final Long systemId = 1L;

		testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithSystemIdParameterByInvalidSystemId() {
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
		  		build();		
		serviceForm.setInterfaceRequirements(List.of("HTTP-SECURE-JSON"));
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = -1L;

		testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testOrchestrationFromStoreWithSystemIdParameterBySystemIdNotInDB() {
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
		  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final OrchestratorStore orchestratorStore = new OrchestratorStore();
		orchestratorStore.setConsumerSystem(consumerSystem);
		orchestratorStore.setForeign(false);
		orchestratorStore.setServiceDefinition(serviceDefinition);
		orchestratorStore.setProviderSystemId(1L);
		orchestratorStore.setServiceInterface(serviceInterface);
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStore);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = null;
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		
		testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithSystemIdParameterByIdBySomeForeignStoreEntriesOk() {
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
		  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final OrchestratorStore orchestratorStoreForeign = new OrchestratorStore();
		orchestratorStoreForeign.setConsumerSystem(consumerSystem);
		orchestratorStoreForeign.setForeign(true);
		orchestratorStoreForeign.setServiceDefinition(serviceDefinition);
		orchestratorStoreForeign.setProviderSystemId(1L);
		orchestratorStoreForeign.setServiceInterface(serviceInterface);
		
		final OrchestratorStore orchestratorStore = new OrchestratorStore();
		orchestratorStore.setConsumerSystem(consumerSystem);
		orchestratorStore.setForeign(false);
		orchestratorStore.setServiceDefinition(serviceDefinition);
		orchestratorStore.setProviderSystemId(1L);
		orchestratorStore.setServiceInterface(serviceInterface);
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStore, orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithSystemIdParameterByIdBySREntryWithMoreThenOneInterfaceOk() {
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
		  		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final OrchestratorStore orchestratorStore = new OrchestratorStore();
		orchestratorStore.setConsumerSystem(consumerSystem);
		orchestratorStore.setForeign(false);
		orchestratorStore.setServiceDefinition(serviceDefinition);
		orchestratorStore.setProviderSystemId(1L);
		orchestratorStore.setServiceInterface(serviceInterface);
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStore);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null), new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-XML", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), anyBoolean(), anyBoolean())).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.orchestrationFromStoreWithSystemIdParameter(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
}