package eu.arrowhead.core.gams.service;

import eu.arrowhead.core.gams.database.entities.AbstractSensorData;
import eu.arrowhead.core.gams.database.entities.GamsInstance;
import eu.arrowhead.core.gams.database.entities.Sensor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class MapeKService {

    private final ScheduledExecutorService executorService;
    private final InstanceService instanceService;
    private final SensorService sensorService;

    public MapeKService(final InstanceService instanceService, final SensorService sensorService) {
        this.instanceService = instanceService;
        this.sensorService = sensorService;
        this.executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void monitor(final GamsInstance instance, final Sensor sensor, final ZonedDateTime timestamp, final Object data) {
        final AbstractSensorData sensorData = sensorService.store(sensor, timestamp, data);
    }

    public void analyze(final AbstractSensorData sensorData) {}

    public void plan(final AbstractSensorData sensorData) {}

    public void execute(final AbstractSensorData sensorData) {}
}
