package eu.arrowhead.core.gams.service;

import java.time.temporal.ChronoUnit;

import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.TimeoutGuard;
import eu.arrowhead.common.database.repository.EventRepository;
import eu.arrowhead.core.gams.DatabaseTestContext;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.SensorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DatabaseTestContext.class})
public class MapeKTimeoutTest {

    private final Logger logger = LogManager.getLogger();


    @Autowired
    private InstanceService instanceService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private TimeoutService timeoutService;

    private GamsInstance instance;
    private Sensor sensor;
    private TimeoutGuard timeoutGuard;

    @Before
    public void setUp() {
        instance = instanceService.create(new CreateInstanceRequest("test", 10L));
        sensor = sensorService.create(instance, new CreateSensorRequest("temp", "localhost", SensorType.INTEGER_NUMBER, 10L, ChronoUnit.MINUTES));
    }

    @After
    @Ignore
    public void tearDown() {
        timeoutService.deleteTimeoutGuard(timeoutGuard);
        eventRepository.deleteAll();
        sensorService.deleteAllSensor(instance);
        instanceService.delete(instance);
    }

    @Test
    public void createTimeout() throws InterruptedException {
        timeoutGuard = timeoutService.createTimeoutGuard(sensor, 1L, ChronoUnit.SECONDS);
        logger.debug("Test sleep for timeout");
        Thread.sleep(1_500L);
    }
}