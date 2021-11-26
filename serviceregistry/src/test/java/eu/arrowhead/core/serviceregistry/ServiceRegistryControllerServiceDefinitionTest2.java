/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBServiceTestContext.class })
@ActiveProfiles("nonstrict")
public class ServiceRegistryControllerServiceDefinitionTest2 {
	
	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockServiceRegistryDBService") 
	private ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void addServiceDefinitionTestWithWrongDefinitionFlagFalse() throws Exception {
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(0, "test_definition", "", "");
		
		when(serviceRegistryDBService.createServiceDefinitionResponse(anyString())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(post("/serviceregistry/mgmt/services")
											   .content("{\"serviceDefinition\": \"test_definition\"}")
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isCreated())
											   .andReturn();
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);
		
		assertEquals("test_definition", responseBody.getServiceDefinition());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void putUpdateServiceDefinitionTestWithWrongDefinitionFlagTrue() throws Exception {
		final String serviceDefinition = "test_definition";
		final int id = 5;
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(id, serviceDefinition, "", "");
		
		when(serviceRegistryDBService.updateServiceDefinitionByIdResponse(anyLong(), anyString())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(put("/serviceregistry/mgmt/services/" + id)
											   .content("{\"serviceDefinition\": \"" + serviceDefinition + "\"}")
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);

		assertEquals(serviceDefinition, responseBody.getServiceDefinition());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void patchUpdateServiceDefinitionTestWithWrongDefinitionFlagTrue() throws Exception {
		final String serviceDefinition = "test-definition-";
		final int id = 5;
		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO(id, serviceDefinition, "", "");
		
		when(serviceRegistryDBService.updateServiceDefinitionByIdResponse(anyLong(), anyString())).thenReturn(serviceDefinitionResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/services/" + id)
											   .content("{\"serviceDefinition\": \"" + serviceDefinition + "\"}")
											   .contentType(MediaType.APPLICATION_JSON)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final ServiceDefinitionResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionResponseDTO.class);

		assertEquals(serviceDefinition, responseBody.getServiceDefinition());
	}
}