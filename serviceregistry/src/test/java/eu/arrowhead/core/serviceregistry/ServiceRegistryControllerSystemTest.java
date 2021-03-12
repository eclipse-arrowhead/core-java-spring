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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
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
	ServiceRegistryDBService serviceRegistryDBService;
	
	private static final String SYSTEMS_URL = "/serviceregistry/mgmt/systems/";
	private static final String REGISTER_SYSTEM_URL = "/serviceregistry/" + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_SYSTEM_URI;
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
	public void getSystemTestWithInvalidSortDirectionFlagParametert() throws Exception {
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
		
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
	public void addSystemTestWithNullAddressDefinition() throws Exception {
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithEmptyAddressDefinition() throws Exception {
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		
		this.mockMvc.perform(post(SYSTEMS_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(request))
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithValidDefinition() throws Exception {
	final System system = createSystemForDBMocking();
	final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
	final SystemRequestDTO request = createValidSystemRequestDTO();

	when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);

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
public void addConsumerSystemTestWithNullPortDefinition() throws Exception {
	final SystemRequestDTO request = createNullPortSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithBelowPortRangePortDefinition() throws Exception {
	final SystemRequestDTO request = createBelowPortRangePortSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithAbovePortRangePortDefinition() throws Exception {
	final SystemRequestDTO request = createAbovePortRangePortSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithNullSystemNameDefinition() throws Exception {
	final SystemRequestDTO request = createNullSystemNameSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithEmptySystemNameDefinition() throws Exception {
	final SystemRequestDTO request = createEmptySystemNameSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithCoreSytemNames() throws Exception {
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
public void addConsumerSystemTestWithNullAddressDefinition() throws Exception {
	final SystemRequestDTO request = createNullAddressSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
}

//-------------------------------------------------------------------------------------------------	
@Test
public void addConsumerSystemTestWithEmptyAddressDefinition() throws Exception {
	final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();

	this.mockMvc.perform(post(REGISTER_SYSTEM_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
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
		
		when(serviceRegistryDBService.updateSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
	public void updateSystemByIdTestWithInValidId() throws Exception  {
		final long inValidSystemId = - 1L;
		final SystemRequestDTO request = createValidSystemRequestDTO();
				
		this.mockMvc.perform(put(SYSTEMS_URL + inValidSystemId)
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
	public void mergeSystemTestWithValidDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createValidSystemRequestDTO();

		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), isNull(), anyString())).thenReturn(systemResponseDTO);
		
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

		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
	public void mergeSystemTestWithNullAddressDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
		
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
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
			final System system = new System(MOCKED_SYSTEM_NAME + i, MOCKED_SYSTEM_ADDRESS, i, MOCKED_SYSTEM_AUTHENTICATION_INFO );
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
		
		final System system = new System(systemName, address, port, authenticationInfo);
		
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