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

    public SensorDto create(final GamsInstance instance, final CreateSensorRequest request) {
        logger.debug("publish({})", request);
        Assert.notNull(request, "CreateSensorRequest must not be null");
        Assert.notNull(request.getName(), "Sensor name must not be null");
        Assert.notNull(request.getType(), "Sensor type must not be null");

        Sensor sensor = new Sensor(instance, request.getName(), request.getType());
        sensor = sensorRepository.saveAndFlush(sensor);

        return new SensorDto(sensor.getUidAsString(), sensor.getName(), sensor.getType());
    }

    public SensorDataDto publish(final Sensor sensor, final PublishSensorDataRequest request) {
        logger.debug("publish({})", request);
        Assert.notNull(sensor, "Sensor must not be null");
        Assert.notNull(request, "PublishSensorDataRequest must not be null");
        Assert.notNull(request.getTimestamp(), "Timestamp must not be null");
        Assert.notNull(request.getData(), "Sensor data must not be null");

        final ZonedDateTime timestamp = Utilities.parseUTCStringToLocalZonedDateTime(request.getTimestamp());
        AbstractSensorData sensorData;

        switch (sensor.getType()) {
            case EVENT:
                sensorData = new StringSensorData(sensor, timestamp, (String) request.getData());
                break;
            case FLOATING_POINT_NUMBER:
                final long doubleNumber = MathHelper.convert(request.getData());
                sensorData = new DoubleSensorData(sensor, timestamp, doubleNumber / MathHelper.PRECISION);
                break;
            case INTEGER_NUMBER:
            default:
                final long longNumber = MathHelper.convert(request.getData());
                sensorData = new LongSensorData(sensor, timestamp, longNumber);
                break;
        }

        sensorData = sensorDataRepository.saveAndFlush(sensorData);
        return new SensorDataDto(sensor.getUidAsString(), request.getTimestamp(), sensorData.getData());
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
