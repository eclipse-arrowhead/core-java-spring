package eu.arrowhead.core.datamanager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.core.datamanager.service.DataManagerDriver;
import eu.arrowhead.core.datamanager.service.HistorianService;
import eu.arrowhead.core.datamanager.service.ProxyService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataManagerMain.class)
@ContextConfiguration(classes = {DataManagerTestContext.class})
@ActiveProfiles("nonstrict")
public class DataManagerControllerSystemTest2 {

    //=================================================================================================
    // test data

    //=================================================================================================
    // members

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockHistorianService")
    private HistorianService historianService;

    @MockBean(name = "mockProxyService")
    private ProxyService proxyService;
    
    @MockBean(name = "mockDataManagerDriver")
	private DataManagerDriver dataManagerDriver;


    private static final String DATAMANAGER_PROXY_URI  = "/datamanager/proxy";
    private static final String DATAMANAGER_HISTORIAN_URI  = "/datamanager/historian";

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianServiceGetWrongServiceNameFlagFalse() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_HISTORIAN_URI + "/valid-system/wrong_service")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isNotFound()); // it passed the service name checks, but after that no results found in the historian as expected
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianServicePutWrongServiceNameFlagFalse() throws Exception {
		final Vector<SenML> content = new Vector<>(1);
		content.add(new SenML());
		
		 final MvcResult result = this.mockMvc.perform(put(DATAMANAGER_HISTORIAN_URI + "/valid-system/wrong_service")
				     						  .contentType(MediaType.APPLICATION_JSON_VALUE)
				     						  .content(objectMapper.writeValueAsBytes(content))
				     						  .accept(MediaType.APPLICATION_JSON))
                 	 						  .andExpect(status().isBadRequest())
                 	 						  .andReturn(); // it passed the service name checks, but after failed payload checks
		 final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		 Assert.assertEquals("Missing mandatory field: bn is null.", response.getErrorMessage());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxyServiceGetWrongServiceNameFlagFalse() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_PROXY_URI + "/valid-system/wrong_service")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isNotFound()); // it passed the service name checks, but after that no results found in the proxy as expected
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxyServicePutWrongServiceNameFlagFalse() throws Exception {
		final Vector<SenML> content = new Vector<>(1);
		content.add(new SenML());
		
		 final MvcResult result = this.mockMvc.perform(put(DATAMANAGER_PROXY_URI + "/valid-system/wrong_service")
				     						  .contentType(MediaType.APPLICATION_JSON_VALUE)
				     						  .content(objectMapper.writeValueAsBytes(content))
				     						  .accept(MediaType.APPLICATION_JSON))
                 	 						  .andExpect(status().isBadRequest())
                 	 						  .andReturn(); // it passed the service name checks, but after failed payload checks
		 final ErrorMessageDTO response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ErrorMessageDTO.class);
		 Assert.assertEquals("Missing mandatory field: bn is null.", response.getErrorMessage());
	}
}