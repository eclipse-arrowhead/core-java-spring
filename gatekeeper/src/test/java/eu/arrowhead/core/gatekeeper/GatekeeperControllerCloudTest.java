package eu.arrowhead.core.gatekeeper;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.CloudRelaysAssignmentRequestDTO;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.CloudWithRelaysListResponseDTO;
import eu.arrowhead.common.dto.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration (classes = { GatekeeperDBServiceTestContext.class })
public class GatekeeperControllerCloudTest {

	//=================================================================================================
	// members
	
	private static final String CLOUDS_MGMT_URI = CommonConstants.GATEKEEPER_URI + CommonConstants.MGMT_URI + "/clouds";
	
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
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getClouds
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudsWithoutParameterWithoutParameter( ) throws Exception {
		final int amountOfClouds = 5;
		final CloudWithRelaysListResponseDTO dto = createCloudWithRelaysListResponseDTOForDBMocking(amountOfClouds, RelayType.GATEKEEPER_RELAY, false, RelayType.GATEWAY_RELAY, false);
		
		when(gatekeeperDBService.getCloudsResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(CLOUDS_MGMT_URI)
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final CloudWithRelaysListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), CloudWithRelaysListResponseDTO.class);
		assertEquals(amountOfClouds, responseBody.getCount());
		assertEquals(RelayType.GATEKEEPER_RELAY, responseBody.getData().get(0).getGatekeeperRelays().get(0).getType());
		assertEquals(RelayType.GATEWAY_RELAY, responseBody.getData().get(0).getGatewayRelays().get(0).getType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudsWithPageAndSizeParameter( ) throws Exception {
		final int amountOfClouds = 5;
		final CloudWithRelaysListResponseDTO dto = createCloudWithRelaysListResponseDTOForDBMocking(amountOfClouds, RelayType.GATEKEEPER_RELAY, false, RelayType.GATEWAY_RELAY, false);
		
		when(gatekeeperDBService.getCloudsResponse(anyInt(), anyInt(), any(), any())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(CLOUDS_MGMT_URI)
											   .param("page", "0")
											   .param("item_per_page", String.valueOf(amountOfClouds))
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final CloudWithRelaysListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), CloudWithRelaysListResponseDTO.class);
		assertEquals(amountOfClouds, responseBody.getCount());
		assertEquals(RelayType.GATEKEEPER_RELAY, responseBody.getData().get(0).getGatekeeperRelays().get(0).getType());
		assertEquals(RelayType.GATEWAY_RELAY, responseBody.getData().get(0).getGatewayRelays().get(0).getType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudsWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(CLOUDS_MGMT_URI)
					.param("item_per_page", "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudsWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(CLOUDS_MGMT_URI)
					.param("page", "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudsWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(CLOUDS_MGMT_URI)
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getCloudById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudByIdWithExistingId() throws Exception {
		final CloudWithRelaysResponseDTO dto = createCloudWithRelaysListResponseDTOForDBMocking(1, RelayType.GATEKEEPER_RELAY, false, RelayType.GATEWAY_RELAY, false).getData().get(0);
		
		when(gatekeeperDBService.getCloudByIdResponse(anyLong())).thenReturn(dto);
		
		final MvcResult response = this.mockMvc.perform(get(CLOUDS_MGMT_URI + "/1")
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final CloudWithRelaysResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), CloudWithRelaysResponseDTO.class);
		assertEquals(1L, responseBody.getId());
		assertEquals(RelayType.GATEKEEPER_RELAY, responseBody.getGatekeeperRelays().get(0).getType());
		assertEquals(RelayType.GATEWAY_RELAY, responseBody.getGatewayRelays().get(0).getType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCloudByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(get(CLOUDS_MGMT_URI + "/-1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of registerClouds
	
	//-------------------------------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsOk() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO);
		
		final CloudWithRelaysListResponseDTO responseDTO = createCloudWithRelaysListResponseDTOForDBMocking(1, RelayType.GATEKEEPER_RELAY, false, RelayType.GATEWAY_RELAY, false);
		
		when(gatekeeperDBService.registerBulkCloudsWithRelaysResponse(any())).thenReturn(responseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(CLOUDS_MGMT_URI)
											   .content(objectMapper.writeValueAsBytes(dtoList))
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
										       .andExpect(status().isCreated())
										       .andReturn();
		
		final CloudWithRelaysListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), CloudWithRelaysListResponseDTO.class);
		assertEquals(1L, responseBody.getCount());
		assertEquals(RelayType.GATEKEEPER_RELAY, responseBody.getData().get(0).getGatekeeperRelays().get(0).getType());
		assertEquals(RelayType.GATEWAY_RELAY, responseBody.getData().get(0).getGatewayRelays().get(0).getType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsWithEmptyDTOList() throws Exception {
		this.mockMvc.perform(post(CLOUDS_MGMT_URI)
					.content("[]")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsWithNullElementInDTOList() throws Exception {
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(null);
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsWithNullOperator() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(null);
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO);
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsWithEmptyOperator() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("     ");
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO);
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsWithNullName() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName(null);
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO);
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterCloudsWithEmptyName() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName("   ");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO);
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI)
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of updateRelayById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateCloudsOk() throws Exception {		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		
		final CloudWithRelaysResponseDTO cloudResponse = createCloudWithRelaysListResponseDTOForDBMocking(1, RelayType.GATEKEEPER_RELAY, false, RelayType.GATEWAY_RELAY, false).getData().get(0);
		
		when(gatekeeperDBService.updateCloudByIdWithRelaysResponse(anyLong(), any())).thenReturn(cloudResponse);
		
		final MvcResult response = this.mockMvc.perform(put(CLOUDS_MGMT_URI + "/1")
											   .content(objectMapper.writeValueAsBytes(cloudRequestDTO))
											   .contentType(MediaType.APPLICATION_JSON)
				   							   .accept(MediaType.APPLICATION_JSON))
				   							   .andExpect(status().isOk())
				   							   .andReturn();
		
		final CloudWithRelaysResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), CloudWithRelaysResponseDTO.class);
		assertEquals(1L, responseBody.getId());
		assertEquals(RelayType.GATEKEEPER_RELAY, responseBody.getGatekeeperRelays().get(0).getType());
		assertEquals(RelayType.GATEWAY_RELAY, responseBody.getGatewayRelays().get(0).getType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateCloudsWithInvalidId() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		
		this.mockMvc.perform(put(CLOUDS_MGMT_URI + "/-1")
					.content(objectMapper.writeValueAsBytes(cloudRequestDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateCloudsWithNullOperator() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(null);
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		
		this.mockMvc.perform(put(CLOUDS_MGMT_URI + "/1")
					.content(objectMapper.writeValueAsBytes(cloudRequestDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateCloudsWithEmptyOperator() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("     ");
		cloudRequestDTO.setName("testName");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));		
		
		this.mockMvc.perform(put(CLOUDS_MGMT_URI + "/1")
					.content(objectMapper.writeValueAsBytes(cloudRequestDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateCloudsWithNullName() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName(null);
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
		final List<CloudRequestDTO> dtoList = new ArrayList<>();
		dtoList.add(cloudRequestDTO);
		
		this.mockMvc.perform(put(CLOUDS_MGMT_URI + "/1")
					.content(objectMapper.writeValueAsBytes(dtoList))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateCloudsWithEmptyName() throws Exception {
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("testOperator");
		cloudRequestDTO.setName("   ");
		cloudRequestDTO.setSecure(true);
		cloudRequestDTO.setNeighbor(true);
		cloudRequestDTO.setAuthenticationInfo("testAuthenticationInfo");
		cloudRequestDTO.setGatekeeperRelayIds(List.of(1L));
		cloudRequestDTO.setGatewayRelayIds(List.of(1L));
				
		this.mockMvc.perform(put(CLOUDS_MGMT_URI + "/1")
					.content(objectMapper.writeValueAsBytes(cloudRequestDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of assignRelaysToCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAssignRelaysToCloudOK() throws Exception {
		final CloudRelaysAssignmentRequestDTO dto = new CloudRelaysAssignmentRequestDTO();
		dto.setCloudId(1L);
		dto.setGatekeeperRelayIds(List.of(1L));
		dto.setGatewayRelayIds(List.of(1L));
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI + "/assign")
				.content(objectMapper.writeValueAsBytes(dto))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAssignRelaysToCloudWithInvalidCloudId() throws Exception {
		final CloudRelaysAssignmentRequestDTO dto = new CloudRelaysAssignmentRequestDTO();
		dto.setCloudId(-1L);
		dto.setGatekeeperRelayIds(List.of(1L));
		dto.setGatewayRelayIds(List.of(1L));
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI + "/assign")
				.content(objectMapper.writeValueAsBytes(dto))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAssignRelaysToCloudWithInvalidGatekeeperRelayId() throws Exception {
		final CloudRelaysAssignmentRequestDTO dto = new CloudRelaysAssignmentRequestDTO();
		dto.setCloudId(1L);
		dto.setGatekeeperRelayIds(List.of(-1L));
		dto.setGatewayRelayIds(List.of(1L));
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI + "/assign")
				.content(objectMapper.writeValueAsBytes(dto))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAssignRelaysToCloudWithInvalidGatewayRelayId() throws Exception {
		final CloudRelaysAssignmentRequestDTO dto = new CloudRelaysAssignmentRequestDTO();
		dto.setCloudId(1L);
		dto.setGatekeeperRelayIds(List.of(1L));
		dto.setGatewayRelayIds(List.of(-1L));
		
		this.mockMvc.perform(post(CLOUDS_MGMT_URI + "/assign")
				.content(objectMapper.writeValueAsBytes(dto))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of removeCloudById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveCloudByIdWithValidId() throws Exception {
		this.mockMvc.perform(delete(CLOUDS_MGMT_URI + "/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveRCloudByIdWithInvalidId() throws Exception {
		this.mockMvc.perform(delete(CLOUDS_MGMT_URI + "/-1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private CloudWithRelaysListResponseDTO createCloudWithRelaysListResponseDTOForDBMocking(final int amountOfClouds, final RelayType gatekeeperRelayType, final boolean gatekeeperRelayExclusive
																											  , final RelayType gatewayRelayType, final boolean gatewayRelayExclusive) {
		final List<CloudWithRelaysResponseDTO> cloudDTOList = new ArrayList<>();
		
		for (int i = 1; i <= amountOfClouds; ++i) {
			final Cloud cloud = new Cloud();
			cloud.setId(i);
			cloud.setOperator("testOperator" + i);
			cloud.setName("testName" + i);
			cloud.setSecure(true);
			cloud.setNeighbor(true);
			cloud.setOwnCloud(false);
			cloud.setAuthenticationInfo("testAuthenticationInfo" + i);
			cloud.setCreatedAt(ZonedDateTime.now());
			cloud.setUpdatedAt(ZonedDateTime.now());
			
			final Relay gatekeeperRelay = new Relay();
			gatekeeperRelay.setId(i);
			gatekeeperRelay.setAddress("testAddress" + i);
			gatekeeperRelay.setPort(i * 1000);
			gatekeeperRelay.setSecure(true);
			gatekeeperRelay.setExclusive(gatekeeperRelayExclusive);
			gatekeeperRelay.setType(gatekeeperRelayType);
			gatekeeperRelay.setCreatedAt(ZonedDateTime.now());
			gatekeeperRelay.setUpdatedAt(ZonedDateTime.now());
			
			final Relay gatewayRelay = new Relay();
			gatewayRelay.setId(i);
			gatewayRelay.setAddress("testAddress" + i);
			gatewayRelay.setPort(i * 1000);
			gatewayRelay.setSecure(true);
			gatewayRelay.setExclusive(gatewayRelayExclusive);
			gatewayRelay.setType(gatewayRelayType);
			gatewayRelay.setCreatedAt(ZonedDateTime.now());
			gatewayRelay.setUpdatedAt(ZonedDateTime.now());
			
			cloud.getGatekeeperRelays().add(new CloudGatekeeperRelay(cloud, gatekeeperRelay));
			cloud.getGatewayRelays().add(new CloudGatewayRelay(cloud, gatewayRelay));
			
			cloudDTOList.add(DTOConverter.convertCloudToCloudWithRelaysResponseDTO(cloud));
		}
		
		return new CloudWithRelaysListResponseDTO(cloudDTOList, cloudDTOList.size());
	}
}
