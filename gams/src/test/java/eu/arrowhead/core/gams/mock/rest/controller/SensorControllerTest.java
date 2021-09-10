package eu.arrowhead.core.gams.mock.rest.controller;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.AbstractEntity;
import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.repository.GamsInstanceRepository;
import eu.arrowhead.common.database.repository.SensorDataRepository;
import eu.arrowhead.common.database.repository.SensorRepository;
import eu.arrowhead.core.gams.mock.GamsTestContext;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {GamsTestContext.class})
public class SensorControllerTest {


    private static final UUID VALID_GAMS_ID = UUID.randomUUID();
    private static final GamsInstance VALID_GAMS_INSTANCE = new GamsInstance("gams", VALID_GAMS_ID, 0L);

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

    @Test
    public void publishLong() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), 123L);
        this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_INTEGER_SENSOR_ID)
                                     .content(objectMapper.writeValueAsString(request))
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    @Test
    public void publishDouble() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), 12.3);
        this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_FLOATING_SENSOR_ID)
                                     .content(objectMapper.writeValueAsString(request))
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    @Test
    public void publishEvent() throws Exception {

        final PublishSensorDataRequest request = new PublishSensorDataRequest(timestamp(), "event");
        this.mockMvc.perform(post("/gams/{uid}/sensor/{sensor}", VALID_GAMS_ID, VALID_EVENT_SENSOR_ID)
                                     .content(objectMapper.writeValueAsString(request))
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
    }

    private <T> Answer<T> firstArgument() {
        return invocationOnMock -> {
            final T obj = invocationOnMock.getArgument(0);
            if(Objects.nonNull(obj) && AbstractEntity.class.isAssignableFrom(obj.getClass())) {
                final AbstractEntity argument = (AbstractEntity) obj;
                argument.onCreate();
            }
            return obj;
        };
    }

    private String timestamp() {
        try {
            return Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}