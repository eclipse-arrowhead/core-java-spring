package eu.arrowhead.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.dto.shared.ConfigurationRequestDTO;
import eu.arrowhead.common.dto.shared.ConfigurationResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationListResponseDTO;

import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.configuration.database.service.ConfigurationDBService;
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
import java.util.ArrayList;
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
@ContextConfiguration(classes = {ConfigurationTestContext.class})
public class ConfigurationControllerSystemTest {

    //=================================================================================================
    // test data
    private final static String DATE_STRING = "2021-01-26T13:22:45";

    private final static long VALID_CONFIG_ID = 1L;

    private final static String VALID_SYSTEM_NAME = "system";
    private final static String UNKNOWN_SYSTEM_NAME = "unknown";
    private final static String INVALID_SYSTEM_NAME = "";

    private final static String VALID_DATA = "c2VydmVySVA9MTAuOC4wLjEwMA==";
    private final static String VALID_RAWDATA = "serverIP=10.8.0.100";

    private final static ConfigurationResponseDTO VALID_CONF = new ConfigurationResponseDTO(VALID_CONFIG_ID, VALID_SYSTEM_NAME, "myconf.cfg", "text/plain", VALID_DATA, DATE_STRING, DATE_STRING);
    private final static List<ConfigurationResponseDTO> EMPTY_CONF_LIST = new ArrayList<ConfigurationResponseDTO>();
    private final static ConfigurationListResponseDTO EMPTY_CONF_RESP = new ConfigurationListResponseDTO();

    //=================================================================================================
    // members

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockConfigurationDBService")
    private ConfigurationDBService configurationDBService;

    private static final String CONFIGURATION_ECHO_URI  = "/configuration/echo";
    private static final String CONFIGUATION_CONF_URI  = "/configuration/conf";
    private static final String CONFIGUATION_RAWCONF_URI  = "/configuration/rawconf";

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        reset(configurationDBService);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    //=================================================================================================
    // Tests of Echo service

    @Test
    public void echoConfiguration() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(CONFIGURATION_ECHO_URI)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        assertEquals("Got it!", response.getResponse().getContentAsString());
    }

    @Test
    public void getConfigurationEntry() throws Exception {
        when(configurationDBService.getConfigForSystem(any())).thenReturn(VALID_CONF);

        final MvcResult response = this.mockMvc.perform(get("/configuration/conf/" + VALID_SYSTEM_NAME)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final ConfigurationResponseDTO responseBody = readResponse(response, ConfigurationResponseDTO.class);
        assertEquals(1, responseBody.getId());
    }

    @Test
    public void getRawConfigurationEntry() throws Exception {
        when(configurationDBService.getConfigForSystem(any())).thenReturn(VALID_CONF);

        final MvcResult response = this.mockMvc.perform(get("/configuration/rawconf/" + VALID_SYSTEM_NAME)
                                               .accept(MediaType.TEXT_PLAIN))
                                               .andExpect(status().isOk())
                                               .andReturn();

        final String responseText = response.getResponse().getContentAsString();
        assertEquals(VALID_RAWDATA, responseText);
    }

    @Test
    public void listConfigurations() throws Exception {
        when(configurationDBService.getAllConfigurations()).thenReturn(EMPTY_CONF_RESP);

        final MvcResult response = this.mockMvc.perform(get("/configuration/mgmt/config")
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        
        final ConfigurationListResponseDTO responseBody = readResponse(response, ConfigurationListResponseDTO.class);
        assertEquals(responseBody.getCount(), 0);
        assertEquals(responseBody.getData().size(), 0);
    }


    //=================================================================================================
    // Tests of Configuration system

    private <T> T readResponse(final MvcResult result, final Class<T> clz) throws IOException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clz);
    }

}
