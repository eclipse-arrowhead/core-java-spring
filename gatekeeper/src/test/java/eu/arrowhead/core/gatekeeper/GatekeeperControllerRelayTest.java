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

package eu.arrowhead.core.gatekeeper;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.RelayListResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration(classes = { GatekeeperDBServiceTestContext.class })
public class GatekeeperControllerRelayTest {

	//=================================================================================================
	// members
	
	private static final String RELAYS_MGMT_URI = CommonConstants.GATEKEEPER_URI + CoreCommonConstants.MGMT_URI + "/relays";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockGatekeeperDBService") 
	private GatekeeperDBService gatekeeperDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//=================================================================================================
	// Tests of getRelays
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelaysWithoutParameterWithoutParameter() throws Exception {
		final int amountOfRelays = 5;
		final RelayListResponseDTO relayListResponseDTO = createRelayListResponseDTOForDBMocking(amountOfRelays);
		
		when(gatekeeperDBService.getRelaysResponse(anyInt(), anyInt(), any(), any())).thenReturn(relayListResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get(RELAYS_MGMT_URI)
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final RelayListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), RelayListResponseDTO.class);
		assertEquals(amountOfRelays, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelaysWithPageAndSizeParameter() throws Exception {
		final int amountOfRelays = 5;
		final RelayListResponseDTO relayListResponseDTO = createRelayListResponseDTOForDBMocking(amountOfRelays);
		
		when(gatekeeperDBService.getRelaysResponse(anyInt(), anyInt(), any(), any())).thenReturn(relayListResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get(RELAYS_MGMT_URI)
											   .param("page", "0")
				   							   .param("item_per_page", String.valueOf(amountOfRelays))
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final RelayListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), RelayListResponseDTO.class);
		assertEquals(amountOfRelays, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelaysWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI)
					.param("item_per_page", "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelaysWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI)
					.param("page", "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelaysWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI)
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of getRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByIdWithExistingId() throws Exception {
		final int requestedId = 1;
		final RelayResponseDTO relayResponseDTO = new RelayResponseDTO(requestedId, "testAddress", 10000, null, true, false, RelayType.GENERAL_RELAY, "", "");
		
		when(gatekeeperDBService.getRelayByIdResponse(anyLong())).thenReturn(relayResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get(RELAYS_MGMT_URI + "/" + requestedId)
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final RelayResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), RelayResponseDTO.class);
		assertEquals(requestedId, responseBody.getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI + "/-1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of getRelayByAddressAndPort
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByAddressAndPortWithExistingEntry() throws Exception {
		final String testAddress = "testAddress";
		final int testPort = 10000;
		final RelayResponseDTO relayResponseDTO = new RelayResponseDTO(1L, testAddress, testPort, null, true, false, RelayType.GENERAL_RELAY, "", "");
		
		when(gatekeeperDBService.getRelayByAddressAndPortResponse(any(), anyInt())).thenReturn(relayResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get(RELAYS_MGMT_URI + "/" + testAddress + "/" + testPort)
				   							   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final RelayResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), RelayResponseDTO.class);
		assertEquals(testAddress, responseBody.getAddress());
		assertEquals(testPort, responseBody.getPort());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByAddressAndPortWithEmptyAddress() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI + "/   " + "/50000")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByAddressAndPortWithPortOutOfRangeMin() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI + "/1.1.1.1" + "/" + (CommonConstants.SYSTEM_PORT_RANGE_MIN - 1))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetRelayByAddressAndPortWithPortOutOfRangeMax() throws Exception {
		this.mockMvc.perform(get(RELAYS_MGMT_URI + "/1.1.1.1" + "/" + (CommonConstants.SYSTEM_PORT_RANGE_MAX + 2))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of registerRelays
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysOk() throws Exception {
		final String testAddress = "1.1.1.1";
		final int testPort = 10000;
		final boolean testSecure = true;
		final boolean testExclusive = false;
		final String testType = "GENERAL_RELAY";
		
		final List<RelayRequestDTO> requestDTOList = List.of(new RelayRequestDTO(testAddress, testPort, null, testSecure, testExclusive, testType));
		final RelayListResponseDTO responseDTOList = new RelayListResponseDTO(List.of(new RelayResponseDTO(1, testAddress, testPort, null, testSecure, testExclusive,
																					  Utilities.convertStringToRelayType(testType), "", "")), 1);
		when(gatekeeperDBService.registerBulkRelaysResponse(any())).thenReturn(responseDTOList); 
		
		final MvcResult response = this.mockMvc.perform(post(RELAYS_MGMT_URI)
											   .content(objectMapper.writeValueAsBytes(requestDTOList))
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											  	.andExpect(status().isCreated())
											  	.andReturn();
		
		final RelayListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), RelayListResponseDTO.class);
		assertEquals(testAddress, responseBody.getData().get(0).getAddress());
		assertEquals(testPort, responseBody.getData().get(0).getPort());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithEmptyDTOList() throws Exception {
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content("[]")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithNullDTOInDTOList() throws Exception {
		final List<RelayRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(null);
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithNullAddress() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO(null, 10000, null, true, false, "GENERAL_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithEmptyAddress() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("   ", 10000, null, true, false, "GENERAL_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithNullPort() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", null, null, true, false, "GENERAL_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithPortOutOfRangeMin() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, null, true, false, "GENERAL_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithPortOutOfRangeMax() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, null, true, false, "GENERAL_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithInvalidRelayType() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, false, "invalid"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithExclusiveGatekeeperRelayType() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, true, "GATEKEEPER_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterRelaysWithExclusiveGeneralRelayType() throws Exception {
		final List<RelayRequestDTO> dtoList = List.of(new RelayRequestDTO("1.1.1.1", 10000, null, true, true, "GENERAL_RELAY"));
		
		this.mockMvc.perform(post(RELAYS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of updateRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdOk() throws Exception {
		final long testId = 1;
		final String testAddress = "1.1.1.1";
		final int testPort = 10000;
		final boolean testSecure = true;
		final boolean testExclusive = false;
		final String testType = "GENERAL_RELAY";
		
		final RelayRequestDTO requestDTO = new RelayRequestDTO(testAddress, testPort, null, testSecure, testExclusive, testType);
		final RelayResponseDTO responseDTO = new RelayResponseDTO(testId, testAddress, testPort, null, testSecure, testExclusive, Utilities.convertStringToRelayType(testType), "", "");
		when(gatekeeperDBService.updateRelayByIdResponse(anyLong(), anyString(), anyInt(), isNull(), anyBoolean(), anyBoolean(), any())).thenReturn(responseDTO); 
		
		final MvcResult response = this.mockMvc.perform(put(RELAYS_MGMT_URI + "/" + testId)
											   .content(objectMapper.writeValueAsBytes(requestDTO))
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final RelayResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), RelayResponseDTO.class);
		assertEquals(testAddress, responseBody.getAddress());
		assertEquals(testPort, responseBody.getPort());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithInvalidId() throws Exception {
		final RelayRequestDTO relayRequestDTO = new RelayRequestDTO("1.1.1.1", 10000, null, true, false, "GENERAL_RELAY");
		
		this.mockMvc.perform(put(RELAYS_MGMT_URI + "/-1")
					.content(objectMapper.writeValueAsBytes(relayRequestDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithNullAddress() throws Exception {
		final RelayRequestDTO relayRequestDTO = new RelayRequestDTO(null, 10000, null, true, false, "GENERAL_RELAY");
		
		this.mockMvc.perform(put(RELAYS_MGMT_URI + "/1")
					.content(objectMapper.writeValueAsBytes(relayRequestDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithEmptyAddress() throws Exception {
	final RelayRequestDTO relayRequestDTO = new RelayRequestDTO("   ", 10000, null, true, false, "GENERAL_RELAY");
	
	this.mockMvc.perform(put(RELAYS_MGMT_URI + "/1")
				.content(objectMapper.writeValueAsBytes(relayRequestDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithNullPort() throws Exception {
	final RelayRequestDTO relayRequestDTO = new RelayRequestDTO("1.1.1.1", null, null, true, false, "GENERAL_RELAY");
	
	this.mockMvc.perform(put(RELAYS_MGMT_URI + "/1")
				.content(objectMapper.writeValueAsBytes(relayRequestDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithPortOutOfRangeMin() throws Exception {
	final RelayRequestDTO relayRequestDTO = new RelayRequestDTO("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MIN - 1, null, true, false, "GENERAL_RELAY");
	
	this.mockMvc.perform(put(RELAYS_MGMT_URI + "/1")
				.content(objectMapper.writeValueAsBytes(relayRequestDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateRelayByIdWithPortOutOfRangeMax() throws Exception {
	final RelayRequestDTO relayRequestDTO = new RelayRequestDTO("1.1.1.1", CommonConstants.SYSTEM_PORT_RANGE_MAX + 1, null, true, false, "GENERAL_RELAY");
	
	this.mockMvc.perform(put(RELAYS_MGMT_URI + "/1")
				.content(objectMapper.writeValueAsBytes(relayRequestDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// Tests of updateRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveRelayByIdWithValidId() throws Exception {
		this.mockMvc.perform(delete(RELAYS_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveRelayByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(delete(RELAYS_MGMT_URI + "/-1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private RelayListResponseDTO createRelayListResponseDTOForDBMocking(final int amountOfRelay) {
		final List<RelayResponseDTO> relayDTOs = new ArrayList<>(amountOfRelay);
		
		for (int i = 1; i <= amountOfRelay; ++i) {
			final Relay relay = new Relay();
			relay.setId(i);
			relay.setAddress("testAddress" + i);
			relay.setPort(i * 1000);
			relay.setSecure(true);
			relay.setExclusive(false);
			relay.setType(RelayType.GENERAL_RELAY);
			relay.setCreatedAt(ZonedDateTime.now());
			relay.setUpdatedAt(ZonedDateTime.now());
			
			relayDTOs.add(DTOConverter.convertRelayToRelayResponseDTO(relay));
		}
		
		return new RelayListResponseDTO(relayDTOs, relayDTOs.size());
	}
}