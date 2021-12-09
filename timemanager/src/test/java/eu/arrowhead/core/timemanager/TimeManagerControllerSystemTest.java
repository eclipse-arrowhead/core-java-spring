package eu.arrowhead.core.timemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.timemanager.database.service.TimeManagerDBService;
import eu.arrowhead.common.dto.shared.TimeManagerTimeResponseDTO;

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
import static org.junit.Assert.assertNotEquals;
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
@SpringBootTest(classes = TimeManagerMain.class)
@ContextConfiguration(classes = {TimeManagerTestContext.class})
public class TimeManagerControllerSystemTest {

    //=================================================================================================
    // test data

    //=================================================================================================
    // members

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "mockTimeManagerDBService")
    private TimeManagerDBService timeManagerDBService;

    private static final String TIMEMANAGER_ECHO_URI  = "/timemanager/echo";
    private static final String TIMEMANAGER_TIME_URI  = "/timemanager/time";

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        reset(timeManagerDBService);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    //=================================================================================================
    // Tests of Echo service

    @Test
    public void echoTM() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(TIMEMANAGER_ECHO_URI)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        assertEquals("Got it!", response.getResponse().getContentAsString());
    }

    @Test
    public void testTime() throws Exception {
        final MvcResult response = this.mockMvc.perform(get(TIMEMANAGER_TIME_URI)
                                               .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
        final TimeManagerTimeResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), TimeManagerTimeResponseDTO.class);
        assertNotEquals(0, responseBody.getEpoch());
    }

}
