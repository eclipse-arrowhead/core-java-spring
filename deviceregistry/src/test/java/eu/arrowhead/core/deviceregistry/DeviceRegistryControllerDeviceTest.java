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
import static org.mockito.ArgumentMatchers.anyString;
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

import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DeviceRegistryTestContext.class})
public class DeviceRegistryControllerDeviceTest {


    //=================================================================================================
    // test data
    private final static long VALID_DEVICE_ID = 1L;
    private final static long UNKNOWN_DEVICE_ID = 2L;
    private final static long NEGATIVE_DEVICE_ID = -1L;

    private final static DeviceResponseDTO VALID_DEVICE = new DeviceResponseDTO(VALID_DEVICE_ID, "device", "address", "macAddress",
            "authenticationInfo", "2020-01-01T00:00:00", "2020-01-01T00:00:00");


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

    //=================================================================================================
    // Tests of device registry

    @Test
    public void echoOnboarding() throws Exception {
        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/echo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("Got it!", response.getResponse().getContentAsString());
    }

    @Test
    public void getDeviceById() throws Exception {
        when(deviceRegistryDBService.getDeviceById(1)).thenReturn(VALID_DEVICE);
        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt/device/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final DeviceResponseDTO responseDTO = objectMapper.readValue(response.getResponse().getContentAsString(), DeviceResponseDTO.class);
        assertEquals(VALID_DEVICE, responseDTO);
    }

    @Test
    public void getDeviceByIdUnknownId() throws Exception {
        when(deviceRegistryDBService.getDeviceById(UNKNOWN_DEVICE_ID)).thenThrow(new InvalidParameterException("Device with id " + UNKNOWN_DEVICE_ID + " not found."));

        this.mockMvc.perform(get("/deviceregistry/mgmt/device/" + UNKNOWN_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void getDeviceByIdNegativeId() throws Exception {
        this.mockMvc.perform(get("/deviceregistry/mgmt/device/" + NEGATIVE_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void getDevicesWithoutParameter() throws Exception {
        final DeviceListResponseDTO deviceListResponseDTO = new DeviceListResponseDTO(List.of(VALID_DEVICE), 1);
        when(deviceRegistryDBService.getDeviceEntries(any(), any())).thenReturn(deviceListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final DeviceListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), DeviceListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    @Test
    public void getDevicesWithPageAndSizeParameter() throws Exception {
        final DeviceListResponseDTO deviceListResponseDTO = new DeviceListResponseDTO(List.of(VALID_DEVICE), 1);
        when(deviceRegistryDBService.getDeviceEntries(any(), any())).thenReturn(deviceListResponseDTO);

        final MvcResult response = this.mockMvc.perform(get("/deviceregistry/mgmt/devices")
                .param("page", "0")
                .param("item_per_page", String.valueOf(5))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final DeviceListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), DeviceListResponseDTO.class);
        assertEquals(1, responseBody.getCount());
    }

    @Test
    public void createDeviceRequest() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        final MvcResult result = this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(VALID_DEVICE, objectMapper.readValue(result.getResponse().getContentAsString(), DeviceResponseDTO.class));
    }

    @Test
    public void createDeviceRequestNullPayload() throws Exception {
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createDeviceRequestNullDeviceName() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO(null, "address", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createDeviceRequestEmptyDeviceName() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("", "address", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createDeviceRequestNullAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", null, "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createDeviceRequestEmptyAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createDeviceRequestNullMacAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", null, "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createDeviceRequestEmptyMacAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", " ", "authenticationInfo");
        when(deviceRegistryDBService.createDeviceDto(anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(post("/deviceregistry/mgmt/devices/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequest() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.updateDeviceByIdResponse(eq(VALID_DEVICE_ID), anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);

        final MvcResult result = this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(VALID_DEVICE, objectMapper.readValue(result.getResponse().getContentAsString(), DeviceResponseDTO.class));
    }

    @Test
    public void updateDeviceUnknownId() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.updateDeviceByIdResponse(eq(UNKNOWN_DEVICE_ID), anyString(), anyString(), anyString(), anyString())).thenThrow(new InvalidParameterException("No device with id : " + UNKNOWN_DEVICE_ID));

        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + UNKNOWN_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceInvalidId() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");

        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + NEGATIVE_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestNullPayload() throws Exception {
        when(deviceRegistryDBService.updateDeviceByIdResponse(eq(VALID_DEVICE_ID), anyString(), anyString(), anyString(), anyString())).thenReturn(VALID_DEVICE);
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestNullDeviceName() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO(null, "address", "macAddress", "authenticationInfo");
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestEmptyDeviceName() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("", "address", "macAddress", "authenticationInfo");
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestNullAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", null, "macAddress", "authenticationInfo");
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestEmptyAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "", "macAddress", "authenticationInfo");
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestNullMacAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", null, "authenticationInfo");
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void updateDeviceRequestEmptyMacAddress() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", " ", "authenticationInfo");
        this.mockMvc.perform(put("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void mergeDeviceWithOneField() throws Exception {
        DeviceRequestDTO dto = null;
        when(deviceRegistryDBService.mergeDevice(eq(VALID_DEVICE_ID), any())).thenReturn(VALID_DEVICE);

        dto = new DeviceRequestDTO();
        dto.setDeviceName("not empty");
        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        dto = new DeviceRequestDTO();
        dto.setMacAddress("not empty");
        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        dto = new DeviceRequestDTO();
        dto.setAddress("not empty");
        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        dto = new DeviceRequestDTO();
        dto.setAuthenticationInfo("not empty");
        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void mergeDeviceWithEmpty() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO();
        when(deviceRegistryDBService.mergeDevice(eq(VALID_DEVICE_ID), any())).thenReturn(VALID_DEVICE);

        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void mergeDeviceUnknownId() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");
        when(deviceRegistryDBService.mergeDevice(eq(UNKNOWN_DEVICE_ID), any())).thenThrow(new InvalidParameterException("No device with id : " + UNKNOWN_DEVICE_ID));

        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + UNKNOWN_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void mergeDeviceInvalidId() throws Exception {
        final DeviceRequestDTO dto = new DeviceRequestDTO("device", "address", "macAddress", "authenticationInfo");

        this.mockMvc.perform(patch("/deviceregistry/mgmt/devices/" + NEGATIVE_DEVICE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void removeDeviceInvalidId() throws Exception {
        this.mockMvc.perform(delete("/deviceregistry/mgmt/devices/" + NEGATIVE_DEVICE_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void removeDeviceUnknownId() throws Exception {
        doThrow(new InvalidParameterException("No device with id : " + UNKNOWN_DEVICE_ID))
                .when(deviceRegistryDBService)
                .removeDeviceById(eq(UNKNOWN_DEVICE_ID));

        this.mockMvc.perform(delete("/deviceregistry/mgmt/devices/" + UNKNOWN_DEVICE_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();
    }

    @Test
    public void removeDevice() throws Exception {

        this.mockMvc.perform(delete("/deviceregistry/mgmt/devices/" + VALID_DEVICE_ID)
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }
}