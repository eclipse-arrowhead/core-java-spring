package eu.arrowhead.core.serviceregistry;

import static org.assertj.core.api.Assertions.assertThat;
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
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.SystemListResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBSerrviceTestContext.class })
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
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
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
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("page", "0")
				.param("item_per_page", "5")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final SystemListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemListResponseDTO.class);
		assertEquals(5, responseBody.getCount());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test 
	public void getSystemTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("page", "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemTestWithInvalidSortDirectionFlagParametert() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void getSystemByIdTestWithInvalidId() throws Exception  {
		final System system = createSystemForDBMocking();
		
		final long invalidSystemId = 0L;
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.getSystemById(invalidSystemId)).thenReturn(systemResponseDTO);
		
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems/"+invalidSystemId)
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
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/systems/"+validSystemId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);
		assertThat(0 < responseBody.getId());	
	
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithValidDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn();
		
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);
		
		assertEquals(request.getSystemName(), responseBody.getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithNullPortDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createNullPortSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithBelowPortRangePortDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createBelowPortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithAbovePortRangePortDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createAbovePortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
		
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithNullSystemNameDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createNullSystemNameSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithEmptySystemNameDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createEmptySystemNameSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}	
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithNullAddressDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void addSystemTestWithEmptyAddressDefinition() throws Exception {
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.createSystemResponse(anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		
		this.mockMvc.perform(post("/serviceregistry/mgmt/systems")
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
		
		when(serviceRegistryDBService.updateSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createValidSystemRequestDTO();
				
		final MvcResult response = this.mockMvc.perform(put("/serviceregistry/mgmt/systems/"+validSystemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsByteArray(), SystemResponseDTO.class);
		assertThat(0 < responseBody.getId());	
	
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void updateSystemByIdTestWithInValidId() throws Exception  {

		final long inValidSystemId = - 1L;
		
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		
		when(serviceRegistryDBService.updateSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createValidSystemRequestDTO();
				
		this.mockMvc.perform(put("/serviceregistry/mgmt/systems/"+inValidSystemId)
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
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createValidSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);	
		assertEquals(request.getSystemName(), responseBody.getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithNullPortDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), isNull(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createNullPortSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		java.lang.System.out.println("systemResponseDTO >> "+response.getResponse().getContentAsString());

		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);
		assertNotNull(responseBody.getSystemName());
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithBelowPortRangePortDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createBelowPortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------	
	@Test
	public void mergeSystemTestWithAbovePortRangePortDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createAbovePortRangePortSystemRequestDTO();
		
		this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
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
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createNullSystemNameSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
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
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createEmptySystemNameSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
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
	public void mergeSystemTestWithNullAddressDefinition() throws Exception {
		final long validSystemId = 1L;
		final System system = createSystemForDBMocking();
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createNullAddressSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
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
		when(serviceRegistryDBService.mergeSystemResponse(anyLong(), anyString(), anyString(), anyInt(), anyString())).thenReturn(systemResponseDTO);
		
		final SystemRequestDTO request = createEmptyAddressSystemRequestDTO();
		
		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/systems/"+validSystemId)
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
		
		this.mockMvc.perform(delete("/serviceregistry/mgmt/systems/"+validId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeSystemTestWithInvalidId( ) throws Exception {
		
		final long inValidId = -1L;
		
		this.mockMvc.perform(delete("/serviceregistry/mgmt/systems/"+inValidId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private Page<System> createSystemPageForDBMocking(final int amountOfEntry) {
		
		final List<System> systemList = new ArrayList<>(amountOfEntry);
		
		for (int i = 0; i < amountOfEntry; i++) {
			final System system = new System("mockedSystem" + i, "mockAddress", i, null );
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
		final String systemName = "mockSystemName";
		final String address = "mockSystemAddress";
		final Integer port = 1;
		final String authenticationInfo = "";
		
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
		
		final String systemName = "mockSystemName";
		final String address = "mockSystemAddress";
		final Integer port = 1;
		final String authenticationInfo = "...";
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createBelowPortRangePortSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "mockSystemName";
		final String address = "mockSystemAddress";
		final Integer port = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final String authenticationInfo = "...";
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createAbovePortRangePortSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "mockSystemName";
		final String address = "mockSystemAddress";
		final Integer port = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final String authenticationInfo = "...";
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
		
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createNullPortSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "mockSystemName";
		final String address = "mockSystemAddress";
		final Integer port = null;
		final String authenticationInfo = "...";
		
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
		final String address = "mockSystemAddress";
		final Integer port = 1;
		final String authenticationInfo = "...";
		
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
		final String address = "mockSystemAddress";
		final Integer port = 1;
		final String authenticationInfo = "...";
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createNullAddressSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "mockSystemName";
		final String address = null;
		final Integer port = 1;
		final String authenticationInfo = "...";
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createEmptyAddressSystemRequestDTO() {	
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		
		final String systemName = "mockSystemName";
		final String address = "   ";
		final Integer port = 1;
		final String authenticationInfo = "...";
		
		systemRequestDTO.setSystemName(systemName);
		systemRequestDTO.setAddress(address);
		systemRequestDTO.setPort(port);
		systemRequestDTO.setAuthenticationInfo(authenticationInfo);
		
		return systemRequestDTO;
	}
	
}
