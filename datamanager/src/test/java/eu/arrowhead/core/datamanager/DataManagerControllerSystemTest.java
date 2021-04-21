package eu.arrowhead.core.datamanager;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Vector;

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

import eu.arrowhead.common.dto.shared.DataManagerSystemsResponseDTO;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.core.datamanager.service.HistorianService;
import eu.arrowhead.core.datamanager.service.ProxyService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {DataManagerTestContext.class})
public class DataManagerControllerSystemTest {

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

    private static final String DATAMANAGER_ECHO_URI  = "/datamanager/echo";
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
    public void echoHistorian() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(DATAMANAGER_ECHO_URI)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        assertEquals("Got it!", response.getResponse().getContentAsString());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void testProxy() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(DATAMANAGER_PROXY_URI)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final DataManagerSystemsResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), DataManagerSystemsResponseDTO.class);
        assertEquals(0, responseBody.getSystems().size());
    }

    //-------------------------------------------------------------------------------------------------
	@Test
    public void testHistorian() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(DATAMANAGER_HISTORIAN_URI)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final DataManagerSystemsResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), DataManagerSystemsResponseDTO.class);
        assertEquals(0, responseBody.getSystems().size());
    }
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianSystemGetWrongSystemName() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_HISTORIAN_URI + "/wrong_system")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianServiceGetWrongSystemName() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_HISTORIAN_URI + "/wrong_system/valid-service")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianServiceGetWrongServiceNameFlagTrue() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_HISTORIAN_URI + "/valid-system/wrong_service")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianServicePutWrongSystemName() throws Exception {
		final Vector<SenML> content = new Vector<>(1);
		content.add(new SenML());
		
		 this.mockMvc.perform(put(DATAMANAGER_HISTORIAN_URI + "/wrong_system/valid-service")
				     .contentType(MediaType.APPLICATION_JSON_VALUE)
				     .content(objectMapper.writeValueAsBytes(content))
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHistorianServicePutWrongServiceNameFlagTrue() throws Exception {
		final Vector<SenML> content = new Vector<>(1);
		content.add(new SenML());
		
		 this.mockMvc.perform(put(DATAMANAGER_HISTORIAN_URI + "/valid-system/wrong_service")
				     .contentType(MediaType.APPLICATION_JSON_VALUE)
				     .content(objectMapper.writeValueAsBytes(content))
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxySystemGetWrongSystemName() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_PROXY_URI + "/wrong_system")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxyServiceGetWrongSystemName() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_PROXY_URI + "/wrong_system/valid-service")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxyServiceGetWrongServiceNameFlagTrue() throws Exception {
		 this.mockMvc.perform(get(DATAMANAGER_PROXY_URI + "/valid-system/wrong_service")
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxyServicePutWrongSystemName() throws Exception {
		final Vector<SenML> content = new Vector<>(1);
		content.add(new SenML());
		
		 this.mockMvc.perform(put(DATAMANAGER_PROXY_URI + "/wrong_system/valid-service")
				     .contentType(MediaType.APPLICATION_JSON_VALUE)
				     .content(objectMapper.writeValueAsBytes(content))
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testProxyServicePutWrongServiceNameFlagTrue() throws Exception {
		final Vector<SenML> content = new Vector<>(1);
		content.add(new SenML());
		
		 this.mockMvc.perform(put(DATAMANAGER_PROXY_URI + "/valid-system/wrong_service")
				     .contentType(MediaType.APPLICATION_JSON_VALUE)
				     .content(objectMapper.writeValueAsBytes(content))
          			 .accept(MediaType.APPLICATION_JSON))
                 	 .andExpect(status().isBadRequest());
	}
}