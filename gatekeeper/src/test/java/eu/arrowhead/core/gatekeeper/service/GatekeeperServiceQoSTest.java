package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@RunWith(SpringRunner.class)
public class GatekeeperServiceQoSTest {

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
	@Test
	public void testInitSystemAddressCollectionOk() {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator("test-operator");
		
		final Cloud cloudEntity = new Cloud();
		cloudEntity.setName(requestDTO.getName());
		cloudEntity.setOperator(requestDTO.getOperator());
		
		final SystemAddressSetRelayResponseDTO responseDTO = new SystemAddressSetRelayResponseDTO(Set.of("10.10.10.10", "20.20.20.20"));
		
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloudEntity);
		when(gatekeeperDriver.sendSystemAddressCollectionRequest(any())).thenReturn(responseDTO);
		
		final SystemAddressSetRelayResponseDTO result = gatekeeperService.initSystemAddressCollection(requestDTO);
		
		assertEquals(responseDTO.getAddresses().size(), result.getAddresses().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitSystemAddressCollectionNullCloudName() {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName(null);
		requestDTO.setOperator("test-operator");
		
		final Cloud cloudEntity = new Cloud();
		cloudEntity.setName(requestDTO.getName());
		cloudEntity.setOperator(requestDTO.getOperator());
		
		final SystemAddressSetRelayResponseDTO responseDTO = new SystemAddressSetRelayResponseDTO(Set.of("10.10.10.10", "20.20.20.20"));
		
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloudEntity);
		when(gatekeeperDriver.sendSystemAddressCollectionRequest(any())).thenReturn(responseDTO);
		
		gatekeeperService.initSystemAddressCollection(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitSystemAddressCollectionBlankCloudName() {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("");
		requestDTO.setOperator("test-operator");
		
		final Cloud cloudEntity = new Cloud();
		cloudEntity.setName(requestDTO.getName());
		cloudEntity.setOperator(requestDTO.getOperator());
		
		final SystemAddressSetRelayResponseDTO responseDTO = new SystemAddressSetRelayResponseDTO(Set.of("10.10.10.10", "20.20.20.20"));
		
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloudEntity);
		when(gatekeeperDriver.sendSystemAddressCollectionRequest(any())).thenReturn(responseDTO);
		
		gatekeeperService.initSystemAddressCollection(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitSystemAddressCollectionNullCloudOperator() {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator(null);
		
		final Cloud cloudEntity = new Cloud();
		cloudEntity.setName(requestDTO.getName());
		cloudEntity.setOperator(requestDTO.getOperator());
		
		final SystemAddressSetRelayResponseDTO responseDTO = new SystemAddressSetRelayResponseDTO(Set.of("10.10.10.10", "20.20.20.20"));
		
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloudEntity);
		when(gatekeeperDriver.sendSystemAddressCollectionRequest(any())).thenReturn(responseDTO);
		
		gatekeeperService.initSystemAddressCollection(requestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitSystemAddressCollectionBlankCloudOperator() {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator("");
		
		final Cloud cloudEntity = new Cloud();
		cloudEntity.setName(requestDTO.getName());
		cloudEntity.setOperator(requestDTO.getOperator());
		
		final SystemAddressSetRelayResponseDTO responseDTO = new SystemAddressSetRelayResponseDTO(Set.of("10.10.10.10", "20.20.20.20"));
		
		when(gatekeeperDBService.getCloudByOperatorAndName(any(), any())).thenReturn(cloudEntity);
		when(gatekeeperDriver.sendSystemAddressCollectionRequest(any())).thenReturn(responseDTO);
		
		gatekeeperService.initSystemAddressCollection(requestDTO);
	}
}
