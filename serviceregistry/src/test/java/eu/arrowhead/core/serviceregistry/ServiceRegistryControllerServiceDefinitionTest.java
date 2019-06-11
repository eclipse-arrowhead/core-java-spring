package eu.arrowhead.core.serviceregistry;

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

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBSerrviceTestContext.class })
public class ServiceRegistryControllerServiceDefinitionTest {
	
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
	//Tests of getServiceDefinitions
	
	@Test
	public void getServiceDefinitionsTestWithoutParameter() throws Exception  {
		final Page<ServiceDefinition> serviceDefinitionEntries = createServiceDefinitionPageForDBMocking(5);
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntriesDTO = DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceDefinitionEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionsListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionsListResponseDTO.class);
		assertEquals(5, responseBody.getCount());
	}
	
	@Test
	public void getServiceDefinitionsTestWithPageAndSizeParameter() throws Exception {
		final Page<ServiceDefinition> serviceDefinitionEntries = createServiceDefinitionPageForDBMocking(5);
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntriesDTO = DTOConverter.convertServiceDefinitionsListToServiceDefinitionListResponseDTO(serviceDefinitionEntries);
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceDefinitionEntriesDTO);
		
		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("page", "0")
				.param("item_per_page", "5")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		final ServiceDefinitionsListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceDefinitionsListResponseDTO.class);
		assertEquals(5, responseBody.getCount());
	}
	
	@Test
	public void getServiceDefinitionsTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test 
	public void getServiceDefinitionsTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("page", "0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void getServiceDefinitionsTestWithInvalidSortDirectionFlagParametert() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("direction", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void getServiceDefinitionsTestWithIllegalSortFiledParameter() throws Exception {
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenThrow(new InvalidParameterException("test"));
		
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("sort_field", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// assistant methods
	
	private Page<ServiceDefinition> createServiceDefinitionPageForDBMocking(final int amountOfEntry) {
		final List<ServiceDefinition> serviceDefinitionList = new ArrayList<>();
		for (int i = 0; i < amountOfEntry; i++) {
			final ServiceDefinition serviceDefinition = new ServiceDefinition("mockedService" + i);
			serviceDefinition.setId(i);
			final ZonedDateTime timeStamp = ZonedDateTime.now();
			serviceDefinition.setCreatedAt(timeStamp);
			serviceDefinition.setUpdatedAt(timeStamp);
			serviceDefinitionList.add(serviceDefinition);
		}
		final Page<ServiceDefinition> entries = new PageImpl<ServiceDefinition>(serviceDefinitionList);
		return entries;
	}
}
