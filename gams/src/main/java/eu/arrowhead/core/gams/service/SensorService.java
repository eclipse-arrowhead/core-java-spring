package eu.arrowhead.core.gams.service;

import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceDto;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(SensorService.class);

    @Autowired
    public SensorService() {
    }

    public GamsInstanceDto create(final CreateSensorRequest createSensorRequest) {
        return null;
    }

    public GamsInstanceDto publish(final PublishSensorDataRequest publishSensorDataRequest) {
        logger.debug("publish({})", publishSensorDataRequest);
        logger.info(publishSensorDataRequest.getData().getClass());
        return null;
    }

    //=================================================================================================
    // methods
}
