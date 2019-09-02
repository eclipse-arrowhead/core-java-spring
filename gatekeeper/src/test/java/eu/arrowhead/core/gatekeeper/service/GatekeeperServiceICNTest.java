package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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

import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.ICNRequestFormDTO;
import eu.arrowhead.common.dto.ICNResultDTO;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayResponseDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

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
		RelayResponseDTO relay = new RelayResponseDTO();
		relay.setType(RelayType.GATEWAY_RELAY);
		final ICNProposalResponseDTO icnResponse = new ICNProposalResponseDTO(resultDTO, relay, new GatewayProviderConnectionResponseDTO());
		
		when(gatekeeperDriver.sendICNProposal(any(Cloud.class), any(ICNProposalRequestDTO.class))).thenReturn(icnResponse);
		when(gatekeeperDriver.connectConsumer(any(GatewayConsumerConnectionRequestDTO.class))).thenReturn(33333);
		when(gatekeeperDriver.getGatewayHost()).thenReturn("127.0.0.1");
		
		final ICNResultDTO icnResult = testingObject.initICN(form);
		Assert.assertEquals(1, icnResult.getResponse().size());
		Assert.assertEquals(CoreSystem.GATEWAY.name().toLowerCase(), icnResponse.getResponse().get(0).getProvider().getSystemName());
		Assert.assertEquals("127.0.0.1", icnResponse.getResponse().get(0).getProvider().getAddress());
		Assert.assertEquals(33333, icnResponse.getResponse().get(0).getProvider().getPort());
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
		ICNProposalRequestDTO request = new ICNProposalRequestDTO();
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
	public void testDoICNOrchestratorNoAccess() {
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
		
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO()));
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(new OrchestrationResponseDTO());
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		Assert.assertTrue(response.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoICNOrchestratorEverythingOK() {
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
		
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO()));
		when(gatekeeperDriver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(orchestrationResponse);
		when(gatekeeperDriver.queryAuthorizationBasedOnOchestrationResponse(any(CloudRequestDTO.class), any(OrchestrationResponseDTO.class))).thenReturn(orchestrationResponse);
		
		final ICNProposalResponseDTO response = testingObject.doICN(request);
		Assert.assertEquals(1, response.getResponse().size());
	}
	
	//TODO: additional test cases here (when using gateway)

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
		return new RelayRequestDTO("localhost", 1234, true, true, RelayType.GATEWAY_RELAY.name());
	}
}