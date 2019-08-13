package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.ICNRequestFormDTO;
import eu.arrowhead.common.dto.ICNResultDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
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
	public void testInitICNEverythingOKWithoutGateway() {
		final ICNRequestFormDTO form = new ICNRequestFormDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("test-service");
		form.setRequestedService(requestedService);
		form.setTargetCloudId(1L);
		final SystemRequestDTO system = getTestSystemRequestDTO();
		system.setPort(12345);
		form.setRequesterSystem(system);
		form.setUseGateway(false);
		
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

}