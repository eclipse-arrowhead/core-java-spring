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

package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressDetector;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBServiceTestContext.class })
public class ServiceRegistryControllerSystemTest {
	
	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockServiceRegistryDBService") 
	private ServiceRegistryDBService serviceRegistryDBService;
	
	@MockBean
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@MockBean
	private NetworkAddressVerifier networkAddressVerifier;
	
	@MockBean
	private NetworkAddressDetector networkAddressDetector;
	
	private static final String SYSTEMS_URL = "/serviceregistry/mgmt/systems/";
	private static final String REGISTER_SYSTEM_URL = "/serviceregistry" + CommonConstants.OP_SERVICEREGISTRY_REGISTER_SYSTEM_URI;
	private static final String MOCKED_SYSTEM_NAME = "mockedSystemName";
	private static final String MOCKED_SYSTEM_ADDRESS = "mockedSystemAddress";
	private static final String MOCKED_SYSTEM_AUTHENTICATION_INFO = "mockedSystemAuthenticationInfo";
	private static final String PAGE = "page";
	private static final String ITEM_PER_PAGE = "item_per_page";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		ReflectionTestUtils.setField(networkAddressVerifier, "allowSelfAddressing", true);
		ReflectionTestUtils.setField(networkAddressVerifier, "allowNonRoutableAddressing", true);
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemsTestWithoutParameter() throws Exception  {
		final Page<System> systemEntryList = createSystemPageForDBMocking(5);
		final SystemListResponseDTO systemEntriesDTO = DTOConverter.convertSystemEntryListToSystemListResponseDTO(systemEntryList);

		when(serviceRegistryDBService.getSystemEntries(anyInt(), anyInt(), any(), any())).thenReturn(systemEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SYSTEMS_URL)
						 					   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemListResponseDTO.class);

		assertEquals(5, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemTestWithPageAndSizeParameter() throws Exception {
		final Page<System> systemEntryList = createSystemPageForDBMocking(5);
		final SystemListResponseDTO systemEntriesDTO = DTOConverter.convertSystemEntryListToSystemListResponseDTO(systemEntryList);
		
		when(serviceRegistryDBService.getSystemEntries(anyInt(), anyInt(), any(), any())).thenReturn(systemEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SYSTEMS_URL)
											   .param(PAGE, "0")
											   .param(ITEM_PER_PAGE, "5")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemListResponseDTO.class);
		
		assertEquals(5, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(SYSTEMS_URL)
					.param(ITEM_PER_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test 
	public void getSystemTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(SYSTEMS_URL)
					.param(PAGE, "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemTestWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(SYSTEMS_URL)
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemByIdTestWithInvalidId() throws Exception  {
		final long inValidSystemId = 0L;
		
		this.mockMvc.perform(get(SYSTEMS_URL + inValidSystemId)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemByIdTestWithValidId() throws Exception  {
		final System system = createSystemForDBMocking();
		final long validSystemId = 1L;
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		
		when(serviceRegistryDBService.getSystemById(validSystemId)).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get(SYSTEMS_URL + validSystemId)
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);

		Assert.assertTrue(0 < responseBody.getId());	
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithValidDefinition() throws Exception {
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(post(SYSTEMS_URL)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
					  						   .andExpect(status().isCreated())
					  						   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);
		
		Assert.assertTrue(request.getSystemName().trim().equalsIgnoreCase(responseBody.getSystemName().trim()));
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithNullPortDefinition() throws Exception {
		final SystemRequestDTO request = createNullPortSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithBelowPortRangePortDefinition() throws Exception {
		final SystemRequestDTO request = createBelowPortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithAbovePortRangePortDefinition() throws Exception {
		final SystemRequestDTO request = createAbovePortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
		
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithNullSystemNameDefinition() throws Exception {
		final SystemRequestDTO request = createNullSystemNameSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithEmptySystemNameDefinition() throws Exception {
		final SystemRequestDTO request = createEmptySystemNameSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithCoreSytemNames() throws Exception {
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			request.setSystemName(coreSystem.name());
			this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithWrongSystemNameDefinition() throws Exception {
		final SystemRequestDTO request = createWrongSystemNameSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithNullAddressDefinition() throws Exception {
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		doThrow(new InvalidParameterException("test msg")).when(networkAddressVerifier).verify(any());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setSkipped(true);
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		
		verify(networkAddressPreProcessor, times(1)).normalize(any());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithEmptyAddressDefinition() throws Exception {
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		when(networkAddressPreProcessor.normalize(any())).thenReturn("");
		doThrow(new InvalidParameterException("test msg")).when(networkAddressVerifier).verify(anyString());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setSkipped(true);
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		
		verify(networkAddressPreProcessor, times(1)).normalize(any());
	}

	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithValidDefinition() throws Exception {
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createValidSystemRequestDTO();
	
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
	
		final MvcResult response = this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
												.contentType(MediaType.APPLICATION_JSON)
												.content(objectMapper.writeValueAsBytes(request))
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isCreated())
												.andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);
	
		Assert.assertTrue(request.getSystemName().trim().equalsIgnoreCase(responseBody.getSystemName().trim()));
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithNullPortDefinition() throws Exception {
		final SystemRequestDTO request = createNullPortSystemRequestDTO();
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithBelowPortRangePortDefinition() throws Exception {
		final SystemRequestDTO request = createBelowPortRangePortSystemRequestDTO();
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithAbovePortRangePortDefinition() throws Exception {
		final SystemRequestDTO request = createAbovePortRangePortSystemRequestDTO();
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithNullSystemNameDefinition() throws Exception {
		final SystemRequestDTO request = createNullSystemNameSystemRequestDTO();
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithEmptySystemNameDefinition() throws Exception {
		final SystemRequestDTO request = createEmptySystemNameSystemRequestDTO();
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithCoreSytemNames() throws Exception {
		final SystemRequestDTO request = createValidSystemRequestDTO();
	
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			request.setSystemName(coreSystem.name());
			this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithWrongSystemNameDefinition() throws Exception {
		final SystemRequestDTO request = createWrongSystemNameSystemRequestDTO();
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithNullAddressDefinition() throws Exception {
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		doThrow(new InvalidParameterException("test msg")).when(networkAddressVerifier).verify(any());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setSkipped(true);
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		
		verify(networkAddressPreProcessor, times(1)).normalize(any());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithNullAddressDefinitionDetectionEnabled() throws Exception {
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		doThrow(new InvalidParameterException("test msg")).when(networkAddressVerifier).verify(any());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setDetectionSuccess(true);
		addressDetectionResult.setDetectedAddress("address");
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
		final ArgumentCaptor<String> addrCaptor = ArgumentCaptor.forClass(String.class);
		when(serviceRegistryDBService.createSystemResponse(anyString(), addrCaptor.capture(), anyInt(), anyString(), any())).thenReturn(new SystemResponseDTO());
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated());
		
		verify(networkAddressPreProcessor, times(1)).normalize(any());
		assertEquals("address", addrCaptor.getValue());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithEmptyAddressDefinition() throws Exception {
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		when(networkAddressPreProcessor.normalize(any())).thenReturn("");
		doThrow(new InvalidParameterException("test msg")).when(networkAddressVerifier).verify(anyString());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setSkipped(true);
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		
		verify(networkAddressPreProcessor, times(1)).normalize(any());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void registerSystemTestWithEmptyAddressDefinitionDetectionEnabled() throws Exception {
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		when(networkAddressPreProcessor.normalize(any())).thenReturn("");
		doThrow(new InvalidParameterException("test msg")).when(networkAddressVerifier).verify(anyString());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setDetectionSuccess(true);
		addressDetectionResult.setDetectedAddress("address");
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
		final ArgumentCaptor<String> addrCaptor = ArgumentCaptor.forClass(String.class);
		when(serviceRegistryDBService.createSystemResponse(anyString(), addrCaptor.capture(), anyInt(), anyString(), any())).thenReturn(new SystemResponseDTO());
	
		this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated());
		
		verify(networkAddressPreProcessor, times(1)).normalize(any());
		assertEquals("address", addrCaptor.getValue());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void unregisterSystemOk() throws Exception {
		final String systemName = "consumer";
		final String address = "address";
		final int port = 5000;
		
		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(serviceRegistryDBService).removeSystemByNameAndAddressAndPort(strCaptor.capture(), strCaptor.capture(), intCaptor.capture());
		
		this.mockMvc.perform(delete(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, systemName)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, address)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, String.valueOf(port))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		
		assertEquals(systemName, strCaptor.getAllValues().get(0));
		assertEquals(address, strCaptor.getAllValues().get(1));
		assertEquals(port, intCaptor.getValue().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void unregisterSystemInvalidSystemNname() throws Exception {
		final String systemName = "-consumer";
		final String address = "address";
		final int port = 5000;
		
		this.mockMvc.perform(delete(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, systemName)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, address)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, String.valueOf(port))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		
		verify(serviceRegistryDBService, never()).removeSystemByNameAndAddressAndPort(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void unregisterSystemInvalidAddress() throws Exception {
		final String systemName = "consumer";
		final String address = "-address";
		final int port = 5000;
		
		doThrow(new InvalidParameterException("test")).when(networkAddressVerifier).verify(any());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setSkipped(true);
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
		
		this.mockMvc.perform(delete(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, systemName)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, address)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, String.valueOf(port))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		
		verify(serviceRegistryDBService, never()).removeSystemByNameAndAddressAndPort(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void unregisterSystemInvalidAddressDetectionEnabled() throws Exception {
		final String systemName = "consumer";
		final String address = "-invalid_address";
		final int port = 5000;
		
		doThrow(new InvalidParameterException("test")).when(networkAddressVerifier).verify(any());
		final AddressDetectionResult addressDetectionResult = new AddressDetectionResult();
		addressDetectionResult.setDetectionSuccess(true);
		addressDetectionResult.setDetectedAddress("address");
		when(networkAddressDetector.detect(any())).thenReturn(addressDetectionResult);
		final ArgumentCaptor<String> addrCaptor = ArgumentCaptor.forClass(String.class);
		doNothing().when(serviceRegistryDBService).removeSystemByNameAndAddressAndPort(anyString(), addrCaptor.capture(), anyInt());
		
		this.mockMvc.perform(delete(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, systemName)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, address)
					.param(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT, String.valueOf(port))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		
		assertEquals("address", addrCaptor.getValue());
	}

	//-------------------------------------------------------------------------------------------------	
	@Test
	public void pullSystemsTestWithoutParameter() throws Exception  {
		final Page<System> systemEntryList = createSystemPageForDBMocking(5);
		final SystemListResponseDTO systemEntriesDTO = DTOConverter.convertSystemEntryListToSystemListResponseDTO(systemEntryList);

		when(serviceRegistryDBService.getSystemEntries(anyInt(), anyInt(), any(), any())).thenReturn(systemEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI)
						 					   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemListResponseDTO.class);

		assertEquals(5, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void pullSystemsTestWithPageAndSizeParameter() throws Exception {
		final Page<System> systemEntryList = createSystemPageForDBMocking(5);
		final SystemListResponseDTO systemEntriesDTO = DTOConverter.convertSystemEntryListToSystemListResponseDTO(systemEntryList);
		
		when(serviceRegistryDBService.getSystemEntries(anyInt(), anyInt(), any(), any())).thenReturn(systemEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI)
											   .param(PAGE, "0")
											   .param(ITEM_PER_PAGE, "5")
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemListResponseDTO.class);
		
		assertEquals(5, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void pullSystemsTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI)
					.param(ITEM_PER_PAGE, "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test 
	public void pullSystemsTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI)
					.param(PAGE, "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void pullSystemsTestWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get(CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI)
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void updateSystemByIdTestWithValidId() throws Exception  {
		final long validSystemId = 1L;
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		when(serviceRegistryDBService.updateSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(put(SYSTEMS_URL + validSystemId)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), SystemResponseDTO.class);

		Assert.assertTrue(0 < responseBody.getId());	
	}
		
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void updateSystemByIdTestWithInvalidId() throws Exception  {
		final long invalidSystemId = - 1L;
		final SystemRequestDTO request = createValidSystemRequestDTO();
				
		this.mockMvc.perform(put(SYSTEMS_URL + invalidSystemId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());	
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void updateSystemByIdTestWithCoreSytemNames() throws Exception {
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			request.setSystemName(coreSystem.name());
			this.mockMvc.perform(put(SYSTEMS_URL + 1)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void updateSystemByIdTestWithWrongNameDefinition() throws Exception  {
		final long validSystemId = 1L;
		final SystemRequestDTO request = createValidSystemRequestDTO();
		request.setSystemName("invalid-system-name-");
				
		this.mockMvc.perform(put(SYSTEMS_URL + validSystemId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemTestWithValidDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createValidSystemRequestDTO();

		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
											  .contentType(MediaType.APPLICATION_JSON)
											  .content(objectMapper.writeValueAsBytes(request))
											  .accept(MediaType.APPLICATION_JSON))
											  .andExpect(status().isOk())
											  .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);	

		Assert.assertTrue(request.getSystemName().trim().equalsIgnoreCase(responseBody.getSystemName().trim()));
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithNullPortDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createNullPortSystemRequestDTO();
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), isNull(), anyString(), any())).thenReturn(systemResponseDTO);
		
		this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithBelowPortRangePortDefinition() throws Exception {
		final long validSystemId = 1L;
		final SystemRequestDTO request = createBelowPortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithAbovePortRangePortDefinition() throws Exception {
		final long validSystemId = 1L;
		final SystemRequestDTO request = createAbovePortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
		
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithNullSystemNameDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createNullSystemNameSystemRequestDTO();

		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);

		assertNotNull(responseBody.getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithEmptySystemNameDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createEmptySystemNameSystemRequestDTO();
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);

		assertNotNull(responseBody.getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithCoreSytemNames() throws Exception {
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			request.setSystemName(coreSystem.name());
			this.mockMvc.perform(patch(SYSTEMS_URL + 1)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithWrongSystemNameDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createWrongSystemNameSystemRequestDTO();
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithNullAddressDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);

		assertNotNull(responseBody.getAddress());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithEmptyAddressDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(patch(SYSTEMS_URL + validSystemId)
											   .contentType(MediaType.APPLICATION_JSON)
											   .content(objectMapper.writeValueAsBytes(request))
											   .accept(MediaType.APPLICATION_JSON))
											   .andExpect(status().isOk())
											   .andReturn();
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);

		assertNotNull(responseBody.getAddress());
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeSystemTestWithValidId( ) throws Exception {
		final long validId = 1L;
		
		this.mockMvc.perform(delete(SYSTEMS_URL + validId)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeSystemTestWithInvalidId( ) throws Exception {
		final long inValidId = -1L;
		
		this.mockMvc.perform(delete(SYSTEMS_URL+inValidId)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private Page<System> createSystemPageForDBMocking(final int amountOfEntry) {
		final List<System> systemList = new ArrayList<>(amountOfEntry);
		
		for (int i = 0; i < amountOfEntry; ++i) {
			final System system = new System(MOCKED_SYSTEM_NAME + i, MOCKED_SYSTEM_ADDRESS, AddressType.HOSTNAME, i, MOCKED_SYSTEM_AUTHENTICATION_INFO, Utilities.map2Text(Map.of("key", "value")));
			system.setId(i);
			final ZonedDateTime timeStamp = ZonedDateTime.now();
			system.setCreatedAt(timeStamp);
			system.setUpdatedAt(timeStamp);
			systemList.add(system);
		}
		
		final Page<System> entries = new PageImpl<System>(systemList);
		
		return entries;
	}

	//-------------------------------------------------------------------------------------------------	
	private System createSystemForDBMocking() {
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		final System system = new System(systemName, address, AddressType.HOSTNAME, port, authenticationInfo, Utilities.map2Text(Map.of("key", "value")));
		
		system.setId(1);
		
		final ZonedDateTime timeStamp = ZonedDateTime.now();
		system.setCreatedAt(timeStamp);
		system.setUpdatedAt(timeStamp);
		
		return system;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createValidSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		systemRequestDTO.setMetadata(Map.of("key", "value"));
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createBelowPortRangePortSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createAbovePortRangePortSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
		
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createNullPortSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = null;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
		
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createNullSystemNameSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = null;
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createEmptySystemNameSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "   ";
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createWrongSystemNameSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "invalid_system_name";
		final String address = MOCKED_SYSTEM_ADDRESS;
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createNullAddressSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = null;
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createEmptyAddressSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = MOCKED_SYSTEM_NAME;
		final String address = "   ";
		final Integer port = 1;
		final String authenticationInfo = MOCKED_SYSTEM_AUTHENTICATION_INFO;
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
}