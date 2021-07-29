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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

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
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
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
	public void testInitAccessTypesCollectionOk() throws Exception {
		final CloudRequestDTO requestDTO1 = new CloudRequestDTO();
		requestDTO1.setName("test-name1");
		requestDTO1.setOperator("test-operator1");
		requestDTO1.setNeighbor(true);
		final CloudRequestDTO requestDTO2 = new CloudRequestDTO();
		requestDTO2.setName("test-name2");
		requestDTO2.setOperator("test-operator2");
		requestDTO2.setNeighbor(true);
		
		final Cloud cloudEntity1 = new Cloud();
		cloudEntity1.setName(requestDTO1.getName());
		cloudEntity1.setOperator(requestDTO1.getOperator());
		cloudEntity1.setNeighbor(requestDTO1.getNeighbor());
		cloudEntity1.setOwnCloud(false);
		final Cloud cloudEntity2 = new Cloud();
		cloudEntity2.setName(requestDTO2.getName());
		cloudEntity2.setOperator(requestDTO2.getOperator());
		cloudEntity2.setNeighbor(requestDTO2.getNeighbor());
		cloudEntity2.setOwnCloud(false);
		
		final CloudAccessResponseDTO responseDTO1 = new CloudAccessResponseDTO(requestDTO1.getName(), requestDTO1.getOperator(), true);
		final ErrorMessageDTO responseDTO2 = new ErrorMessageDTO();
		
		when(gatekeeperDBService.getCloudByOperatorAndName(eq(requestDTO1.getOperator()), eq(requestDTO1.getName()))).thenReturn(cloudEntity1);
		when(gatekeeperDBService.getCloudByOperatorAndName(eq(requestDTO2.getOperator()), eq(requestDTO2.getName()))).thenReturn(cloudEntity2);
		when(gatekeeperDriver.sendAccessTypesCollectionRequest(any())).thenReturn(List.of(responseDTO1, responseDTO2));
		
		final CloudAccessListResponseDTO result = gatekeeperService.initAccessTypesCollection(List.of(requestDTO1, requestDTO2));
		assertEquals(1, result.getCount());
		assertEquals(1, result.getData().size());
		assertEquals(requestDTO1.getName(), result.getData().get(0).getCloudName());
		assertEquals(requestDTO1.getOperator(), result.getData().get(0).getCloudOperator());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitAccessTypesCollectionNullCloudName() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName(null);
		requestDTO.setOperator("test-operator");
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitAccessTypesCollectionBlankCloudName() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("  ");
		requestDTO.setOperator("test-operator");
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitAccessTypesCollectionNullCloudOperator() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator(null);
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitAccessTypesCollectionBlankCloudOperator() throws Exception {
		final CloudRequestDTO requestDTO = new CloudRequestDTO();
		requestDTO.setName("test-name");
		requestDTO.setOperator("  ");
				
		gatekeeperService.initAccessTypesCollection(List.of(requestDTO));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestNullRequest() throws Exception {
		
		gatekeeperService.initRelayTest(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudNull() throws Exception {
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudOperatorNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudOperatorEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator(" ");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudNameNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudNameEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName(" ");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayAddressNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayAddressEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(" ");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayPortNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayPortTooLow() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(-2);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayPortTooHigh() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(200000);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeNull() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeEmpty() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeInvalid() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("invalid");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeGatekeeper() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEKEEPER_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		gatekeeperService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestOk() throws Exception {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		when(gatekeeperDriver.queryQoSMonitorPublicKey()).thenReturn("valid public key");
		when(commonDBService.getOwnCloud(true)).thenReturn(new Cloud("aitia", "testcloud", true, false, true, "abcd"));
		when(gatekeeperDBService.getCloudByOperatorAndName(anyString(), anyString())).thenReturn(new Cloud());
		when(gatekeeperDriver.sendQoSRelayTestProposal(any(QoSRelayTestProposalRequestDTO.class), any(Cloud.class))).thenReturn(
																												new QoSRelayTestProposalResponseDTO("1234", "peer", "public key"));
		doNothing().when(gatekeeperDriver).initRelayTest(any(QoSMonitorSenderConnectionRequestDTO.class));
		
		gatekeeperService.initRelayTest(request);
		
		verify(gatekeeperDriver, times(1)).queryQoSMonitorPublicKey();
		verify(commonDBService, times(1)).getOwnCloud(true);
		verify(gatekeeperDBService, times(1)).getCloudByOperatorAndName(anyString(), anyString());
		verify(gatekeeperDriver, times(1)).sendQoSRelayTestProposal(any(QoSRelayTestProposalRequestDTO.class), any(Cloud.class));
		verify(gatekeeperDriver, times(1)).initRelayTest(any(QoSMonitorSenderConnectionRequestDTO.class));
	}
	
	// skip cloud and relay validations tests because it is the same method that was used in the initRelayTask()
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testJoinRelayTestSenderQoSMonitorPublicKeyNull() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud2");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);

		gatekeeperService.joinRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testJoinRelayTestSenderQoSMonitorPublicKeyEmpty() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud2");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey(" ");

		gatekeeperService.joinRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testJoinRelayTestOk() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud2");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid key");
		
		when(gatekeeperDriver.joinRelayTest(any(QoSRelayTestProposalRequestDTO.class))).thenReturn(new QoSRelayTestProposalResponseDTO());

		gatekeeperService.joinRelayTest(request);
		
		verify(gatekeeperDriver, times(1)).joinRelayTest(any(QoSRelayTestProposalRequestDTO.class));
	}
}