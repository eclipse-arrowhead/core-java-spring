package eu.arrowhead.core.gatekeeper.database.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.repository.CloudGatekeeperRelayRepository;
import eu.arrowhead.common.database.repository.CloudGatewayRelayRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.RelayRepository;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class GatekeeperDBServiceCloudTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperDBService gatekeeperDBService;
	
	@Mock
	private CloudRepository cloudRepository;
	
	@Mock
	private RelayRepository relayRepository;
	
	@Mock
	private CloudGatekeeperRelayRepository cloudGatekeeperRelayRepository;
	
	@Mock
	private CloudGatewayRelayRepository cloudGatewayRelayRepository;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	// Tests of getClouds
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetCloudsWithInvalidSortFieldParameter() {
		gatekeeperDBService.getClouds(1, 1, Direction.ASC, "invalid");
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of getCloudById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void tesGetCloudByIdWithInvalidId() {
		gatekeeperDBService.getCloudById(-1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void tesGetCloudByIdWithNotExistingRelay() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		gatekeeperDBService.getCloudById(5);
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of registerBulkCloudsWithRelays
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithNullDTOList() {
		gatekeeperDBService.registerBulkCloudsWithRelays(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithEmptyDTOList() {
		gatekeeperDBService.registerBulkCloudsWithRelays(new ArrayList<>());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithDTOListContainingNullElement() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(null);
		dtoList.add(new CloudRequestDTO());
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithNullOperator() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(null);
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithBlankOperator() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithOperatorContainsDot() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator.");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithNullName() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName(null);
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithBlankName() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithNameContainsDot() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name.");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
				
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithSecureCloudWithNullAuthInfo() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo(null);
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
					
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithSecureCloudWithBlankAuthInfo() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("");
		cloudRequestDTO.setGatewayRelayIds(new ArrayList<>());
		dtoList.add(cloudRequestDTO);
					
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithUniqueConstraintViolationInCloudTable() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithUniqueConstraintViolationInDTOList() {
		final CloudRequestDTO cloudRequestDTO1 = new CloudRequestDTO();
		cloudRequestDTO1.setOperator("operator");
		cloudRequestDTO1.setName("name");
		cloudRequestDTO1.setSecure(true);
		cloudRequestDTO1.setNeighbor(true);
		cloudRequestDTO1.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO1.setGatekeeperRelayIds(List.of(1L));
		
		final CloudRequestDTO cloudRequestDTO2 = new CloudRequestDTO();
		cloudRequestDTO2.setOperator("operator");
		cloudRequestDTO2.setName("name");
		cloudRequestDTO2.setSecure(true);
		cloudRequestDTO2.setNeighbor(true);
		cloudRequestDTO2.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO2.setGatekeeperRelayIds(List.of(1L));
		
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO1);
		dtoList.add(cloudRequestDTO2);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(anyLong())).thenReturn(true);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithNullGatekeeperRelayList() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(null);
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithInvalidGatekeeperRelayid() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(-1L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithGatekeeperRelayNotExists() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(anyLong())).thenReturn(false);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithInvalidGatewayRelayId() {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(-2L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(1L)).thenReturn(true);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithGatewayRelayNotExists() {
		final Relay relayGatekeeper = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEKEEPER_RELAY);
		relayGatekeeper.setId(1);
		
		final Relay relayGateway = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEKEEPER_RELAY);
		relayGateway.setId(2);
		
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(2L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(1L)).thenReturn(true);
		when(relayRepository.existsById(2L)).thenReturn(false);
						
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithExclusiveRelayInGatekeeperIdList() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, true, RelayType.GATEKEEPER_RELAY); //It's not possible to register such Relay in real operation
		relay.setId(1);
		
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(anyLong())).thenReturn(true);
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
					
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithGatewayRelayInGatekeeperIdList() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY);
		relay.setId(1);
		
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(anyLong())).thenReturn(true);
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
					
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testregisterBulkCloudsWithRelaysWithGatekeeperRelayInGatewayIdList() {
		final Relay relayGatekeeper = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEKEEPER_RELAY);
		relayGatekeeper.setId(1);
		
		final Relay relayGateway = new Relay("2.2.2.2", 20000, true, false, RelayType.GATEKEEPER_RELAY);
		relayGateway.setId(2);
		
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(2L));
		dtoList.add(cloudRequestDTO);
		
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(false);
		when(relayRepository.existsById(anyLong())).thenReturn(true);
		when(relayRepository.findAllById(List.of(1L))).thenReturn(List.of(relayGatekeeper));
		when(relayRepository.findAllById(List.of(2L))).thenReturn(List.of(relayGateway));
					
		gatekeeperDBService.registerBulkCloudsWithRelays(dtoList);
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of updateCloudByIdWithRelays
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithInvalidId() {
		gatekeeperDBService.updateCloudByIdWithRelays(-1, new CloudRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNullDTO() {
		gatekeeperDBService.updateCloudByIdWithRelays(1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNotExistingCloud() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		gatekeeperDBService.updateCloudByIdWithRelays(100, new CloudRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNullOperator() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(null);
		cloudRequestDTO.setName("updatedName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithBlankOperator() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("");
		cloudRequestDTO.setName("updatedName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithOperatorContainsDot() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("updatedOperator.");
		cloudRequestDTO.setName("updatedName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNullName() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("updatedOperator");
		cloudRequestDTO.setName(null);
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithBlankName() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("updatedOperator");
		cloudRequestDTO.setName("");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNameContainsDot() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("updatedOperator");
		cloudRequestDTO.setName("updatedName.");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithSecureCloudButWithNullAuthInfo() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("updatedOperator");
		cloudRequestDTO.setName("updatedName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo(null);
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithSecureCloudButWithBlankAuthInfo() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("updatedOperator");
		cloudRequestDTO.setName("updatedName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("");
				
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNullGatekeeperRelayIdsList() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(null);
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithEmptyGatekeeperRelayIdsList() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(new ArrayList<>());
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNotInvalidIdInGatekeeperRelayIdsList() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(-1L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNotExistingRelayInGatekeeperRelayIdsList() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(relayRepository.existsById(anyLong())).thenReturn(false);
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithInvalidRelayInGatewayRelayIdsList() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(-2L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(relayRepository.existsById(1L)).thenReturn(true);
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithNotExistingRelayInGatewayRelayIdsList() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(2L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(relayRepository.existsById(1L)).thenReturn(true);
		when(relayRepository.existsById(2L)).thenReturn(false);
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithUniqueConstraintViolationInCloudTable() {
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(relayRepository.existsById(anyLong())).thenReturn(true);
		when(cloudRepository.existsByOperatorAndName(any(), any())).thenReturn(true);
						
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithExclusiveRelay() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, true, RelayType.GATEKEEPER_RELAY); //It's not possible to register such Relay in real operation
		relay.setId(1);
		
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(relayRepository.existsById(anyLong())).thenReturn(true);
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCloudByIdWithRelaysWithGatewayRelay() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY);
		relay.setId(1);
		
		final Cloud cloud = new Cloud("originalOperator", "originalName", true, true, false, "originalAuthInfo");
		cloud.setId(1);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("operator");
		cloudRequestDTO.setName("name");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("yfbgfbngfs");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		when(relayRepository.existsById(anyLong())).thenReturn(true);
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
		
		gatekeeperDBService.updateCloudByIdWithRelays(1, cloudRequestDTO);
	}
	
	
	//-------------------------------------------------------------------------------------------------
	// Tests of assignRelaysToCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithInvalidCloudId() {
		gatekeeperDBService.assignRelaysToCloud(-1L, List.of(1L), List.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithNotExistingCloud() {
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		gatekeeperDBService.assignRelaysToCloud(10L, List.of(1L), List.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithInvalidGatekeeperRelayId() {
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "authInfo");
		cloud.setId(1);
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.assignRelaysToCloud(1L, List.of(-1L), List.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithInvalidGatewayRelayId() {
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "authInfo");
		cloud.setId(1);
		
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.assignRelaysToCloud(1L, List.of(1L), List.of(-1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithExclusiveRelayAsGatekeeper() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, true, RelayType.GATEKEEPER_RELAY); //It's not possible to register such Relay in real operation
		relay.setId(1);
		
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "authInfo");
		cloud.setId(1);
		
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.assignRelaysToCloud(1L, List.of(1L), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithGatewayRelayAsGatekeeper() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY); //
		relay.setId(1);
		
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "authInfo");
		cloud.setId(1);
		
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.assignRelaysToCloud(1L, List.of(1L), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAssignRelaysToCloudWithGatekeeperRelayAsGateway() {
		final Relay relay = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEKEEPER_RELAY); //
		relay.setId(1);
		
		final Cloud cloud = new Cloud("operator", "name", true, true, false, "authInfo");
		cloud.setId(1);
		
		when(relayRepository.findAllById(any())).thenReturn(List.of(relay));
		when(cloudRepository.findById(anyLong())).thenReturn(Optional.of(cloud));
		
		gatekeeperDBService.assignRelaysToCloud(1L, null, List.of(1L));
	}
	
	//-------------------------------------------------------------------------------------------------
	// Tests of removeCloudById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveCloudByIdWithInvalidId() {
		gatekeeperDBService.removeCloudById(-1);
	}
}
