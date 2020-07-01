package eu.arrowhead.core.datamanager;

import com.fasterxml.jackson.databind.ObjectMapper;
//import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
//import eu.arrowhead.common.dto.shared.SystemRequestDTO;
//import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.dto.shared.DataManagerSystemsResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
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

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockBean(name = "mockDataManagerDBService")
    private DataManagerDBService dataManagerDBService;

    private static final String DATAMANAGER_ECHO_URI  = "/datamanager/echo";
    private static final String DATAMANAGER_PROXY_URI  = "/datamanager/proxy";
    private static final String DATAMANAGER_HISTORIAN_URI  = "/datamanager/historian";

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        reset(dataManagerDBService);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    //=================================================================================================
    // Tests of Historian echo service

    @Test
    public void echoHistorian() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(DATAMANAGER_ECHO_URI)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        assertEquals("Got it!", response.getResponse().getContentAsString());
    }

    @Test
    public void testProxy() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(DATAMANAGER_PROXY_URI)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final DataManagerSystemsResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), DataManagerSystemsResponseDTO.class);
        assertEquals(0, responseBody.getSystems().size());
    }

    @Test
    public void testHistorian() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(DATAMANAGER_HISTORIAN_URI)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final DataManagerSystemsResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), DataManagerSystemsResponseDTO.class);
        assertEquals(0, responseBody.getSystems().size());
    }
}
