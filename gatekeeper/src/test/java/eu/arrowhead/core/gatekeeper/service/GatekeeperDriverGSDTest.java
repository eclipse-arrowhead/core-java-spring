package eu.arrowhead.core.gatekeeper.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;

@RunWith(SpringRunner.class)
public class GatekeeperDriverGSDTest {
	
	//=================================================================================================
	// members
		
	@InjectMocks
	private GatekeeperDriver testingObject;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullCloudList() {
		testingObject.sendGSDPollRequest(null, getGSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithEmptyCloudList() {
		testingObject.sendGSDPollRequest(new ArrayList<>(), getGSDPollRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullGSDPollRequestDTO() {
		testingObject.sendGSDPollRequest(List.of(new Cloud()), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequestedService() {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.setRequestedService(null);
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequestedServiceDefinition() {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.getRequestedService().setServiceDefinitionRequirement(null);
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithBlankRequestedServiceDefinition() {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.getRequestedService().setServiceDefinitionRequirement("   ");
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendGSDPollRequestWithNullRequesterCloud() {
		final GSDPollRequestDTO gsdPollRequestDTO = getGSDPollRequestDTO();
		gsdPollRequestDTO.setRequesterCloud(null);;
		
		testingObject.sendGSDPollRequest(List.of(new Cloud()), gsdPollRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendServiceReistryQueryNullQueryForm() {
		testingObject.sendServiceReistryQuery(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullQueryDataList() {
		testingObject.sendInterCloudAuthorizationCheckQuery(null, new CloudRequestDTO(), "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullCloud() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), null, "test-service");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithNullServiceDefinition() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), new CloudRequestDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendInterCloudAuthorizationCheckQueryWithBlankServiceDefinition() {
		testingObject.sendInterCloudAuthorizationCheckQuery(List.of(new ServiceRegistryResponseDTO()), new CloudRequestDTO(), "  ");
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------		
	private GSDPollRequestDTO getGSDPollRequestDTO() {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		
		return new GSDPollRequestDTO(serviceQueryFormDTO, cloudRequestDTO, false);
	}
}
