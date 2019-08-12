package eu.arrowhead.core.gatekeeper;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryFormDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.core.gatekeeper.service.GatekeeperService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration (classes = { GatekeeperServiceTestContext.class })
public class GatekeeperControllerGSDTests {

	//=================================================================================================
	// members
	
	private static final String INIT_GLOBAL_SERVICE_DISCOVERY_URI = CommonConstants.GATEKEEPER_URI + "/init_gsd";
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockGatekeeperService") 
	private GatekeeperService gatekeeperService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of initiateGlobalServiceDiscovery
	
	@Test
	public void testInitiateGlobalServiceDiscoveryOk() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredCloudIds(List.of(1L, 2L, 3L, 4L));
		
		when(gatekeeperService.initGSDPoll(any())).thenReturn(new GSDQueryResultDTO(List.of(new GSDPollResponseDTO()), 3));
		
		final MvcResult response = this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
				.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final GSDQueryResultDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), GSDQueryResultDTO.class);
		assertEquals(3, responseBody.getUnsuccessfulRequests());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullRequestedService() throws Exception {
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(null);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
				.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullServiceDefinition() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement(null);
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
				.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithBlankServiceDefinition() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("  ");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
				.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullIdInPreferreCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		final List<Long> preferredClouds = new ArrayList<>();
		preferredClouds.add(1L);
		preferredClouds.add(null);
		preferredClouds.add(3L);
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredCloudIds(preferredClouds);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
				.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithInvalidIdInPreferreCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredCloudIds(List.of(1L, 2L, -3L, 4L));
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
				.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
}
