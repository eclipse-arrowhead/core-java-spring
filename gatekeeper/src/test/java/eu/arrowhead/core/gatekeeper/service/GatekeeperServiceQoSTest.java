package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
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
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void initAccessTypesCollectionOk() throws Exception {
		final CloudRequestDTO requestDTO1 = new CloudRequestDTO();
		requestDTO1.setName("test-name1");
		requestDTO1.setOperator("test-operator1");
		final CloudRequestDTO requestDTO2 = new CloudRequestDTO();
		requestDTO2.setName("test-name2");
		requestDTO2.setOperator("test-operator2");
		
		final Cloud cloudEntity1 = new Cloud();
		cloudEntity1.setName(requestDTO1.getName());
		cloudEntity1.setOperator(requestDTO1.getOperator());
		final Cloud cloudEntity2 = new Cloud();
		cloudEntity2.setName(requestDTO2.getName());
		cloudEntity2.setOperator(requestDTO2.getOperator());
		
		final CloudAccessResponseDTO responseDTO1 = new CloudAccessResponseDTO(requestDTO1.getName(), requestDTO1.getOperator(), true);
		final ErrorMessageDTO responseDTO2 = new ErrorMessageDTO();
		
		when(gatekeeperDBService.getCloudByOperatorAndName(requestDTO1.getName(), requestDTO1.getOperator())).thenReturn(cloudEntity1);
		when(gatekeeperDBService.getCloudByOperatorAndName(requestDTO2.getName(), requestDTO2.getOperator())).thenReturn(cloudEntity2);
		when(gatekeeperDriver.sendAccessTypesCollectionRequest(any())).thenReturn(List.of(responseDTO1, responseDTO2));
		
		final CloudAccessListResponseDTO result = gatekeeperService.initAccessTypesCollection(List.of(requestDTO1, requestDTO2));
		assertEquals(1, result.getCount());
		assertEquals(1, result.getData().size());
		assertEquals(requestDTO1.getName(), result.getData().get(0).getCloudName());
		assertEquals(requestDTO1.getOperator(), result.getData().get(0).getCloudOperator());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void initAccessTypesCollectionNullCloudName() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName(null);
		requestDTO.setOperator("test-operator");
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void initAccessTypesCollectionBlankCloudName() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("  ");
		requestDTO.setOperator("test-operator");
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void initAccessTypesCollectionNullCloudOperator() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator(null);
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void initAccessTypesCollectionBlankCloudOperator() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator("  ");
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}
}
