package eu.arrowhead.core.gams.service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.gams.database.entities.AbstractSensorData;
import eu.arrowhead.core.gams.database.entities.DoubleSensorData;
import eu.arrowhead.core.gams.database.entities.GamsInstance;
import eu.arrowhead.core.gams.database.entities.LongSensorData;
import eu.arrowhead.core.gams.database.entities.Sensor;
import eu.arrowhead.core.gams.database.entities.StringSensorData;
import eu.arrowhead.core.gams.database.repositories.SensorDataRepository;
import eu.arrowhead.core.gams.database.repositories.SensorRepository;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import eu.arrowhead.core.gams.rest.dto.SensorDataDto;
import eu.arrowhead.core.gams.rest.dto.SensorDto;
import eu.arrowhead.core.gams.utility.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class SensorService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(SensorService.class);
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;

    @Autowired
    public SensorService(SensorRepository sensorRepository, SensorDataRepository sensorDataRepository) {
        this.sensorRepository = sensorRepository;
        this.sensorDataRepository = sensorDataRepository;
    }

    public Sensor create(final GamsInstance instance, final CreateSensorRequest request) {
        logger.debug("publish({})", request);
        Assert.notNull(request, "CreateSensorRequest must not be null");
        Assert.notNull(request.getName(), "Sensor name must not be null");
        Assert.notNull(request.getType(), "Sensor type must not be null");

        Sensor sensor = new Sensor(instance, request.getName(), request.getType());
        return sensorRepository.saveAndFlush(sensor);
    }

    public AbstractSensorData store(final Sensor sensor, final ZonedDateTime timestamp, final Object data) {
        logger.debug("publish({},{},{})", sensor, timestamp, data);
        Assert.notNull(sensor, "Sensor must not be null");
        Assert.notNull(timestamp, "Timestamp must not be null");
        Assert.notNull(data, "Sensor data must not be null");

        AbstractSensorData sensorData;

        switch (sensor.getType()) {
            case EVENT:
                sensorData = new StringSensorData(sensor, timestamp, (String) data);
                break;
            case FLOATING_POINT_NUMBER:
                final long doubleNumber = MathHelper.convert(data);
                sensorData = new DoubleSensorData(sensor, timestamp, doubleNumber / MathHelper.PRECISION);
                break;
            case INTEGER_NUMBER:
            default:
                final long longNumber = MathHelper.convert(data);
                sensorData = new LongSensorData(sensor, timestamp, longNumber);
                break;
        }

        return sensorDataRepository.saveAndFlush(sensorData);
    }

    public Sensor findByUid(String uid) {
        logger.debug("findByUid({})", uid);
        Assert.notNull(uid, "Sensor uid must not be null");

        final Optional<Sensor> instanceByUid = sensorRepository.findByUid(UUID.fromString(uid));
        return instanceByUid.orElseThrow(() -> new DataNotFoundException("Unable to find sensor with given uid"));
    }

    //=================================================================================================
    // methods
}
