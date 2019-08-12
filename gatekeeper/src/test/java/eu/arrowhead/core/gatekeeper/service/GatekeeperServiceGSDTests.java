package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryFormDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
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
		when(gatekeeperDriver.sendGSDPollRequest(any(), any())).thenReturn(List.of(new GSDPollResponseDTO(DTOConverter.convertCloudToCloudResponseDTO(cloud), "test-service", List.of("HTTP-SECURE-JSON"), 2, null, 3)));
		
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
}
