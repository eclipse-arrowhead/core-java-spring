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
import eu.arrowhead.common.dto.internal.SystemRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryResponseDTO;
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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
public class SystemRegistryControllerSystemRegistryTest {


    //=================================================================================================
    // test data
    private final static String DATE_STRING = "2020-01-01T00:00:00";

    private final static long VALID_SYSTEM_ID = 1L;
    private final static long VALID_DEVICE_ID = 1L;

    private final static long VALID_SYSTEM_REGISTRY_ID = 1L;
    private final static long UNKNOWN_SYSTEM_REGISTRY_ID = 2L;
    private final static long INVALID_SYSTEM_REGISTRY_ID = -1L;

    private final static String VALID_DEVICE_NAME = "device";
    private final static String VALID_SYSTEM_NAME = "system";
    private final static String UNKNOWN_SYSTEM_NAME = "unknown";
    private final static String INVALID_SYSTEM_NAME = "";
    private final static String WRONG_SYSTEM_NAME = "wrong_system_name";

    private static final SystemRequestDTO VALID_SYSTEM_REQUEST =
            new SystemRequestDTO("system", "address", 80, "authenticationInfo", null);
    private final static SystemResponseDTO VALID_SYSTEM =
            new SystemResponseDTO(VALID_SYSTEM_ID, VALID_SYSTEM_NAME, "address", 80,
                                  "authenticationInfo", null, DATE_STRING, DATE_STRING);

    private final static DeviceRequestDTO VALID_DEVICE_REQUEST =
            new DeviceRequestDTO(VALID_DEVICE_NAME, "address", "AA:AA:AA:AA:AA:AA", "authenticationInfo");
    private final static DeviceResponseDTO VALID_DEVICE =
            new DeviceResponseDTO(VALID_DEVICE_ID, VALID_DEVICE_NAME, "address", "macAddress",
                                  "authenticationInfo", DATE_STRING, DATE_STRING);

    private final static SystemRegistryResponseDTO VALID_SYSTEM_REGISTRY =
            new SystemRegistryResponseDTO(VALID_SYSTEM_REGISTRY_ID, VALID_SYSTEM, VALID_DEVICE, DATE_STRING,
                                          null, 1, DATE_STRING, DATE_STRING);


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

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntriesWithoutParameter() throws Exception {
        final SystemRegistryListResponseDTO systemListResponseDTO = new SystemRegistryListResponseDTO(List.of(VALID_SYSTEM_REGISTRY), 1);
        when(systemRegistryDBService.getSystemRegistryEntries(any(), any())).thenReturn(systemListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt")
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemRegistryListResponseDTO responseBody = readResponse(response, SystemRegistryListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntriesWithPageAndSizeParameter() throws Exception {
        final SystemRegistryListResponseDTO systemListResponseDTO = new SystemRegistryListResponseDTO(List.of(VALID_SYSTEM_REGISTRY), 1);
        when(systemRegistryDBService.getSystemRegistryEntries(any(), any())).thenReturn(systemListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt")
                                                                .param("page", "0")
                                                                .param("item_per_page", String.valueOf(5))
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemRegistryListResponseDTO responseBody = readResponse(response, SystemRegistryListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntryById() throws Exception {
        when(systemRegistryDBService.getSystemRegistryById(VALID_SYSTEM_REGISTRY_ID)).thenReturn(VALID_SYSTEM_REGISTRY);
        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemRegistryResponseDTO responseDTO = readResponse(response, SystemRegistryResponseDTO.class);
        assertEquals(VALID_SYSTEM_REGISTRY, responseDTO);
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntryByIdUnknownId() throws Exception {
        when(systemRegistryDBService.getSystemRegistryById(UNKNOWN_SYSTEM_REGISTRY_ID)).thenThrow(
                new InvalidParameterException("System Registry with id " + UNKNOWN_SYSTEM_REGISTRY_ID + " not found."));

        this.mockMvc.perform(get("/systemregistry/mgmt/" + UNKNOWN_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntryByIdNegativeId() throws Exception {
        this.mockMvc.perform(get("/systemregistry/mgmt/" + INVALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntriesBySystemName() throws Exception {
        final SystemRegistryListResponseDTO systemListResponseDTO = new SystemRegistryListResponseDTO(List.of(VALID_SYSTEM_REGISTRY), 1);
        when(systemRegistryDBService.getSystemRegistryEntriesBySystemName(eq(VALID_SYSTEM_NAME), any(), any()))
                .thenReturn(systemListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt/systemname/" + VALID_SYSTEM_NAME)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemRegistryListResponseDTO responseBody = readResponse(response, SystemRegistryListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntriesByUnknownSystemName() throws Exception {
        final SystemRegistryListResponseDTO systemListResponseDTO = new SystemRegistryListResponseDTO(List.of(), 0);
        when(systemRegistryDBService.getSystemRegistryEntriesBySystemName(eq(UNKNOWN_SYSTEM_NAME), any(), any()))
                .thenReturn(systemListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/systemregistry/mgmt/systemname/" + UNKNOWN_SYSTEM_NAME)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final SystemRegistryListResponseDTO responseBody = readResponse(response, SystemRegistryListResponseDTO.class);
        assertEquals(0, responseBody.getCount());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntriesByInvalidSystemName() throws Exception {
        this.mockMvc.perform(get("/systemregistry/mgmt/systemname/" + INVALID_SYSTEM_NAME)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
    //-------------------------------------------------------------------------------------------------
	@Test
    public void getSystemRegistryEntriesByWrongSystemName() throws Exception {
        this.mockMvc.perform(get("/systemregistry/mgmt/systemname/" + WRONG_SYSTEM_NAME)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void removeSystemRegistryEntryByInvalidId() throws Exception {
        this.mockMvc.perform(delete("/systemregistry/mgmt/" + INVALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void removeSystemRegistryEntryByUnknownId() throws Exception {
        doThrow(new InvalidParameterException("No system registry with id : " + UNKNOWN_SYSTEM_REGISTRY_ID))
                .when(systemRegistryDBService)
                .removeSystemRegistryEntryById(eq(UNKNOWN_SYSTEM_REGISTRY_ID));

        this.mockMvc.perform(delete("/systemregistry/mgmt/" + UNKNOWN_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void removeSystemRegistryEntryById() throws Exception {

        this.mockMvc.perform(delete("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRegistryRequest() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, null, null, 1);
        when(systemRegistryDBService.updateSystemRegistryById(eq(VALID_SYSTEM_REGISTRY_ID), any())).thenReturn(VALID_SYSTEM_REGISTRY);

        final MvcResult result = this.mockMvc.perform(put("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                                              .accept(MediaType.APPLICATION_JSON)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(dto)))
                                             .andExpect(status().isOk())
                                             .andReturn();
        assertEquals(VALID_SYSTEM_REGISTRY, objectMapper.readValue(result.getResponse().getContentAsString(), SystemRegistryResponseDTO.class));
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRegistryUnknownId() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, null, null, 1);
        when(systemRegistryDBService.updateSystemRegistryById(eq(UNKNOWN_SYSTEM_REGISTRY_ID), any()))
                .thenThrow(new InvalidParameterException("No system registry with id : " + UNKNOWN_SYSTEM_REGISTRY_ID));

        this.mockMvc.perform(put("/systemregistry/mgmt/" + UNKNOWN_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRegistryInvalidId() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, null, null, 1);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + INVALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRegistryInvalidSystemAddress() throws Exception {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO("system", "0.0.0.0", 80, "authenticationInfo", null);
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(systemRequestDTO, VALID_DEVICE_REQUEST, null, null, 1);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + INVALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRegistryInvalidDeviceAddress() throws Exception {
		final DeviceRequestDTO deviceRequestDTO = new DeviceRequestDTO(VALID_DEVICE_NAME, "0.0.0.0", "AA:AA:AA:AA:AA:AA", "authenticationInfo");
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, deviceRequestDTO, null, null, 1);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + INVALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void updateSystemRegistryNullPayload() throws Exception {
        when(systemRegistryDBService.updateSystemRegistryById(eq(VALID_SYSTEM_REGISTRY_ID), any())).thenReturn(VALID_SYSTEM_REGISTRY);
        this.mockMvc.perform(put("/systemregistry/mgmt/systems/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemWithOneField() throws Exception {
        SystemRegistryRequestDTO dto = null;
        when(systemRegistryDBService.mergeSystemRegistryById(eq(VALID_SYSTEM_REGISTRY_ID), any())).thenReturn(VALID_SYSTEM_REGISTRY);

        dto = new SystemRegistryRequestDTO();
        dto.setSystem(VALID_SYSTEM_REQUEST);
        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new SystemRegistryRequestDTO();
        dto.setEndOfValidity(DATE_STRING);
        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new SystemRegistryRequestDTO();
        dto.setMetadata(Map.of());
        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new SystemRegistryRequestDTO();
        dto.setVersion(2);
        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemWithEmpty() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO();
        when(systemRegistryDBService.mergeSystemRegistryById(eq(VALID_SYSTEM_REGISTRY_ID), any())).thenReturn(VALID_SYSTEM_REGISTRY);

        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
   //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemWithWrongSystemName() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO();
        SystemRequestDTO system = new SystemRequestDTO();
        system.setSystemName(WRONG_SYSTEM_NAME);
		dto.setSystem(system);
        when(systemRegistryDBService.mergeSystemRegistryById(eq(VALID_SYSTEM_REGISTRY_ID), any())).thenReturn(VALID_SYSTEM_REGISTRY);

        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemUnknownId() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, DATE_STRING, null, 1);
        when(systemRegistryDBService.mergeSystemRegistryById(eq(UNKNOWN_SYSTEM_REGISTRY_ID), any()))
                .thenThrow(new InvalidParameterException("No system with id : " + UNKNOWN_SYSTEM_REGISTRY_ID));

        this.mockMvc.perform(patch("/systemregistry/mgmt/" + UNKNOWN_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

	//-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemInvalidId() throws Exception {
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, DATE_STRING, null, 1);

        this.mockMvc.perform(patch("/systemregistry/mgmt/" + INVALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemInvalidSystemAddress() throws Exception {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO("system", "0.0.0.0", 80, "authenticationInfo", null);
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(systemRequestDTO, VALID_DEVICE_REQUEST, DATE_STRING, null, 1);

        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void mergeSystemInvalidDeviceAddress() throws Exception {
		final DeviceRequestDTO deviceRequestDTO = new DeviceRequestDTO(VALID_DEVICE_NAME, "0.0.0.0", "AA:AA:AA:AA:AA:AA", "authenticationInfo");
        final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, deviceRequestDTO, DATE_STRING, null, 1);

        this.mockMvc.perform(patch("/systemregistry/mgmt/" + VALID_SYSTEM_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void registerSystemNullPayload() throws Exception {
        when(systemRegistryDBService.registerSystemRegistry(any())).thenReturn(VALID_SYSTEM_REGISTRY);

        this.mockMvc.perform(post("/systemregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void registerSystemNameWrong() throws Exception {
		final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(new SystemRequestDTO("invalid.format_", "address", 80, "authenticationInfo", null), VALID_DEVICE_REQUEST, DATE_STRING, null, 1);

        this.mockMvc.perform(post("/systemregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void registerSystemAddressWrong() throws Exception {
		final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(new SystemRequestDTO(VALID_SYSTEM_NAME, "0.0.0.0", 80, "authenticationInfo", null), VALID_DEVICE_REQUEST, DATE_STRING, null, 1);

        this.mockMvc.perform(post("/systemregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
    public void registerDeviceAddressWrong() throws Exception {
		final SystemRegistryRequestDTO dto = new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, new DeviceRequestDTO(VALID_DEVICE_NAME, "0.0.0.0", "AA:AA:AA:AA:AA:AA", "authenticationInfo"), DATE_STRING, null, 1);

        this.mockMvc.perform(post("/systemregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void registerSystemNullEndOfValidity() throws Exception {
        final SystemRegistryRequestDTO systemRegistryRequestDTO =
                new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, null, null, 1);

        when(systemRegistryDBService.registerSystemRegistry(any())).thenReturn(VALID_SYSTEM_REGISTRY);

        this.mockMvc.perform(post("/systemregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(systemRegistryRequestDTO)))
                    .andExpect(status().isCreated())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void registerSystemInvalidEndOfValidity() throws Exception {
        final SystemRegistryRequestDTO systemRegistryRequestDTO =
                new SystemRegistryRequestDTO(VALID_SYSTEM_REQUEST, VALID_DEVICE_REQUEST, "not parsable", null, 1);

        when(systemRegistryDBService.registerSystemRegistry(any())).thenReturn(VALID_SYSTEM_REGISTRY);

        this.mockMvc.perform(post("/systemregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(systemRegistryRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void unregisterSystemNullSystemName() throws Exception {
        this.mockMvc.perform(delete("/systemregistry/unregister")
                                     .param("system_name", "")
                                     .param("port", "80"))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
   //-------------------------------------------------------------------------------------------------
	@Test
    public void unregisterSystemWrongSystemName() throws Exception {
        this.mockMvc.perform(delete("/systemregistry/unregister")
                                     .param("system_name", "system.name")
                                     .param("port", "80"))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void unregisterSystemNullPort() throws Exception {
        this.mockMvc.perform(delete("/systemregistry/unregister")
                                     .param("system_name", "system")
                                     .param("port", ""))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
	
    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
	private <T> T readResponse(final MvcResult result, final Class<T> clz) throws IOException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clz);
    }
}