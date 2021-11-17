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

package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationResponseDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.service.matchmaking.ICNProviderMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.ICNProviderMatchmakingParameters;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;

@RunWith(SpringRunner.class)
public class GatekeeperServiceICNTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperService testingObject;
	
	@Mock
	private GatekeeperDriver gatekeeperDriver;
	
	@Mock
	private CommonDBService commonDBService;
	
	@Mock
	private GatekeeperDBService gatekeeperDBService;
	
	@Mock
	private RelayMatchmakingAlgorithm gatewayMatchmaker;
	
	@Mock
	private ICNProviderMatchmakingAlgorithm icnProviderMatchmaker;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNFormNull() {
		testingObject.initICN(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequestedServiceNull() {
		testingObject.initICN(new ICNRequestFormDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequestedServiceDefinitionNull() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		form.setRequestedService(new ServiceQueryFormDTO());
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequestedServiceDefinitionEmpty() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement(" ");
		form.setRequestedService(requestedService);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNTargetCloudIdNull() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNTargetCloudIdInvalid() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(-1L);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemNull() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemNameNull() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setSystemName(null);
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemNameEmpty() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setSystemName("\t");
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemAddressNull() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setAddress(null);
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemAddressEmpty() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setAddress("");
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemPortNull() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(null);
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemPortTooLow() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(-100);
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitICNRequesterSystemPortTooHigh() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(123456);
		form.setRequesterSystem(system);
		
		testingObject.initICN(form);
	}
	
	// we skip tests about preferred system validation because it uses the same method as requester system validation
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitICNEverythingOKButNoResult() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		form.setRequesterSystem(system);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud1", false, true, false, "abcd");
		targetCloud.setGatewayRelays(Set.of());
		when(gatekeeperDBService.getCloudById(anyLong())).thenReturn(targetCloud);
		final Cloud ownCloud = new Cloud("aitia", "testcloud2", false, false, true, "efgh");
		when(commonDBService.getOwnCloud(anyBoolean())).thenReturn(ownCloud);
		when(gatekeeperDriver.sendICNProposal(any(Cloud.class), any(ICNProposalRequestDTO.class))).thenReturn(new ICNProposalResponseDTO());
		
		final ICNResultDTO icnResult = testingObject.initICN(form);
		
		Assert.assertEquals(0, icnResult.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitICNEverythingOKWithoutGateway() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		form.setRequesterSystem(system);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud1", false, true, false, "abcd");
		targetCloud.setGatewayRelays(Set.of());
		when(gatekeeperDBService.getCloudById(anyLong())).thenReturn(targetCloud);
		final Cloud ownCloud = new Cloud("aitia", "testcloud2", false, false, true, "efgh");
		when(commonDBService.getOwnCloud(anyBoolean())).thenReturn(ownCloud);
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		when(gatekeeperDriver.sendICNProposal(any(Cloud.class), any(ICNProposalRequestDTO.class))).thenReturn(new ICNProposalResponseDTO(List.of(resultDTO)));
		
		final ICNResultDTO icnResult = testingObject.initICN(form);
		
		Assert.assertEquals(1, icnResult.getResponse().size());
		Assert.assertEquals(resultDTO, icnResult.getResponse().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitICNEverythingOKWithGateway() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		form.setRequesterSystem(system);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud1", false, true, false, "abcd");
		targetCloud.setGatewayRelays(Set.of());
		when(gatekeeperDBService.getCloudById(anyLong())).thenReturn(targetCloud);
		final Cloud ownCloud = new Cloud("aitia", "testcloud2", false, false, true, "efgh");
		when(commonDBService.getOwnCloud(anyBoolean())).thenReturn(ownCloud);
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO());
		final RelayResponseDTO relay = new RelayResponseDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType(RelayType.GATEWAY_RELAY);
		final ICNProposalResponseDTO icnResponse = new ICNProposalResponseDTO(resultDTO, relay, new GatewayProviderConnectionResponseDTO());
		when(gatekeeperDriver.sendICNProposal(any(Cloud.class), any(ICNProposalRequestDTO.class))).thenReturn(icnResponse);
		when(gatekeeperDriver.connectConsumer(any(GatewayConsumerConnectionRequestDTO.class))).thenReturn(33333);
		when(gatekeeperDriver.getGatewayHost()).thenReturn("127.0.0.1");
		when(gatekeeperDBService.getRelayByAddressAndPort(anyString(), anyInt())).thenReturn(new Relay());
		
		final ICNResultDTO icnResult = testingObject.initICN(form);
		
		Assert.assertEquals(1, icnResult.getResponse().size());
		Assert.assertEquals(CoreSystem.GATEWAY.name().toLowerCase(), icnResponse.getResponse().get(0).getProvider().getSystemName());
		Assert.assertEquals("127.0.0.1", icnResponse.getResponse().get(0).getProvider().getAddress());
		Assert.assertEquals(33333, icnResponse.getResponse().get(0).getProvider().getPort());
		Assert.assertTrue(icnResult.getResponse().get(0).getWarnings().contains(OrchestratorWarnings.VIA_GATEWAY));
		
		verify(gatekeeperDBService, times(1)).getRelayByAddressAndPort("localhost", 1234);
		verify(gatekeeperDBService, never()).getRelayByAuthenticationInfo(anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitICNEverythingOKWithGatewayAndRelayAuthenticationInfo() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		form.setRequesterSystem(system);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud1", false, true, false, "abcd");
		targetCloud.setGatewayRelays(Set.of());
		when(gatekeeperDBService.getCloudById(anyLong())).thenReturn(targetCloud);
		final Cloud ownCloud = new Cloud("aitia", "testcloud2", false, false, true, "efgh");
		when(commonDBService.getOwnCloud(anyBoolean())).thenReturn(ownCloud);
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO());
		final RelayResponseDTO relay = new RelayResponseDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setAuthenticationInfo("test");
		relay.setType(RelayType.GATEWAY_RELAY);
		final ICNProposalResponseDTO icnResponse = new ICNProposalResponseDTO(resultDTO, relay, new GatewayProviderConnectionResponseDTO());
		when(gatekeeperDriver.sendICNProposal(any(Cloud.class), any(ICNProposalRequestDTO.class))).thenReturn(icnResponse);
		when(gatekeeperDriver.connectConsumer(any(GatewayConsumerConnectionRequestDTO.class))).thenReturn(33333);
		when(gatekeeperDriver.getGatewayHost()).thenReturn("127.0.0.1");
		when(gatekeeperDBService.getRelayByAuthenticationInfo("test")).thenReturn(new Relay());
		
		final ICNResultDTO icnResult = testingObject.initICN(form);
		
		Assert.assertEquals(1, icnResult.getResponse().size());
		Assert.assertEquals(CoreSystem.GATEWAY.name().toLowerCase(), icnResponse.getResponse().get(0).getProvider().getSystemName());
		Assert.assertEquals("127.0.0.1", icnResponse.getResponse().get(0).getProvider().getAddress());
		Assert.assertEquals(33333, icnResponse.getResponse().get(0).getProvider().getPort());
		Assert.assertTrue(icnResult.getResponse().get(0).getWarnings().contains(OrchestratorWarnings.VIA_GATEWAY));

		verify(gatekeeperDBService, never()).getRelayByAddressAndPort(anyString(), anyInt());
		verify(gatekeeperDBService, times(1)).getRelayByAuthenticationInfo("test");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequestNull() {
		testingObject.doICN(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequestedServiceNull() {
		testingObject.doICN(new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testDoICNRequestedCloudWantsToUseGatewayButRequesterCloudDoesNotSupportedGateways() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", true);
		
		testingObject.doICN(new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testDoICNRequestedCloudWantsToUseGatewayButNoRelaysAreProvided() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", true);
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		request.setGatewayIsPresent(true);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequestedServiceNameNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		request.setRequestedService(requestedService);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequestedServiceNameBlank() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement(" \n");
		request.setRequestedService(requestedService);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemNameNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setSystemName(null);
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemNameBlank() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setSystemName("");
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemAddressNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setAddress(null);
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemAddressBlank() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setAddress("");
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemPortNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(null);
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemPortTooLow() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(-2);
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterSystemPortTooHigh() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(123456);
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterCloudNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterCloudOperatorNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		cloud.setOperator(null);
		request.setRequesterCloud(cloud);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterCloudOperatorBlank() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		cloud.setOperator("   ");
		request.setRequesterCloud(cloud);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterCloudNameNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		cloud.setName(null);
		request.setRequesterCloud(cloud);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNRequesterCloudNameBlank() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		cloud.setName("");
		request.setRequesterCloud(cloud);
		
		testingObject.doICN(request);
	}

	// we skip tests about preferred system validation because it uses the same method as requester system validation
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final List<RelayRequestDTO> preferredRelays = new ArrayList<>(1);
		preferredRelays.add(null);
		request.setPreferredGatewayRelays(preferredRelays);
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayAddressNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setAddress(null);
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayAddressEmpty() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setAddress("");
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayPortNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setPort(null);
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayPortTooLow() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setPort(-273);
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayPortHigh() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setPort(101010);
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayTypeNull() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setType(null);
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayTypeEmpty() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setType("\r");
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayTypeInvalid1() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setType("Not a type");
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNPreferredRelayTypeInvalid2() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setType(RelayType.GATEKEEPER_RELAY.name());
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	// we skip tests about known relay validation because it uses the same method as preferred relay validation
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNConsumerGWPublicKeyNull() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", true);
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		request.setGatewayIsPresent(true);
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setType(RelayType.GATEWAY_RELAY.name());
		request.setPreferredGatewayRelays(List.of(relay));
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoICNConsumerGWPublicKeyEmpty() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", true);
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		request.setGatewayIsPresent(true);
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		relay.setType(RelayType.GATEWAY_RELAY.name());
		request.setPreferredGatewayRelays(List.of(relay));
		request.setConsumerGatewayPublicKey(" ");
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNOrchestratorEmptyResponse() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO());
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertTrue(response.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNNoAccess() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(new OrchestrationResponseDTO());
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertTrue(response.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNEverythingOKWithoutGateway() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(new ArrayList<>());
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(1, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testDoICNWithGatewayNoCommonRelay() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", true);
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequesterSystem(new SystemRequestDTO());
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.setConsumerGatewayPublicKey("consumerGWPublicKey");
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDBService.getCloudByOperatorAndName(any(String.class), any(String.class))).thenReturn(new Cloud());
		when(gatewayMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(null);
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(new ArrayList<>());
		
		testingObject.doICN(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testDoICNEverythingOKWithGateway() {
		ReflectionTestUtils.setField(testingObject, "gatewayIsMandatory", true);
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		request.setGatewayIsPresent(true);
		request.setRequesterSystem(new SystemRequestDTO());
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.setConsumerGatewayPublicKey("consumerGWPublicKey");
		
		final OrchestrationResultDTO selectedResult = new OrchestrationResultDTO();
		selectedResult.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		selectedResult.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResultDTO otherResult = new OrchestrationResultDTO();
		otherResult.setProvider(new SystemResponseDTO(2, "test-sys", "0.0.0.0", 78058, null, null, null, null));
		otherResult.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(selectedResult, otherResult));
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(new ArrayList<>());
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDBService.getCloudByOperatorAndName(any(String.class), any(String.class))).thenReturn(new Cloud());
		final Relay selectedRelay = new Relay("localhost", 1234, false, false, RelayType.GATEWAY_RELAY);
		selectedRelay.setCreatedAt(ZonedDateTime.now());
		selectedRelay.setUpdatedAt(ZonedDateTime.now());
		when(gatewayMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(selectedRelay);
		when(icnProviderMatchmaker.doMatchmaking(any(List.class), any(ICNProviderMatchmakingParameters.class))).thenReturn(selectedResult);
		when(commonDBService.getOwnCloud(anyBoolean())).thenReturn(new Cloud());
		final GatewayProviderConnectionResponseDTO connectionResponseDTO = new GatewayProviderConnectionResponseDTO("queueId", "peerName", "providerGWPublicKey");
		when(gatekeeperDriver.connectProvider(any(GatewayProviderConnectionRequestDTO.class))).thenReturn(connectionResponseDTO);
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(1, response.getResponse().size());
		Assert.assertNotNull(response.getRelay());
		Assert.assertEquals("localhost", response.getRelay().getAddress());
		Assert.assertEquals(1234, response.getRelay().getPort());
		Assert.assertNotNull(response.getConnectionInfo());
		Assert.assertEquals("queueId", response.getConnectionInfo().getQueueId());
		Assert.assertEquals("peerName", response.getConnectionInfo().getPeerName());
		Assert.assertEquals("providerGWPublicKey", response.getConnectionInfo().getProviderGWPublicKey());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNQoSRequiredButNotEnabled() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.getNegotiationFlags().put(Flag.ENABLE_QOS, true);
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(false);
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(0, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNNeedReservationButTemporaryLockNotPossible() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.getNegotiationFlags().put(Flag.ENABLE_QOS, true);
		request.getCommands().put(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "15");
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.sendQoSTemporaryLockRequest(any())).thenReturn(new QoSTemporaryLockResponseDTO());		
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(0, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNWithQoSButNoAvailableMeasurements() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.getNegotiationFlags().put(Flag.ENABLE_QOS, true);
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.getQoSIntraPingMeasurementsForLocalSystem(anyLong())).thenReturn(new QoSIntraPingMeasurementResponseDTO());
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(0, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNWithoutGatwayAndReservationButAllProviderReserved() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.getNegotiationFlags().put(Flag.ENABLE_QOS, false);
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		
		final QoSReservationResponseDTO reservationResponseDTO = new QoSReservationResponseDTO();
		reservationResponseDTO.setReservedProviderId(resultDTO.getProvider().getId());
		reservationResponseDTO.setReservedServiceId(resultDTO.getService().getId());
		reservationResponseDTO.setReservedTo(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusMinutes(15)));
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(List.of(reservationResponseDTO));
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(0, response.getResponse().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNWithoutGatwayAndReservationProviderNotReserved() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.getNegotiationFlags().put(Flag.ENABLE_QOS, false);
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.getQoSReservationList()).thenReturn(List.of());
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		Assert.assertEquals(1, response.getResponse().size());
		Assert.assertEquals(resultDTO.getProvider().getSystemName(), response.getResponse().get(0).getProvider().getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNWithoutGatewayButNeedReservation() {
		final ICNProposalRequestDTO request = new ICNProposalRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		request.setRequestedService(requestedService);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		request.setRequesterSystem(system);
		final CloudRequestDTO cloud = getTestCloudRequestDTO();
		request.setRequesterCloud(cloud);
		final RelayRequestDTO relay = getTestRelayDTO();
		request.setPreferredGatewayRelays(List.of(relay));
		request.getNegotiationFlags().put(Flag.ENABLE_QOS, true);
		request.getCommands().put(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY, "15");
		
		final OrchestrationResultDTO resultDTO = new OrchestrationResultDTO();
		resultDTO.setProvider(new SystemResponseDTO(1, "test-sys", "0.0.0.0", 50058, null, null, null, null));
		resultDTO.setService(new ServiceDefinitionResponseDTO(1, "test-service", null, null));
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(resultDTO));
		
		when(gatekeeperDriver.checkQoSEnabled()).thenReturn(true);
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.sendQoSTemporaryLockRequest(any())).thenReturn(new QoSTemporaryLockResponseDTO(List.of(resultDTO)));
		when(gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		when(icnProviderMatchmaker.doMatchmaking(any(), any())).thenReturn(resultDTO);
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		
		verify(icnProviderMatchmaker, times(1)).doMatchmaking(any(), any());
		Assert.assertEquals(1, response.getResponse().size());
		Assert.assertEquals(resultDTO.getProvider().getSystemName(), response.getResponse().get(0).getProvider().getSystemName());
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO getTestSystemRequestDTO() {
		final SystemRequestDTO result = new SystemRequestDTO();
		result.setSystemName("system1");
		result.setAddress("localhost");
		result.setPort(1234);
		result.setAuthenticationInfo("abcd");
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getTestCloudRequestDTO() {
		final CloudRequestDTO result = new CloudRequestDTO();
		result.setOperator("aitia");
		result.setName("testcloud1");
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private RelayRequestDTO getTestRelayDTO() {
		return new RelayRequestDTO("localhost", 1234, null, true, true, RelayType.GATEWAY_RELAY.name());
	}
}