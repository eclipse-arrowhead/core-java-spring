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

package eu.arrowhead.core.deviceregistry;

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

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.DeviceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DeviceRegistryTestContext.class})
public class DeviceRegistryControllerDeviceRegistryTest {


    //=================================================================================================
    // test data
    private final static String DATE_STRING = "2020-01-01T00:00:00";

    private final static long VALID_DEVICE_ID = 1L;

    private final static long VALID_DEVICE_REGISTRY_ID = 1L;
    private final static long UNKNOWN_DEVICE_REGISTRY_ID = 2L;
    private final static long INVALID_DEVICE_REGISTRY_ID = -1L;

    private final static String VALID_DEVICE_NAME = "device";
    private final static String UNKNOWN_DEVICE_NAME = "unknown";
    private final static String INVALID_DEVICE_NAME = "";

    private static final DeviceRequestDTO VALID_DEVICE_REQUEST =
            new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");
    private final static DeviceResponseDTO VALID_DEVICE =
            new DeviceResponseDTO(VALID_DEVICE_ID, VALID_DEVICE_NAME, "address", "macAddress",
                                  "authenticationInfo", DATE_STRING, DATE_STRING);

    private final static DeviceRegistryResponseDTO VALID_DEVICE_REGISTRY =
            new DeviceRegistryResponseDTO(VALID_DEVICE_REGISTRY_ID, VALID_DEVICE, DATE_STRING,
                                          null, 1, DATE_STRING, DATE_STRING);


    //=================================================================================================
    // members

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockDeviceRegistryDBService")
    private DeviceRegistryDBService deviceRegistryDBService;
    
    @MockBean(name = "mockCommonDBService")
    private CommonDBService commonDBService;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        reset(deviceRegistryDBService);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void getDeviceRegistryEntriesWithoutParameter() throws Exception {
        final DeviceRegistryListResponseDTO deviceListResponseDTO = new DeviceRegistryListResponseDTO(List.of(VALID_DEVICE_REGISTRY), 1);
        when(deviceRegistryDBService.getDeviceRegistryEntries(any(), any())).thenReturn(deviceListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt")
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final DeviceRegistryListResponseDTO responseBody = readResponse(response, DeviceRegistryListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    @Test
    public void getDeviceRegistryEntriesWithPageAndSizeParameter() throws Exception {
        final DeviceRegistryListResponseDTO deviceListResponseDTO = new DeviceRegistryListResponseDTO(List.of(VALID_DEVICE_REGISTRY), 1);
        when(deviceRegistryDBService.getDeviceRegistryEntries(any(), any())).thenReturn(deviceListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt")
                                                                .param("page", "0")
                                                                .param("item_per_page", String.valueOf(5))
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final DeviceRegistryListResponseDTO responseBody = readResponse(response, DeviceRegistryListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    @Test
    public void getDeviceRegistryEntryById() throws Exception {
        when(deviceRegistryDBService.getDeviceRegistryById(VALID_DEVICE_REGISTRY_ID)).thenReturn(VALID_DEVICE_REGISTRY);
        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final DeviceRegistryResponseDTO responseDTO = readResponse(response, DeviceRegistryResponseDTO.class);
        assertEquals(VALID_DEVICE_REGISTRY, responseDTO);
    }

    @Test
    public void getDeviceRegistryEntryByIdUnknownId() throws Exception {
        when(deviceRegistryDBService.getDeviceRegistryById(UNKNOWN_DEVICE_REGISTRY_ID)).thenThrow(
                new InvalidParameterException("Device Registry with id " + UNKNOWN_DEVICE_REGISTRY_ID + " not found."));

        this.mockMvc.perform(get("/deviceregistry/mgmt/" + UNKNOWN_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void getDeviceRegistryEntryByIdNegativeId() throws Exception {
        this.mockMvc.perform(get("/deviceregistry/mgmt/" + INVALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void getDeviceRegistryEntriesByDeviceName() throws Exception {
        final DeviceRegistryListResponseDTO deviceListResponseDTO = new DeviceRegistryListResponseDTO(List.of(VALID_DEVICE_REGISTRY), 1);
        when(deviceRegistryDBService.getDeviceRegistryEntriesByDeviceName(eq(VALID_DEVICE_NAME), any(), any()))
                .thenReturn(deviceListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt/devicename/" + VALID_DEVICE_NAME)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final DeviceRegistryListResponseDTO responseBody = readResponse(response, DeviceRegistryListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    @Test
    public void getDeviceRegistryEntriesByUnknownDeviceName() throws Exception {
        final DeviceRegistryListResponseDTO deviceListResponseDTO = new DeviceRegistryListResponseDTO(List.of(), 0);
        when(deviceRegistryDBService.getDeviceRegistryEntriesByDeviceName(eq(UNKNOWN_DEVICE_NAME), any(), any()))
                .thenReturn(deviceListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt/devicename/" + UNKNOWN_DEVICE_NAME)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final DeviceRegistryListResponseDTO responseBody = readResponse(response, DeviceRegistryListResponseDTO.class);
        assertEquals(0, responseBody.getCount());
    }

    @Test
    public void getDeviceRegistryEntriesByInvalidDeviceName() throws Exception {
        this.mockMvc.perform(get("/deviceregistry/mgmt/devicename/" + INVALID_DEVICE_NAME)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void removeDeviceRegistryEntryByInvalidId() throws Exception {
        this.mockMvc.perform(delete("/deviceregistry/mgmt/" + INVALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void removeDeviceRegistryEntryByUnknownId() throws Exception {
        doThrow(new InvalidParameterException("No device registry with id : " + UNKNOWN_DEVICE_REGISTRY_ID))
                .when(deviceRegistryDBService)
                .removeDeviceRegistryEntryById(eq(UNKNOWN_DEVICE_REGISTRY_ID));

        this.mockMvc.perform(delete("/deviceregistry/mgmt/" + UNKNOWN_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void removeDeviceRegistryEntryById() throws Exception {

        this.mockMvc.perform(delete("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    @Test
    public void updateDeviceRegistryRequest() throws Exception {
        final DeviceRegistryRequestDTO dto = new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST,null, null, 1);
        when(deviceRegistryDBService.updateDeviceRegistryById(eq(VALID_DEVICE_REGISTRY_ID), any())).thenReturn(VALID_DEVICE_REGISTRY);

        final MvcResult result = this.mockMvc.perform(put("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                                              .accept(MediaType.APPLICATION_JSON)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsString(dto)))
                                             .andExpect(status().isOk())
                                             .andReturn();
        assertEquals(VALID_DEVICE_REGISTRY, objectMapper.readValue(result.getResponse().getContentAsString(), DeviceRegistryResponseDTO.class));
    }

    @Test
    public void updateDeviceRegistryUnknownId() throws Exception {
        final DeviceRegistryRequestDTO dto = new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST,null, null, 1);
        when(deviceRegistryDBService.updateDeviceRegistryById(eq(UNKNOWN_DEVICE_REGISTRY_ID), any()))
                .thenThrow(new InvalidParameterException("No device registry with id : " + UNKNOWN_DEVICE_REGISTRY_ID));

        this.mockMvc.perform(put("/deviceregistry/mgmt/" + UNKNOWN_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void updateDeviceRegistryInvalidId() throws Exception {
        final DeviceRegistryRequestDTO dto = new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST,null, null, 1);
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + INVALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void updateDeviceRegistryNullPayload() throws Exception {
        when(deviceRegistryDBService.updateDeviceRegistryById(eq(VALID_DEVICE_REGISTRY_ID), any())).thenReturn(VALID_DEVICE_REGISTRY);
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void mergeDeviceWithOneField() throws Exception {
        DeviceRegistryRequestDTO dto = null;
        when(deviceRegistryDBService.mergeDeviceRegistryById(eq(VALID_DEVICE_REGISTRY_ID), any())).thenReturn(VALID_DEVICE_REGISTRY);

        dto = new DeviceRegistryRequestDTO();
        dto.setDevice(VALID_DEVICE_REQUEST);
        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new DeviceRegistryRequestDTO();
        dto.setEndOfValidity(DATE_STRING);
        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new DeviceRegistryRequestDTO();
        dto.setMetadata(Map.of());
        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();

        dto = new DeviceRegistryRequestDTO();
        dto.setVersion(2);
        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + VALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    @Test
    public void mergeDeviceWithEmpty() throws Exception {
        final DeviceRegistryRequestDTO dto = new DeviceRegistryRequestDTO();
        when(deviceRegistryDBService.mergeDeviceRegistryById(eq(VALID_DEVICE_REGISTRY_ID), any())).thenReturn(VALID_DEVICE_REGISTRY);

        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + VALID_DEVICE_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void mergeDeviceUnknownId() throws Exception {
        final DeviceRegistryRequestDTO dto = new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST, DATE_STRING, null, 1);
        when(deviceRegistryDBService.mergeDeviceRegistryById(eq(UNKNOWN_DEVICE_REGISTRY_ID), any()))
                .thenThrow(new InvalidParameterException("No device with id : " + UNKNOWN_DEVICE_REGISTRY_ID));

        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + UNKNOWN_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void mergeDeviceInvalidId() throws Exception {
        final DeviceRegistryRequestDTO dto = new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST, DATE_STRING, null, 1);

        this.mockMvc.perform(patch("/deviceregistry/mgmt/" + INVALID_DEVICE_REGISTRY_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void registerDeviceNullPayload() throws Exception {
        when(deviceRegistryDBService.registerDeviceRegistry(any())).thenReturn(VALID_DEVICE_REGISTRY);

        this.mockMvc.perform(post("/deviceregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void registerDeviceNullEndOfValidity() throws Exception {
        final DeviceRegistryRequestDTO deviceRegistryRequestDTO =
                new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST, null, null, 1);

        when(deviceRegistryDBService.registerDeviceRegistry(any())).thenReturn(VALID_DEVICE_REGISTRY);

        this.mockMvc.perform(post("/deviceregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(deviceRegistryRequestDTO)))
                    .andExpect(status().isCreated())
                    .andReturn();
    }

    @Test
    public void registerDeviceInvalidEndOfValidity() throws Exception {
        final DeviceRegistryRequestDTO deviceRegistryRequestDTO =
                new DeviceRegistryRequestDTO(VALID_DEVICE_REQUEST, "not parsable", null, 1);

        when(deviceRegistryDBService.registerDeviceRegistry(any())).thenReturn(VALID_DEVICE_REGISTRY);

        this.mockMvc.perform(post("/deviceregistry/register")
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(deviceRegistryRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void unregisterDeviceNullDeviceName() throws Exception {
        this.mockMvc.perform(delete("/deviceregistry/unregister")
                                     .param("device_name", "")
                                     .param("mac_address", "macAddress"))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void unregisterDeviceNullMacAddress() throws Exception {
        this.mockMvc.perform(delete("/deviceregistry/unregister")
                                     .param("device_name", "device")
                                     .param("mac_address", ""))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }
    //=================================================================================================
    // Tests of device registry

    private <T> T readResponse(final MvcResult result, final Class<T> clz) throws IOException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clz);
    }
}