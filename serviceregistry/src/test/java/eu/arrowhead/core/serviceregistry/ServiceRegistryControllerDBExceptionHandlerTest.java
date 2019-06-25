package eu.arrowhead.core.serviceregistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith (SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBServiceTestContext.class })
public class ServiceRegistryControllerDBExceptionHandlerTest {

	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
		
	@MockBean(name = "mockServiceRegistryDBService") 
	private ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void dbServiceThrowInvalidParameterExceptionTest() throws Exception {
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenThrow(new InvalidParameterException("test"));
		
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void dbServiceThrowArrowheadExceptionTest() throws Exception {
		when(serviceRegistryDBService.getServiceDefinitionEntriesResponse(anyInt(), anyInt(), any(), any())).thenThrow(new ArrowheadException("test"));
		
		this.mockMvc.perform(get("/serviceregistry/mgmt/services")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isInternalServerError());
	}
}