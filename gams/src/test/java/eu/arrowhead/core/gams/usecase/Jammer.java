package eu.arrowhead.core.gams.usecase;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import eu.arrowhead.common.database.entity.ActionPlan;
import eu.arrowhead.common.database.entity.CountingAggregation;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.HttpUrlApiCall;
import eu.arrowhead.common.database.entity.LoggingAction;
import eu.arrowhead.common.database.entity.MatchPolicy;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.SetPointEvaluation;
import eu.arrowhead.common.database.repository.ActionPlanRepository;
import eu.arrowhead.common.database.repository.ActionRepository;
import eu.arrowhead.common.database.repository.AggregationRepository;
import eu.arrowhead.common.database.repository.AnalysisRepository;
import eu.arrowhead.common.database.repository.PolicyRepository;
import eu.arrowhead.core.gams.DatabaseTestContext;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.HttpMethod;
import eu.arrowhead.core.gams.rest.dto.SensorType;
import eu.arrowhead.core.gams.service.InstanceService;
import eu.arrowhead.core.gams.service.MapeKService;
import eu.arrowhead.core.gams.service.SensorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static eu.arrowhead.core.gams.dto.MatchType.GREATER_THAN;
import static eu.arrowhead.core.gams.dto.MatchType.SMALLER_THAN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DatabaseTestContext.class})
public class Jammer {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private MapeKService mapeKService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ActionPlanRepository actionPlanRepository;

    @Autowired
    private AggregationRepository aggregationRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private PolicyRepository policyRepository;


    @Before
    public void setupUseCase() {
        tearDown();

        final CreateInstanceRequest instanceRequest = new CreateInstanceRequest("jammer", 300L);
        final GamsInstance instance = instanceService.create(instanceRequest);
        logger.info("Created GamsInstance: {}", instance);

        final CreateSensorRequest sensorRequest = new CreateSensorRequest("temperature", "0.0.0.0", SensorType.FLOATING_POINT_NUMBER, 7L, ChronoUnit.DAYS);
        final Sensor tempSensor = sensorService.create(instance, sensorRequest);
        logger.info("Created Sensor: {}", tempSensor);

        final CountingAggregation aggregation = new CountingAggregation(tempSensor, 7, ChronoUnit.DAYS, 1, ChronoUnit.DAYS);
        aggregationRepository.saveAndFlush(aggregation);
        final Sensor aggrEventSensor = sensorService.createEventSensor(instance, aggregation, SensorType.INTEGER_NUMBER);

        final SetPointEvaluation setPointAnalysis = new SetPointEvaluation(aggrEventSensor, "TEMP-SETPOINT", "180", "220", false);
        analysisRepository.saveAndFlush(setPointAnalysis);
        final Sensor setPointEventSensor = sensorService.createEventSensor(instance, setPointAnalysis, SensorType.INTEGER_NUMBER);

        final MatchPolicy brokenSensor = new MatchPolicy(setPointEventSensor, "TEMP-SETPOINT", "BROKEN_SENSOR", SMALLER_THAN, 0L);
        final MatchPolicy attack = new MatchPolicy(setPointEventSensor, "TEMP-SETPOINT","JAMMER_ATTACK", GREATER_THAN, 0L);

        policyRepository.saveAndFlush(brokenSensor);
        policyRepository.saveAndFlush(attack);

        sensorService.createEventSensor(instance, brokenSensor, SensorType.EVENT);
        sensorService.createEventSensor(instance, attack, SensorType.EVENT);

        final HttpUrlApiCall httpAction = new HttpUrlApiCall(instance, "wlan-script", HttpMethod.GET, "http://localhost:8201/");
        actionRepository.saveAndFlush(httpAction);

        final ActionPlan actionPlan1 = new ActionPlan(instance, "JAMMER_ATTACK", httpAction);
        actionPlanRepository.saveAndFlush(actionPlan1);

        final LoggingAction logAction = new LoggingAction(instance, "Log BROKEN_SENSOR");
        logAction.setMarker("ACTION");
        actionRepository.saveAndFlush(logAction);

        final ActionPlan actionPlan2 = new ActionPlan(instance, "BROKEN_SENSOR", logAction);
        actionPlanRepository.saveAndFlush(actionPlan2);
    }

    @After
    public void tearDown() {
        policyRepository.deleteAll();
        analysisRepository.deleteAll();
        aggregationRepository.deleteAll();
        for(final GamsInstance instance : instanceService.getAll(Pageable.unpaged())) {
            sensorService.deleteAllSensor(instance);
            instanceService.delete(instance);
        }
    }

    @Test
    @Ignore
    public void lowSetPoint() throws InterruptedException {
        final GamsInstance instance = instanceService.findByName("jammer");
        final Sensor temperature = sensorService.findSensorByName(instance, "temperature");
        final ZonedDateTime time = ZonedDateTime.now().minusDays(1);

        for(int i = 0; i < 200; i++) {
            mapeKService.publish(temperature, time, i+1, "0.0.0.0");
        }

        Thread.sleep(15000L);
        logger.info("Sleep is over; existing unit test");
    }
}
