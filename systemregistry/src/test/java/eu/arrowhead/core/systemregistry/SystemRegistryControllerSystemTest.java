/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.systemregistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.systemregistry.database.service.SystemRegistryDBService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
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

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {SystemRegistryTestContext.class})
public class SystemRegistryControllerSystemTest {


    //=================================================================================================
    // test data
    private final static long VALID_SYSTEM_ID = 1L;
    private final static long UNKNOWN_SYSTEM_ID = 2L;
    private final static long NEGATIVE_SYSTEM_ID = -1L;

    private final static SystemResponseDTO VALID_SYSTEM = new SystemResponseDTO(VALID_SYSTEM_ID, "system", "address", 80, "authenticationInfo", null, "2020-01-01T00:00:00", "2020-01-01T00:00:00");


    //=================================================================================================
    // members

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockSystemRegistryDBService")
    private SystemRegistryDBService systemRegistryDBService;
    
    @MockBean(name = "mockCommonDBService")
    private CommonDBService commonDBService;
    
    @Spy
    private NetworkAddressVerifier networkAddressVerifier;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        reset(systemRegistryDBService);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    //=================================================================================================
    // Tests of system registry

    //-------------------------------------------------------------------------------------------------
    @Test
    public void echoOnboarding() throws Exception {
        final MvcResult response = this.mockMvc.perform(get("/systemregistry/echo")
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        assertEquals("Got it!", response.getResponse().getContentAsString());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemById() throws Exception {
        when(systemRegistryDBService.getSystemById(VALID_SYSTEM_ID)).thenReturn(VALID_SYSTEM);
        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt/system/" + VALID_SYSTEM_ID)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        
        final SystemResponseDTO responseDTO = readResponse(response, SystemResponseDTO.class);
        assertEquals(VALID_SYSTEM, responseDTO);
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemByUnknownId() throws Exception {
        when(systemRegistryDBService.getSystemById(UNKNOWN_SYSTEM_ID))
                .thenThrow(new InvalidParameterException("System with id " + UNKNOWN_SYSTEM_ID + " not found."));

        this.mockMvc.perform(get("/systemregistry/mgmt/system/" + UNKNOWN_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemByNegativeId() throws Exception {
        this.mockMvc.perform(get("/systemregistry/mgmt/system/" + NEGATIVE_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemsWithoutParameter() throws Exception {
        final SystemListResponseDTO systemListResponseDTO = new SystemListResponseDTO(List.of(VALID_SYSTEM), 1);
        when(systemRegistryDBService.getSystemEntries(any(), any())).thenReturn(systemListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt/systems")
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemListResponseDTO responseBody = readResponse(response, SystemListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemsWithPageAndSizeParameter() throws Exception {
        final SystemListResponseDTO systemListResponseDTO = new SystemListResponseDTO(List.of(VALID_SYSTEM), 1);
        when(systemRegistryDBService.getSystemEntries(any(), any())).thenReturn(systemListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt/systems")
                                                                .param("page", "0")
                                                                .param("item_per_page", String.valueOf(5))
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemListResponseDTO responseBody = readResponse(response, SystemListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequest() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        final MvcResult response = this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                                              .accept(MediaType.APPLICATION_JSON)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(dto)))
                                             .andExpect(status().isCreated())
                                             .andReturn();
        assertEquals(VALID_SYSTEM, readResponse(response, SystemResponseDTO.class));
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestNullPayload() throws Exception {
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestNullSystemName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO(null, "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestEmptySystemName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("", "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestWrongSystemName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("invalid_format", "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestNullAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", null, 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestEmptyAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "", 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestInvalidAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "0.0.0.0", 80, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestNullPort() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", null, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void createSystemRequestZeroPort() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 0, "authenticationInfo", null);
        when(systemRegistryDBService.createSystemDto(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(post("/systemregistry/mgmt/systems/")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequest() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.updateSystemDto(eq(VALID_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any()))
                .thenReturn(VALID_SYSTEM);

        final MvcResult response = this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                                              .accept(MediaType.APPLICATION_JSON)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(dto)))
                                             .andExpect(status().isOk())
                                             .andReturn();
        assertEquals(VALID_SYSTEM, readResponse(response, SystemResponseDTO.class));
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemUnknownId() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.updateSystemDto(eq(UNKNOWN_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any()))
                .thenThrow(new InvalidParameterException("No system with id : " + UNKNOWN_SYSTEM_ID));

        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + UNKNOWN_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemInvalidId() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);

        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + NEGATIVE_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestNullPayload() throws Exception {
        when(systemRegistryDBService.updateSystemDto(eq(VALID_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any()))
                .thenReturn(VALID_SYSTEM);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestNullSystemName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO(null, "address", 80, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestEmptySystemName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("", "address", 80, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestWrongSystemName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("invalid.format", "address", 80, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestNullAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", null, 80, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestEmptyAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "", 80, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestInvalidAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "0.0.0.0", 80, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestNullPort() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", null, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRequestZeroPort() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 0, "authenticationInfo", null);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemWithOneField() throws Exception {
        SystemRequestDTO dto = null;
        when(systemRegistryDBService.mergeSystemResponse(eq(VALID_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        dto = new SystemRequestDTO();
        dto.setSystemName("not-empty");
        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new SystemRequestDTO();
        dto.setPort(80);
        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new SystemRequestDTO();
        dto.setAddress("not-empty");
        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new SystemRequestDTO();
        dto.setAuthenticationInfo("not-empty");
        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemWithWrongName() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO();
        dto.setSystemName("invalid_format");
        when(systemRegistryDBService.mergeSystemResponse(eq(VALID_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	   //-------------------------------------------------------------------------------------------------
		@Test
	    public void mergeSystemWithEmpty() throws Exception {
	        final SystemRequestDTO dto = new SystemRequestDTO();
	        when(systemRegistryDBService.mergeSystemResponse(eq(VALID_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(VALID_SYSTEM);

	        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
	                                     .accept(MediaType.APPLICATION_JSON)
	                                     .contentType(MediaType.APPLICATION_JSON)
	                                     .content(objectMapper.writeValueAsString(dto)))
	                    .andExpect(status().isBadRequest())
	                    .andReturn();
	    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemUnknownId() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);
        when(systemRegistryDBService.mergeSystemResponse(eq(UNKNOWN_SYSTEM_ID), anyString(), anyString(), anyInt(), anyString(), any()))
                .thenThrow(new InvalidParameterException("No system with id : " + UNKNOWN_SYSTEM_ID));

        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + UNKNOWN_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemInvalidId() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);

        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + NEGATIVE_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemInvalidAddress() throws Exception {
        final SystemRequestDTO dto = new SystemRequestDTO("system", "0.0.0.0", 80, "authenticationInfo", null);

        this.mockMvc.perform(patch("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void removeSystemInvalidId() throws Exception {
        this.mockMvc.perform(delete("/systemregistry/mgmt/systems/" + NEGATIVE_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void removeSystemUnknownId() throws Exception {
        doThrow(new InvalidParameterException("No system with id : " + UNKNOWN_SYSTEM_ID))
                .when(systemRegistryDBService)
                .removeSystemById(eq(UNKNOWN_SYSTEM_ID));

        this.mockMvc.perform(delete("/systemregistry/mgmt/systems/" + UNKNOWN_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void removeSystem() throws Exception {

        this.mockMvc.perform(delete("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }
	
    //=================================================================================================
    // Tests of system registry

    //-------------------------------------------------------------------------------------------------
	private <T> T readResponse(final MvcResult response, final Class<T> clz) throws IOException {
        return objectMapper.readValue(response.getResponse().getContentAsString(), clz);
    }
}