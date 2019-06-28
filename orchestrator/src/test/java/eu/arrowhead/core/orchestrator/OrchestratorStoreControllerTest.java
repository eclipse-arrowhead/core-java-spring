package eu.arrowhead.core.orchestrator;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;



@RunWith (SpringRunner.class)
@SpringBootTest(classes = OrchestratorMain.class)
@ContextConfiguration (classes = { OrchestratorStoreDBServiceTestContext.class })
public class OrchestratorStoreControllerTest {

	//=================================================================================================
	// members
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean(name = "mockOrchestratorStoreDBService") 
	OrchestratorStoreDBService orchestratorStoreDBService;
	
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
	public void testGetOrchestratorStoreWithInvalidId() throws Exception {
		this.mockMvc.perform(get(CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
}
