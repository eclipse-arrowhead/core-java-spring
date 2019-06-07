package eu.arrowhead.core.serviceregistry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@AutoConfigureMockMvc
public class ServiceRegistryControllerServiceDefinitionTest {
	
	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;

	@InjectMocks
	ServiceRegistryController serviceRegistryController;
	
	@Mock
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
	public void getServiceDefinitionsTestWithoutParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	public void getServiceDefinitionsTestWithPageAndSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("page", "0")
				.param("item_per_page", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
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
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
				.param("sort_field", "invalid")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// assistant methods
	
	private Page<ServiceDefinition> createServiceDefinitionPageForDBMocking(int amountOfEntry) {
		List<ServiceDefinition> serviceDefinitionList = new ArrayList<>();
		for (int i = 0; i < amountOfEntry; i++) {
			ServiceDefinition serviceDefinition = new ServiceDefinition("mockedService" + i);
			serviceDefinition.setId(i);
			ZonedDateTime timeStamp = ZonedDateTime.now();
			serviceDefinition.setCreatedAt(timeStamp);
			serviceDefinition.setUpdatedAt(timeStamp);
			serviceDefinitionList.add(serviceDefinition);
		}
		Page<ServiceDefinition> entries = new PageImpl<ServiceDefinition>(serviceDefinitionList);
		return entries;
	}
}
