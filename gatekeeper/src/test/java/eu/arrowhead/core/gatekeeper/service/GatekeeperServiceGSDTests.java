package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryFormDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@RunWith(SpringRunner.class)
public class GatekeeperServiceGSDTests {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperService gatekeeperService;
	
	@Mock
	private GatekeeperDBService gatekeeperDBService;
	
	@Mock
	private GatekeeperDriver gatekeeperDriver;
	
	@Mock
	private CommonDBService commonDBService;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	// Tests of initGSDPoll
	
	@Test
	public void testInitGSDPollOK() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "dfsgdfsgdfh");
		cloud.setCreatedAt(ZonedDateTime.now());
		cloud.setUpdatedAt(ZonedDateTime.now());
		
		final Cloud ownCloud = new Cloud("operatorOwn", "nameOwn", true, true, true, "fadgafdh");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(List.of(cloud));
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		when(gatekeeperDriver.sendGSDPollRequest(any(), any())).thenReturn(List.of(new GSDPollResponseDTO(DTOConverter.convertCloudToCloudResponseDTO(cloud), "test-service", List.of("HTTP-SECURE-JSON"), 2, null)));
		
		final GSDQueryResultDTO result = gatekeeperService.initGSDPoll(gsdQueryFormDTO);
		assertEquals("operator", result.getResults().get(0).getProviderCloud().getOperator());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitGSDPollWithoutPreferredAndNeighborClouds() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		when(gatekeeperDBService.getNeighborClouds()).thenReturn(new ArrayList<>());
		
		gatekeeperService.initGSDPoll(gsdQueryFormDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitGSDPollWithNotExistingPreferredCloud() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredCloudIds(List.of(134L));
		
		when(gatekeeperDBService.getCloudsByIds(any())).thenReturn(new ArrayList<>());
		
		gatekeeperService.initGSDPoll(gsdQueryFormDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of doGSDPoll
	
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullGSDPollRequestDTO() {
		gatekeeperService.doGSDPoll(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithMandatoryGatewayButNotPresentInRequesterCloud() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", true);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", true);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequestedService() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(null, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequestedServiceDefinition() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement(null);
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequestedServiceDefinition() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("   ");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloud() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, null, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloudOperator() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator(null);
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequesterCloudOperator() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("   ");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloudName() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName(null);
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequesterCloudName() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("    ");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithNullRequesterCloudAuthInfo() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo(null);
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDoGSDPollWithBlankRequesterCloudAuthInfo() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("  ");
		
		gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithNoServiceRegistryQueryResults() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		when(gatekeeperDriver.sendServiceReistryQuery(any())).thenReturn(srQueryResult);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
		assertNull(doGSDPollResponse.getProviderCloud());
		assertNull(doGSDPollResponse.getNumOfProviders());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollWithNoAuthResults() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO.setId(1);
		serviceRegistryResponseDTO.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO.setProvider(new SystemResponseDTO(1L, "test-provider-operator", "1.1.1.1", 1000, "test-provider-auth-info", "", ""));
		serviceRegistryResponseDTO.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO));
		when(gatekeeperDriver.sendServiceReistryQuery(any())).thenReturn(srQueryResult);
		
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(new HashMap<>());
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
		assertNull(doGSDPollResponse.getProviderCloud());
		assertNull(doGSDPollResponse.getNumOfProviders());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoGSDPollSRAndAuthCrossCheck() {
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsPresent", false);
		ReflectionTestUtils.setField(gatekeeperService, "gatewayIsMandatory", false);
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudDTO = new CloudRequestDTO();
		cloudDTO.setOperator("test-operator");
		cloudDTO.setName("test-name");
		cloudDTO.setSecure(true);
		cloudDTO.setNeighbor(true);
		cloudDTO.setAuthenticationInfo("test-auth-info");
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO1 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO1.setId(1);
		serviceRegistryResponseDTO1.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO1.setProvider(new SystemResponseDTO(1L, "test-provider-operator-1", "1.1.1.1", 1000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO1.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", "")));
		
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO2 = new ServiceRegistryResponseDTO();
		serviceRegistryResponseDTO2.setId(2);
		serviceRegistryResponseDTO2.setServiceDefinition(new ServiceDefinitionResponseDTO(1L, "test-service", "", ""));
		serviceRegistryResponseDTO2.setProvider(new SystemResponseDTO(2L, "test-provider-operator-2", "2.2.2.2", 2000, "test-provider-auth-info-2", "", ""));
		serviceRegistryResponseDTO2.setInterfaces(List.of(new ServiceInterfaceResponseDTO(1L, "HTTP-SECURE-JSON", "", ""), new ServiceInterfaceResponseDTO(2L, "XML", "", "")));
		
		final ServiceQueryResultDTO srQueryResult = new ServiceQueryResultDTO();
		srQueryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO1, serviceRegistryResponseDTO2));
		when(gatekeeperDriver.sendServiceReistryQuery(any())).thenReturn(srQueryResult);
				
		when(gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(any(), any(), any())).thenReturn(Map.of(2L,List.of(2L)));
		
		final Cloud ownCloud = new Cloud("own-c-operator", "own-c-name", true, true, true, "own-c-auth-info");
		ownCloud.setCreatedAt(ZonedDateTime.now());
		ownCloud.setUpdatedAt(ZonedDateTime.now());
		when(commonDBService.getOwnCloud(true)).thenReturn(ownCloud);
		
		final GSDPollResponseDTO doGSDPollResponse = gatekeeperService.doGSDPoll(new GSDPollRequestDTO(serviceQueryFormDTO, cloudDTO, false));
		assertEquals(1, (int) doGSDPollResponse.getNumOfProviders());
		assertEquals(1, doGSDPollResponse.getAvailableInterfaces().size());
		assertEquals("XML", doGSDPollResponse.getAvailableInterfaces().get(0));
	}
}
