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
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.core.gatekeeper.service.GatekeeperService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperServiceTestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatekeeperMain.class)
@ContextConfiguration(classes = { GatekeeperServiceTestContext.class })
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
	
	//=================================================================================================
	// Tests of initiateGlobalServiceDiscovery
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryOk() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("test-name");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		when(gatekeeperService.initGSDPoll(any())).thenReturn(new GSDQueryResultDTO(List.of(new GSDPollResponseDTO()), 0));
		
		final MvcResult response = this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
											   .content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		
		final GSDQueryResultDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), GSDQueryResultDTO.class);
		assertEquals(0, responseBody.getUnsuccessfulRequests());
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
	public void testInitiateGlobalServiceDiscoveryWithNullCloudInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		preferredClouds.add(null);
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		gsdQueryFormDTO.setPreferredClouds(preferredClouds);
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullOperatorInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator(null);
		cloudRequestDTO.setName("test-name");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithBlankOperatorInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("   ");
		cloudRequestDTO.setName("test-name");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithNullNameInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName(null);
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateGlobalServiceDiscoveryWithBlankNameInPreferredCloudList() throws Exception {
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
		serviceQueryFormDTO.setServiceDefinitionRequirement("test-service");
		
		final GSDQueryFormDTO gsdQueryFormDTO = new GSDQueryFormDTO();
		gsdQueryFormDTO.setRequestedService(serviceQueryFormDTO);
		
		final CloudRequestDTO cloudRequestDTO = new CloudRequestDTO();
		cloudRequestDTO.setOperator("test-operator");
		cloudRequestDTO.setName("   ");
		gsdQueryFormDTO.setPreferredClouds(List.of(cloudRequestDTO));
		
		this.mockMvc.perform(post(INIT_GLOBAL_SERVICE_DISCOVERY_URI)
					.content(objectMapper.writeValueAsBytes(gsdQueryFormDTO))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
}