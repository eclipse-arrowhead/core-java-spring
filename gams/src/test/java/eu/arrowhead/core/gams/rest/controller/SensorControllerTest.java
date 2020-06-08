package eu.arrowhead.core.gams.rest.controller;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.core.gams.GamsTestContext;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {GamsTestContext.class})
public class SensorControllerTest {


    private static final String VALID_GAMS_ID = "gams";
    private static final String VALID_SENSOR_ID = "sensor";


    //=================================================================================================
    // members

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    //=================================================================================================
    // Tests of gams sensor

    @Test
    public void publishLong() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), 123L);

        //when(deviceRegistryDBService.getDeviceById(1)).thenReturn(VALID_DEVICE);
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_SENSOR_ID)
                                                                .content(objectMapper.writeValueAsString(request))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
    }

    @Test
    public void publishDouble() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), 12.3);

        //when(deviceRegistryDBService.getDeviceById(1)).thenReturn(VALID_DEVICE);
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_SENSOR_ID)
                                                                .content(objectMapper.writeValueAsString(request))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
    }

    @Test
    public void publishEvent() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), "event");

        //when(deviceRegistryDBService.getDeviceById(1)).thenReturn(VALID_DEVICE);
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_SENSOR_ID)
                                                                .content(objectMapper.writeValueAsString(request))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
    }

    public String timestamp() {
        try {
            return Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}