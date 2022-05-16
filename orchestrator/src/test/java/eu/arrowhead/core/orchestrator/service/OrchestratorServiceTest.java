/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.orchestrator.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.core.orchestrator.matchmaking.CloudMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.CloudMatchmakingParameters;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingParameters;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingParameters;
import eu.arrowhead.core.qos.manager.QoSManager;
import eu.arrowhead.core.qos.manager.impl.DummyQoSManager;

@RunWith(SpringRunner.class)
public class OrchestratorServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private OrchestratorService testingObject;
	
	@Mock
	private OrchestratorDriver orchestratorDriver;
	
	@Mock
	private OrchestratorFlexibleDriver orchestratorFlexibleDriver;
	
	@Mock
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	@Mock
	private IntraCloudProviderMatchmakingAlgorithm intraCloudProviderMatchmaker;
	
	@Mock
	private InterCloudProviderMatchmakingAlgorithm interCloudProviderMatchmaker;
	
	@Mock
	private CloudMatchmakingAlgorithm cloudMatchmaker;
	
	@Spy
	private final QoSManager qosManager = new DummyQoSManager();
	
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
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		
		final OrchestrationResponseDTO response = testingObject.externalServiceRequest(request);
		
		Assert.assertEquals(0, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalServiceRequestServiceTimeCalculation() {
		ReflectionTestUtils.setField(testingObject, "qosEnabled", true);
		ReflectionTestUtils.setField(testingObject, "maxReservationDuration", 3600);
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.ENABLE_QOS, true).
																				    flag(Flag.MATCHMAKING, true).
																				    command(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10").
																					build();
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		srEntry.setServiceDefinition(new ServiceDefinitionResponseDTO(1l, "test-service", null, null));
		srEntry.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1l, "test-interface", null, null)));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<List<OrchestrationResultDTO>> valueCapture = ArgumentCaptor.forClass(List.class);
		when(orchestratorDriver.generateAuthTokens(any(), valueCapture.capture())).thenReturn(List.of());
		
		testingObject.externalServiceRequest(request);
		
		final List<OrchestrationResultDTO> captured = valueCapture.getValue();
		Assert.assertEquals("15", captured.get(0).getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME)); // 15 due to 5 extra seconds are given
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalServiceRequestServiceTimeCalculationQoSDisabled() {
		ReflectionTestUtils.setField(testingObject, "qosEnabled", false);
		ReflectionTestUtils.setField(testingObject, "maxReservationDuration", 3600);
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.ENABLE_QOS, true).
																				    flag(Flag.MATCHMAKING, true).
																				    command(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10").
																					build();
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		srEntry.setServiceDefinition(new ServiceDefinitionResponseDTO(1l, "test-service", null, null));
		srEntry.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1l, "test-interface", null, null)));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<List<OrchestrationResultDTO>> valueCapture = ArgumentCaptor.forClass(List.class);
		when(orchestratorDriver.generateAuthTokens(any(), valueCapture.capture())).thenReturn(List.of());
		
		testingObject.externalServiceRequest(request);
		
		final List<OrchestrationResultDTO> captured = valueCapture.getValue();
		Assert.assertNull( captured.get(0).getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalServiceRequestServiceTimeCalculationQoSDisabledFlag() {
		ReflectionTestUtils.setField(testingObject, "qosEnabled", true);
		ReflectionTestUtils.setField(testingObject, "maxReservationDuration", 3600);
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.ENABLE_QOS, false).
																				    flag(Flag.MATCHMAKING, true).
																				    command(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "10").
																					build();
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		srEntry.setServiceDefinition(new ServiceDefinitionResponseDTO(1l, "test-service", null, null));
		srEntry.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1l, "test-interface", null, null)));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<List<OrchestrationResultDTO>> valueCapture = ArgumentCaptor.forClass(List.class);
		when(orchestratorDriver.generateAuthTokens(any(), valueCapture.capture())).thenReturn(List.of());
		
		testingObject.externalServiceRequest(request);
		
		final List<OrchestrationResultDTO> captured = valueCapture.getValue();
		Assert.assertNull( captured.get(0).getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExternalServiceRequestServiceTimeCalculationWihoutExcusivityRequest() {
		ReflectionTestUtils.setField(testingObject, "qosEnabled", true);
		ReflectionTestUtils.setField(testingObject, "maxReservationDuration", 3600);
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																  		build();
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.ENABLE_QOS, true).
																				    flag(Flag.MATCHMAKING, true).
																					build();
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		srEntry.setServiceDefinition(new ServiceDefinitionResponseDTO(1l, "test-service", null, null));
		srEntry.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1l, "test-interface", null, null)));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<List<OrchestrationResultDTO>> valueCapture = ArgumentCaptor.forClass(List.class);
		when(orchestratorDriver.generateAuthTokens(any(), valueCapture.capture())).thenReturn(List.of());
		
		testingObject.externalServiceRequest(request);
		
		final List<OrchestrationResultDTO> captured = valueCapture.getValue();
		Assert.assertNull( captured.get(0).getMetadata().get(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationRequestNull() {
		testingObject.dynamicOrchestration(null, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint1Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.OVERRIDE_STORE, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.dynamicOrchestration(request, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint2Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.TRIGGER_INTER_CLOUD, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.dynamicOrchestration(request, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testDynamicOrchestrationCrossParameterConstraint3Failed() {
		final OrchestrationFlags flags = new OrchestrationFlags();
		flags.put(Flag.ONLY_PREFERRED, true);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setOrchestrationFlags(flags);
		
		testingObject.dynamicOrchestration(request, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationServiceFormNull() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																					build();
		
		testingObject.dynamicOrchestration(request, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationServiceNameNull() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(new ServiceQueryFormDTO()).
																					build();
		
		testingObject.dynamicOrchestration(request, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDynamicOrchestrationServiceNameEmpty() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO();
		serviceForm.setServiceDefinitionRequirement(" ");
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																					build();
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		testingObject.dynamicOrchestration(request, false);
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
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(new ServiceQueryResultDTO());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, false);
		
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
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(List.of());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, false);
		
		Assert.assertEquals(0, result.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDynamicOrchestrationAuthorizationSkipped() {
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
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null);
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(List.of(serviceInterfaceResponseDTO));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(qosManager.filterReservedProviders(anyList(), any(SystemRequestDTO.class))).thenReturn(List.of());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, true);
		
		Assert.assertEquals(0, result.getResponse().size());
		
		verify(orchestratorDriver, times(1)).queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class));
		verify(orchestratorDriver, never()).queryAuthorization(any(SystemRequestDTO.class), anyList());
		verify(qosManager, times(1)).filterReservedProviders(anyList(), any(SystemRequestDTO.class));
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
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, false);
		
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
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null);
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(List.of(serviceInterfaceResponseDTO));
		final ServiceRegistryResponseDTO srEntry2 = new ServiceRegistryResponseDTO();
		srEntry2.setProvider(new SystemResponseDTO(2, "d", "e", 6, null, null, null, null));
		srEntry2.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry2.setInterfaces(List.of(serviceInterfaceResponseDTO));
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		srResult.getServiceQueryData().add(srEntry2);
		
		final OrchestrationResultDTO convertedSrEntry2 = new OrchestrationResultDTO(srEntry2.getProvider(), srEntry2.getServiceDefinition(), srEntry2.getServiceUri(), 
																					srEntry2.getSecure(), srEntry2.getMetadata(), srEntry2.getInterfaces(), srEntry2.getVersion());
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(intraCloudProviderMatchmaker.doMatchmaking(any(), any(IntraCloudProviderMatchmakingParameters.class))).thenReturn(convertedSrEntry2);
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, false);
		
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinition);
		srEntry.setInterfaces(interfaces);
		final ServiceRegistryResponseDTO srEntry2 = new ServiceRegistryResponseDTO();
		srEntry2.setProvider(new SystemResponseDTO(2, "d", "e", 6, null, null, null, null));
		srEntry2.setServiceDefinition(serviceDefinition);
		srEntry2.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		srResult.getServiceQueryData().add(srEntry2);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, false);
		
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinition);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.dynamicOrchestration(request, false);
		
		Assert.assertEquals(1, result.getResponse().size());
		Assert.assertEquals(1, result.getResponse().get(0).getProvider().getId());
		Assert.assertEquals(1, result.getResponse().get(0).getWarnings().size());
		Assert.assertEquals(OrchestratorWarnings.TTL_UNKNOWN, result.getResponse().get(0).getWarnings().get(0));
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTopPriorityEntriesOrchestrationProcessOk() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTopPriorityEntriesOrchestrationProcessNullEntryListOk() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
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
		
		final List<OrchestratorStore> entryList = null;
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTopPriorityEntriesOrchestrationProcessEmptyEntryListOk() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
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
		
		final List<OrchestratorStore> entryList = List.of();
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResponse().isEmpty());
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTopPriorityEntriesOrchestrationProcessEmptyCrossCheckedEntryListOk() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(List.of());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResponse().isEmpty());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithSystemIdParameterByNullIdOk() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces("HTTP-SECURE-JSON").
																		build();
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.orchestrationFromOriginalStore(request);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testTopPriorityEntriesOrchestrationProcessWithSystemIdParameterByNullRequest() {
		final OrchestrationFormRequestDTO request = null;
		final Long systemId = 1L;

		testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithSystemIdParameterByNullRequest() {
		final OrchestrationFormRequestDTO request = null;

		testingObject.orchestrationFromOriginalStore(request);
				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithSystemIdParameterByInvalidSystemId() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces("HTTP-SECURE-JSON").
																		build();

		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = -1L;

		testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreSystemNotInDB() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = null;
			
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = null;
				
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		
		testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithSystemIdParameterByIdBySomeForeignStoreEntriesOk() {
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																					build();
			
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.orchestrationFromOriginalStore(request);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithNullConsumerInOrchestrationRequestDTOOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces(serviceInterface.getInterfaceName()).
																		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		
		request.setRequesterSystem(null);
		
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri",	ServiceSecurityType.NOT_SECURE, Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
 		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.orchestrationFromOriginalStore(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithEmptyInterfaceNameInOrchestrationRequestDTOOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces(" ").
																		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE, Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.orchestrationFromOriginalStore(request);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithMoreThenOneInterfaceNameInOrchestrationRequestDTOOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																	    interfaces("HTTP-SECURE-JSON", "HTTP-SECURE-XML").
																	    build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.orchestrationFromOriginalStore(request);		
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithEmptyListOfInterfaceNameInOrchestrationRequestDTOOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces().
																		build();
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.orchestrationFromOriginalStore(request);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithNullInterfaceNameInOrchestrationRequestDTOOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces("HTTP-SECURE-JSON").
																		build();
		serviceForm.setInterfaceRequirements(null);
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.orchestrationFromOriginalStore(request);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromStoreWithEmptyServiceDefinitionInOrchestrationRequestDTOOk() {		
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces("  ").
																		build();
		
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO,	serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.orchestrationFromOriginalStore(request);		
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithForeignStoreEntriesOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces(serviceInterface.getInterfaceName()).
																		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE, Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of(orchestratorStoreForeign));
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO result = testingObject.orchestrationFromOriginalStore(request);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithNullStoreEntryListOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces(serviceInterface.getInterfaceName()).
																		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false,	null, null,	null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn( null );
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO result = testingObject.orchestrationFromOriginalStore(request);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromStoreWithEmptyStoreEntriesOk() {
		final ServiceDefinition serviceDefinition = new ServiceDefinition("serviceDefinition");
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").
																		interfaces(serviceInterface.getInterfaceName()).
																		build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
																				    requestedService(serviceForm).
																					build();
		final Long systemId = 1L;
			
		final System consumerSystem = new System();
		consumerSystem.setId(systemId);
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
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
		
		final List<OrchestratorStore> entryList = List.of(orchestratorStoreForeign);
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
		
		final CloudResponseDTO providerCloud1 = new CloudResponseDTO(1L, "operator1", "cloudname1", false, true, false, null, null, null);
		final OrchestratorStoreResponseDTO foreignStoreEntry = new OrchestratorStoreResponseDTO();
		foreignStoreEntry.setProviderCloud(providerCloud1);
		foreignStoreEntry.setProviderSystem(systemResponseDTO);
		foreignStoreEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		foreignStoreEntry.setServiceInterface(new ServiceInterfaceResponseDTO(1L, serviceInterface.getInterfaceName(), null, null));
		foreignStoreEntry.setForeign(true);
		
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO,	serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE, Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		when(orchestratorStoreDBService.getForeignResponseDTO( any( OrchestratorStore.class ) )).thenReturn(foreignStoreEntry);
		when(orchestratorDriver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(anyLong(), any(), any())).thenReturn(List.of());
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO result = testingObject.orchestrationFromOriginalStore(request);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTopPriorityEntriesOrchestrationProcessWithSystemIdParameterByIdBySREntryWithMoreThenOneInterfaceOk() {
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(provider).
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
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null), 
																	 new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-XML", null, null));
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.topPriorityEntriesOrchestrationProcess(request, systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudOk() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
	
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("cloud2name");
		providerCloud.setOperator("operator");
		
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri",	ServiceSecurityType.NOT_SECURE,	Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(!orchestrationResult.getResponse().isEmpty());
	}
	

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testTriggerInterCloudNullRequest() {		
		testingObject.triggerInterCloud(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudNullPreferredCloudsOk() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
	
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = null;
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO,	serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(!orchestrationResult.getResponse().isEmpty());
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudNullWarnings() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").	build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = null;
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(), List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(null);
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(!orchestrationResult.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudWithOtherWarnings() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = null;
		
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		final List<OrchestratorWarnings> warningList = new ArrayList<OrchestratorWarnings>(1);
		warningList.add(OrchestratorWarnings.TTL_UNKNOWN);
		orchestrationResultDTO.setWarnings(warningList);
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(!orchestrationResult.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudWithForeignWarningAlreadySet() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = null;
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false,	true, false, "", "", "");
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE, Map.of(), 
																						 interfaces, 1);
		final List<OrchestratorWarnings> warningList = new ArrayList<OrchestratorWarnings>(1);
		warningList.add(OrchestratorWarnings.FROM_OTHER_CLOUD);
		orchestrationResultDTO.setWarnings(warningList);
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(!orchestrationResult.getResponse().isEmpty());
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudWithNullICNResultDTO() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("cloud2name");
		providerCloud.setOperator("operator");
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(), List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = null;
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(orchestrationResult.getResponse().isEmpty());
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudWithMatchmakingFalse() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("cloud2name");
		providerCloud.setOperator("operator");
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, false).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(), List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(!orchestrationResult.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudNullTargetCloudOk() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
	
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = null;
		final CloudResponseDTO cloudResponseDTO = null;
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(orchestrationResult.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudNullCloudResponseNameOk() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = null;
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", null, false, true, false, "", "", "");		
		final PreferredProviderDataDTO preferredProviderDataDTO = new PreferredProviderDataDTO();
		preferredProviderDataDTO.setProviderSystem(provider);
		preferredProviderDataDTO.setProviderCloud(providerCloud);
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    preferredProviders(preferredProviderDataDTO).
																				    flag(Flag.MATCHMAKING, true).
																					build();

		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(), List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO,	serviceDefinitionResponseDTO, "serviceUri", ServiceSecurityType.NOT_SECURE,	Map.of(), 
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		final OrchestrationResponseDTO orchestrationResult = testingObject.triggerInterCloud(request);
		
		Assert.assertNotNull(orchestrationResult);
		Assert.assertTrue(orchestrationResult.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudQoSEnabled() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
	
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("cloud2name");
		providerCloud.setOperator("operator");
		
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.TRIGGER_INTER_CLOUD, true).
																				    flag(Flag.ENABLE_QOS, true).
																				    build();
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri",	ServiceSecurityType.NOT_SECURE,	Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final QoSMeasurementAttributesFormDTO qoSMeasurement = new QoSMeasurementAttributesFormDTO();
		qoSMeasurement.setServiceRegistryEntry(srEntry);
		
		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(qoSMeasurement), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.triggerInterCloud(request);
		
		verify(qosManager, times(1)).preVerifyInterCloudServices(any(), any());
		verify(qosManager, times(1)).verifyInterCloudServices(any(), any(), any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudQoSEnabledWithReservation() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
	
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("cloud2name");
		providerCloud.setOperator("operator");
		
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.TRIGGER_INTER_CLOUD, true).
																				    flag(Flag.ENABLE_QOS, true).
																				    flag(Flag.MATCHMAKING, true).
																				    command(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "20").
																					build();
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri",	ServiceSecurityType.NOT_SECURE,	Map.of(),
																						 interfaces, 1);
		orchestrationResultDTO.setWarnings(new ArrayList<>());
		
		final QoSMeasurementAttributesFormDTO qoSMeasurement = new QoSMeasurementAttributesFormDTO();
		qoSMeasurement.setServiceRegistryEntry(srEntry);
		
		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(qoSMeasurement), Map.of(), false);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.triggerInterCloud(request);
		
		verify(qosManager, times(1)).preVerifyInterCloudServices(any(), any());
		verify(qosManager, times(0)).verifyInterCloudServices(any(), any(), any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTriggerInterCloudQoSEnabledWithGatewayMandatory() {
		final ServiceInterface serviceInterface = new ServiceInterface("HTTP-SECURE-JSON");
		
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(3, "service", null, null);
		final List<ServiceInterfaceResponseDTO> interfaces = List.of(new ServiceInterfaceResponseDTO(4, "HTTP-SECURE-JSON", null, null));
	
		final ServiceQueryFormDTO serviceForm = new ServiceQueryFormDTO.Builder("service").build();
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("cloud2name");
		providerCloud.setOperator("operator");
		
		final CloudResponseDTO cloudResponseDTO = new CloudResponseDTO(1L, "operator", "cloud2name", false, true, false, "", "", "");
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO.Builder(new SystemRequestDTO()).
																				    requestedService(serviceForm).
																				    flag(Flag.TRIGGER_INTER_CLOUD, true).
																				    flag(Flag.ENABLE_QOS, true).
																				    build();
		
		final System consumerSystem = new System();
		consumerSystem.setSystemName("consumerSystemName");
		consumerSystem.setAddress("localhost");
		consumerSystem.setPort(1234);
		
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
			
		final OrchestrationResultDTO orchestrationResultDTO = new OrchestrationResultDTO(systemResponseDTO, serviceDefinitionResponseDTO, "serviceUri",	ServiceSecurityType.NOT_SECURE,	Map.of(),
																						 interfaces, 1);
		final List<OrchestratorWarnings> warnings = new ArrayList<>();
		warnings.add(OrchestratorWarnings.VIA_GATEWAY);
		orchestrationResultDTO.setWarnings(warnings);
		
		final QoSMeasurementAttributesFormDTO qoSMeasurement = new QoSMeasurementAttributesFormDTO();
		qoSMeasurement.setServiceRegistryEntry(srEntry);
		
		final GSDPollResponseDTO gsdPollResponseDTO = new GSDPollResponseDTO(cloudResponseDTO, serviceDefinitionResponseDTO.getServiceDefinition(),	List.of(serviceInterface.getInterfaceName()), 
																			 1,	List.of(qoSMeasurement), Map.of(), true);
		final GSDQueryResultDTO gsdResult = new GSDQueryResultDTO(List.of(gsdPollResponseDTO), 0);
		
		final ICNResultDTO icnResultDTO = new ICNResultDTO(List.of(orchestrationResultDTO));
		    
		when(orchestratorDriver.doGlobalServiceDiscovery(any(GSDQueryFormDTO.class))).thenReturn(gsdResult);		
		when(cloudMatchmaker.doMatchmaking(any(CloudMatchmakingParameters.class))).thenReturn(cloudResponseDTO);
		when(orchestratorDriver.doInterCloudNegotiation(any(ICNRequestFormDTO.class))).thenReturn(icnResultDTO);
		when(interCloudProviderMatchmaker.doMatchmaking(any(InterCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchestrationResultDTO)));
	
		testingObject.triggerInterCloud(request);
		
		verify(qosManager, times(1)).preVerifyInterCloudServices(any(), any());
		verify(qosManager, times(0)).verifyInterCloudServices(any(), any(), any(), any());
	}
		
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStoreOchestrationProcessResponseOk() {
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
		srEntry.setProvider(new SystemResponseDTO(1, "a", "b", 3, null, null, null, null));
		srEntry.setServiceDefinition(serviceDefinitionResponseDTO);
		srEntry.setInterfaces(interfaces);
		final ServiceQueryResultDTO srResult = new ServiceQueryResultDTO();
		srResult.getServiceQueryData().add(srEntry);
		
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(consumerSystem);
				
		when(orchestratorDriver.queryServiceRegistryBySystemId(anyLong())).thenReturn(systemResponseDTO);
		when(orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(anyLong())).thenReturn(entryList);
		when(orchestratorDriver.queryServiceRegistry(any(ServiceQueryFormDTO.class), any(OrchestrationFlags.class))).thenReturn(srResult);
		when(orchestratorDriver.queryAuthorization(any(SystemRequestDTO.class), any())).thenReturn(srResult.getServiceQueryData());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), any())).thenCallRealMethod();
		
		final OrchestrationResponseDTO result = testingObject.storeOchestrationProcessResponse(systemId);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(!result.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testStoreOchestrationProcessInvalidId() {
		testingObject.storeOchestrationProcessResponse(-1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testLockProvidersTemporarilyOk() {
		final QoSTemporaryLockRequestDTO request = new QoSTemporaryLockRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		request.setRequester(requesterSystem);
		request.setOrList(List.of(new OrchestrationResultDTO()));
		
		when(qosManager.reserveProvidersTemporarily(any(), any())).thenReturn(request.getOrList());
		
		final QoSTemporaryLockResponseDTO result = testingObject.lockProvidersTemporarily(request);
		
		assertEquals(request.getOrList().size(), result.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLockProvidersTemporarilyNullRequest() {
		testingObject.lockProvidersTemporarily(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLockProvidersTemporarilyNullRequesterSystemName() {
		final QoSTemporaryLockRequestDTO request = new QoSTemporaryLockRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(null);
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		request.setRequester(requesterSystem);
		request.setOrList(List.of(new OrchestrationResultDTO()));
		
		testingObject.lockProvidersTemporarily(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLockProvidersTemporarilyBlankRequesterSystemName() {
		final QoSTemporaryLockRequestDTO request = new QoSTemporaryLockRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		request.setRequester(requesterSystem);
		request.setOrList(List.of(new OrchestrationResultDTO()));
		
		testingObject.lockProvidersTemporarily(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLockProvidersTemporarilyNullRequesterSystemAddress() {
		final QoSTemporaryLockRequestDTO request = new QoSTemporaryLockRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress(null);
		requesterSystem.setPort(10000);
		request.setRequester(requesterSystem);
		request.setOrList(List.of(new OrchestrationResultDTO()));
		
		testingObject.lockProvidersTemporarily(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLockProvidersTemporarilyBlankRequesterSystemAddress() {
		final QoSTemporaryLockRequestDTO request = new QoSTemporaryLockRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("");
		requesterSystem.setPort(10000);
		request.setRequester(requesterSystem);
		request.setOrList(List.of(new OrchestrationResultDTO()));
		
		testingObject.lockProvidersTemporarily(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLockProvidersTemporarilyNullRequesterSystemPort() {
		final QoSTemporaryLockRequestDTO request = new QoSTemporaryLockRequestDTO();
		
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(null);
		request.setRequester(requesterSystem);
		request.setOrList(List.of(new OrchestrationResultDTO()));
		
		testingObject.lockProvidersTemporarily(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void confirmProviderReservationOk() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("requester-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));
		
		testingObject.confirmProviderReservation(request);		
		verify(qosManager, times(1)).confirmReservation(any(), any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationNullRequesterSystemName() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName(null);
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationBlankRequesterSystemName() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationNullRequesterSystemAddress() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("selected-system");
		requesterSystem.setAddress(null);
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationBlankRequesterSystemAddress() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("selected-system");
		requesterSystem.setAddress("");
		requesterSystem.setPort(10000);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationNullRequesterSystemPort() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("selected-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(null);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationNullSelected() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("selected-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(null, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationNullSelectedSystem() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("selected-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(10000);
		
		final ServiceDefinitionResponseDTO requestedService = new ServiceDefinitionResponseDTO(1, "requested-service", null, null);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(null);
		selectedResult.setService(requestedService);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void confirmProviderReservationNullRequestedService() {
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setSystemName("selected-system");
		requesterSystem.setAddress("1.1.1.1");
		requesterSystem.setPort(null);
		
		final SystemResponseDTO selectedSystem = new SystemResponseDTO();
		selectedSystem.setId(1);
		selectedSystem.setSystemName("selected-system");
		selectedSystem.setAddress("2.2.2.2");
		selectedSystem.setPort(20000);
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(selectedSystem);
		selectedResult.setService(null);
		
		final QoSReservationRequestDTO request = new QoSReservationRequestDTO(selectedResult, requesterSystem, List.of(new OrchestrationResultDTO()));		
		testingObject.confirmProviderReservation(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromFlexibleStoreNullRequest() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		try {
			testingObject.orchestrationFromStore(null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Request is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testOrchestrationFromFlexibleStoreFlagObjectNull() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			ReflectionTestUtils.setField(request, "orchestrationFlags", null);
			testingObject.orchestrationFromStore(request);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Flags map is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromFlexibleStoreIntercloudNotSupported1() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			request.getOrchestrationFlags().put(Flag.ENABLE_INTER_CLOUD, true);
			testingObject.orchestrationFromStore(request);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Intercloud mode is not supported yet.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromFlexibleStoreIntercloudNotSupported2() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			request.getOrchestrationFlags().put(Flag.TRIGGER_INTER_CLOUD, true);
			testingObject.orchestrationFromStore(request);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Intercloud mode is not supported yet.", ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testOrchestrationFromFlexibleStoreQoSNotSupported2() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			request.getOrchestrationFlags().put(Flag.ENABLE_QOS, true);
			testingObject.orchestrationFromStore(request);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Quality of Service requirements is not supported yet.", ex.getMessage());
			throw ex;
		}
	}
	
	// checkSystemRequestDTO() and checkServiceRequestForm() used in dynamic orchestration too and are tested there
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testOrchestrationFromFlexibleStoreOnlyCloudPreferred() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		try {
			final SystemRequestDTO consumer = new SystemRequestDTO("testconsumer", "localhost", 1234, null, null);
			final ServiceQueryFormDTO queryForm = new ServiceQueryFormDTO.Builder("testservice")
																		 .build();
			
			final PreferredProviderDataDTO preferredProvider = new PreferredProviderDataDTO();
			preferredProvider.setProviderSystem(new SystemRequestDTO("testprovider", "localhost", 5678, null, null));
			preferredProvider.setProviderCloud(new CloudRequestDTO());
			final List<PreferredProviderDataDTO> preferredProviderList = new ArrayList<>(1);
			preferredProviderList.add(preferredProvider);
			
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			request.setRequesterSystem(consumer);
			request.setRequestedService(queryForm);
			request.setPreferredProviders(preferredProviderList);
			request.getOrchestrationFlags().put(Flag.ONLY_PREFERRED, true);
			testingObject.orchestrationFromStore(request);
		} catch (final BadPayloadException ex) {
			Assert.assertEquals("There is no valid preferred provider, but \"" + Flag.ONLY_PREFERRED + "\" is set to true", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromFlexibleStoreNoMatchedRules() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		final SystemRequestDTO consumer = new SystemRequestDTO("testconsumer", "localhost", 1234, null, null);
		final ServiceQueryFormDTO queryForm = new ServiceQueryFormDTO.Builder("testservice")
																	 .build();
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(consumer);
		request.setRequestedService(queryForm);
		
		when(orchestratorFlexibleDriver.queryConsumerSystem(any(SystemRequestDTO.class))).thenReturn(new SystemResponseDTO());
		when(orchestratorFlexibleDriver.collectAndSortMatchingRules(any(OrchestrationFormRequestDTO.class), any(SystemResponseDTO.class))).thenReturn(List.of());
		
		final OrchestrationResponseDTO response = testingObject.orchestrationFromStore(request);
		Assert.assertEquals(0, response.getResponse().size());
		verify(orchestratorFlexibleDriver).queryConsumerSystem(any(SystemRequestDTO.class));
		verify(orchestratorFlexibleDriver).collectAndSortMatchingRules(any(OrchestrationFormRequestDTO.class), any(SystemResponseDTO.class));
		verify(orchestratorFlexibleDriver, never()).queryServiceRegistry(any(OrchestrationFormRequestDTO.class), anyList());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromFlexibleStoreNoMatchedResultByProviderRequirements() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		final SystemRequestDTO consumer = new SystemRequestDTO("testconsumer", "localhost", 1234, null, null);
		final ServiceQueryFormDTO queryForm = new ServiceQueryFormDTO.Builder("testservice")
																	 .build();
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(consumer);
		request.setRequestedService(queryForm);
		
		when(orchestratorFlexibleDriver.queryConsumerSystem(any(SystemRequestDTO.class))).thenReturn(new SystemResponseDTO());
		when(orchestratorFlexibleDriver.collectAndSortMatchingRules(any(OrchestrationFormRequestDTO.class), any(SystemResponseDTO.class))).thenReturn(List.of(new OrchestratorStoreFlexible()));
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryWithRules = List.of(new ImmutablePair<>(new OrchestratorStoreFlexible(), new ServiceQueryResultDTO()));
		when(orchestratorFlexibleDriver.queryServiceRegistry(any(OrchestrationFormRequestDTO.class), anyList())).thenReturn(queryWithRules);
		when(orchestratorFlexibleDriver.filterSRResultsByProviderRequirements(anyList(), any())).thenReturn(List.of());
		
		final OrchestrationResponseDTO response = testingObject.orchestrationFromStore(request);
		Assert.assertEquals(0, response.getResponse().size());
		verify(orchestratorFlexibleDriver).queryConsumerSystem(any(SystemRequestDTO.class));
		verify(orchestratorFlexibleDriver).collectAndSortMatchingRules(any(OrchestrationFormRequestDTO.class), any(SystemResponseDTO.class));
		verify(orchestratorFlexibleDriver).queryServiceRegistry(any(OrchestrationFormRequestDTO.class), anyList());
		verify(orchestratorFlexibleDriver).filterSRResultsByProviderRequirements(anyList(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrchestrationFromFlexibleStoreWithMatchmaking() {
		ReflectionTestUtils.setField(testingObject, "useFlexibleStore", true);
		final SystemRequestDTO consumer = new SystemRequestDTO("testconsumer", "localhost", 1234, null, null);
		final ServiceQueryFormDTO queryForm = new ServiceQueryFormDTO.Builder("testservice")
																	 .build();
		
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		request.setRequesterSystem(consumer);
		request.setRequestedService(queryForm);
		request.getOrchestrationFlags().put(Flag.MATCHMAKING, true);
		
		when(orchestratorFlexibleDriver.queryConsumerSystem(any(SystemRequestDTO.class))).thenReturn(new SystemResponseDTO());
		when(orchestratorFlexibleDriver.collectAndSortMatchingRules(any(OrchestrationFormRequestDTO.class), any(SystemResponseDTO.class))).thenReturn(List.of(new OrchestratorStoreFlexible()));
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryWithRules = List.of(new ImmutablePair<>(new OrchestratorStoreFlexible(), new ServiceQueryResultDTO()));
		when(orchestratorFlexibleDriver.queryServiceRegistry(any(OrchestrationFormRequestDTO.class), anyList())).thenReturn(queryWithRules);
		final ServiceRegistryResponseDTO srEntry = new ServiceRegistryResponseDTO();
		srEntry.setProvider(new SystemResponseDTO());
		srEntry.setServiceDefinition(new ServiceDefinitionResponseDTO());
		srEntry.setInterfaces(List.of(new ServiceInterfaceResponseDTO()));
		when(orchestratorFlexibleDriver.filterSRResultsByProviderRequirements(anyList(), any())).thenReturn(List.of(srEntry));
		when(intraCloudProviderMatchmaker.doMatchmaking(anyList(), any(IntraCloudProviderMatchmakingParameters.class))).thenReturn(new OrchestrationResultDTO());
		when(orchestratorDriver.generateAuthTokens(any(OrchestrationFormRequestDTO.class), anyList())).thenAnswer(new Answer<List<OrchestrationResultDTO>>() {
			@SuppressWarnings("unchecked")
			public List<OrchestrationResultDTO> answer(final InvocationOnMock invocation) throws Throwable {
				return (List<OrchestrationResultDTO>) invocation.getArgument(1);
			}
		});
		
		final OrchestrationResponseDTO response = testingObject.orchestrationFromStore(request);
		Assert.assertEquals(1, response.getResponse().size());
		verify(orchestratorFlexibleDriver).queryConsumerSystem(any(SystemRequestDTO.class));
		verify(orchestratorFlexibleDriver).collectAndSortMatchingRules(any(OrchestrationFormRequestDTO.class), any(SystemResponseDTO.class));
		verify(orchestratorFlexibleDriver).queryServiceRegistry(any(OrchestrationFormRequestDTO.class), anyList());
		verify(orchestratorFlexibleDriver).filterSRResultsByProviderRequirements(anyList(), any());
		verify(intraCloudProviderMatchmaker).doMatchmaking(anyList(), any(IntraCloudProviderMatchmakingParameters.class));
		verify(orchestratorDriver).generateAuthTokens(any(OrchestrationFormRequestDTO.class), anyList());
	}
}