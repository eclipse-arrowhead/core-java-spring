package eu.arrowhead.core.serviceregistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.SystemListResponseDTO;
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
	//Tests of getSystems
	
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
	
	@Test
	public void getSystemTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test 
	public void getSystemTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("page", "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void getSystemTestWithInvalidSortDirectionFlagParametert() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems")
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void getSystemByIdTestWithInvalidId() throws Exception  {
		final System system = createSystemForDBMocking();
		
		long invalidSystemId = 0L;
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.getSystemById(invalidSystemId)).thenReturn(systemResponseDTO);
		
		this.mockMvc.perform(get("/serviceregistry/mgmt/systems/"+invalidSystemId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	
	}
	
	@Test
	public void getSystemByIdTestWithValidId() throws Exception  {
		final System system = createSystemForDBMocking();
		
		long validSystemId = 11;
		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(system);
		when(serviceRegistryDBService.getSystemById(validSystemId)).thenReturn(systemResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/systems/"+validSystemId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final SystemResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), SystemResponseDTO.class);
		assertThat(0 < responseBody.getId());	
	
	}

	//=================================================================================================
	// assistant methods

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

	
	private System createSystemForDBMocking() {
		final String systemName = "mockSystemName";
		final String address = "mockSystemAddress";
		final Integer port = 1;
		final String authenticationInfo = null;
		
		System system = new System(systemName, address, port, authenticationInfo);
		
		system.setId(1);
		
		final ZonedDateTime timeStamp = ZonedDateTime.now();
		system.setCreatedAt(timeStamp);
		system.setUpdatedAt(timeStamp);
		
		return system;
	}
}
