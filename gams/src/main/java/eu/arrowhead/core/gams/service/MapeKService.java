package eu.arrowhead.core.gams.service;

import eu.arrowhead.core.gams.rest.dto.SensorDataDto;
import org.springframework.stereotype.Service;

@Service
public class MapeKService {

    private final InstanceService instanceService;
    private final SensorService sensorService;

    public MapeKService(final InstanceService instanceService, final SensorService sensorService) {
        this.instanceService = instanceService;
        this.sensorService = sensorService;
    }

    public void monitor(final SensorDataDto sensorDataDto) {}

    public void analyze(final SensorDataDto sensorDataDto) {}

    public void plan(final SensorDataDto sensorDataDto) {}

    public void execute(final SensorDataDto sensorDataDto) {}

}
