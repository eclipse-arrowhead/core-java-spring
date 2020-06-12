package eu.arrowhead.core.gams.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.core.gams.GamsTestContext;
import eu.arrowhead.core.gams.database.entities.AbstractSensorData;
import eu.arrowhead.core.gams.database.entities.GamsInstance;
import eu.arrowhead.core.gams.database.entities.Sensor;
import eu.arrowhead.core.gams.database.repositories.GamsInstanceRepository;
import eu.arrowhead.core.gams.database.repositories.SensorDataRepository;
import eu.arrowhead.core.gams.database.repositories.SensorRepository;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import eu.arrowhead.core.gams.rest.dto.SensorDto;
import eu.arrowhead.core.gams.rest.dto.SensorType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {GamsTestContext.class})
public class SensorControllerTest {


    private static final UUID VALID_GAMS_ID = UUID.randomUUID();
    private static final GamsInstance VALID_GAMS_INSTANCE = new GamsInstance("gams", VALID_GAMS_ID);

    private static final UUID VALID_EVENT_SENSOR_ID = UUID.randomUUID();
    private static final UUID VALID_FLOATING_SENSOR_ID = UUID.randomUUID();
    private static final UUID VALID_INTEGER_SENSOR_ID = UUID.randomUUID();
    private static final Sensor VALID_EVENT_SENSOR = new Sensor(VALID_GAMS_INSTANCE, "sensor", VALID_EVENT_SENSOR_ID, SensorType.EVENT);
    private static final Sensor VALID_FLOATING_SENSOR = new Sensor(VALID_GAMS_INSTANCE, "sensor", VALID_FLOATING_SENSOR_ID, SensorType.FLOATING_POINT_NUMBER);
    private static final Sensor VALID_INTEGER_SENSOR = new Sensor(VALID_GAMS_INSTANCE, "sensor", VALID_INTEGER_SENSOR_ID, SensorType.INTEGER_NUMBER);


    //=================================================================================================
    // members

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GamsInstanceRepository gamsInstanceRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    private MockMvc mockMvc;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        Mockito.when(gamsInstanceRepository.findByUid(VALID_GAMS_ID)).thenReturn(Optional.of(VALID_GAMS_INSTANCE));
        Mockito.when(sensorRepository.findByUid(VALID_EVENT_SENSOR_ID)).thenReturn(Optional.of(VALID_EVENT_SENSOR));
        Mockito.when(sensorRepository.findByUid(VALID_FLOATING_SENSOR_ID)).thenReturn(Optional.of(VALID_FLOATING_SENSOR));
        Mockito.when(sensorRepository.findByUid(VALID_INTEGER_SENSOR_ID)).thenReturn(Optional.of(VALID_INTEGER_SENSOR));
        Mockito.when(sensorDataRepository.saveAndFlush(any(AbstractSensorData.class))).thenAnswer(firstArgument());
    }

    //=================================================================================================
    // Tests of gams sensor

    @Test(expected = NestedServletException.class)
    public void publishObject() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), new SensorDto("abc", "abc", null));
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_INTEGER_SENSOR_ID)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public void publishLong() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), 123L);
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_INTEGER_SENSOR_ID)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void publishDouble() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), 12.3);
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_FLOATING_SENSOR_ID)
                                                                .content(objectMapper.writeValueAsString(request))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
    }

    @Test
    public void publishEvent() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), "event");
        final MvcResult response = this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_EVENT_SENSOR_ID)
                                                                .content(objectMapper.writeValueAsString(request))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON))
                                               .andExpect(status().isOk())
                                               .andReturn();
    }

    private <T> Answer<T> firstArgument() {
        return invocationOnMock -> invocationOnMock.getArgument(0);
    }

    private String timestamp() {
        try {
            return Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}