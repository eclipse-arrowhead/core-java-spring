package eu.arrowhead.core.gams.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.ConfigurationEntity;
import eu.arrowhead.common.database.entity.DoubleSensorData;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.LongSensorData;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.StringSensorData;
import eu.arrowhead.common.database.repository.SensorDataRepository;
import eu.arrowhead.common.database.repository.SensorRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.dto.ProcessingState;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.SensorType;
import eu.arrowhead.core.gams.utility.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class SensorService {

    public static final String SENSOR_EVENT_NAME = "Event Sensor";

    private final DataValidation validation = new DataValidation();
    private final Logger logger = LogManager.getLogger(SensorService.class);
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;

    @Autowired
    public SensorService(SensorRepository sensorRepository, SensorDataRepository sensorDataRepository) {
        this.sensorRepository = sensorRepository;
        this.sensorDataRepository = sensorDataRepository;
    }

    @Transactional
    public Sensor create(final GamsInstance instance, final CreateSensorRequest request) {
        logger.info("create({})", request);
        validation.verify(instance);
        Assert.notNull(request, "CreateSensorRequest must not be null");
        Assert.notNull(request.getName(), "Sensor name must not be null");
        Assert.notNull(request.getType(), "Sensor type must not be null");

        try {
            final Sensor sensor = new Sensor(instance, request.getName(), request.getType());
            sensor.setAddress(request.getAddress());
            sensor.setRetentionTime(request.getRetentionTime());
            sensor.setTimeUnit(request.getTimeUnit());

            return sensorRepository.saveAndFlush(sensor);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    @Transactional
    public AbstractSensorData<?> store(final Sensor sensor, final ZonedDateTime timestamp, final Object data, final String address) {
        logger.trace("store({},{},{})", sensor.shortToString(), timestamp, data);
        validation.verify(sensor);
        Assert.notNull(data, "Sensor data must not be null");

        AbstractSensorData<?> sensorData;

        switch (sensor.getType()) {
            case FLOATING_POINT_NUMBER:
                sensorData = new DoubleSensorData(sensor, timestamp, MathHelper.convertToDouble(data));
                break;
            case INTEGER_NUMBER:
                sensorData = new LongSensorData(sensor, timestamp, MathHelper.convertToLong(data));
                break;
            case EVENT: // same as default
            default:
                sensorData = new StringSensorData(sensor, timestamp, String.valueOf(data));
                break;
        }

        sensorData.setAddress(address);
        return sensorDataRepository.saveAndFlush(sensorData);
    }

    @Transactional
    public void store(final AbstractSensorData datum) {
        sensorDataRepository.saveAndFlush(datum);
    }

    public List<AbstractSensorData> load(final Sensor sensor) {
        logger.debug("load({})", sensor);
        validation.verify(sensor);
        return sensorDataRepository.findBySensorAndStateOrderByValidTillDesc(sensor, ProcessingState.PERSISTED);
    }

    public List<AbstractSensorData> load(final Sensor sensor, final int count) {
        logger.debug("load({},{})", sensor, count);
        validation.verify(sensor);
        final Page<AbstractSensorData> page = sensorDataRepository
                .findBySensorAndStateOrderByValidTillDesc(sensor, ProcessingState.PERSISTED, PageRequest.of(0, count));
        return page.getContent();
    }

    public List<AbstractSensorData> load(final Sensor sensor, final int count, final int validity, final ChronoUnit validityTimeUnit) {
        logger.debug("load({},{},{},{})", sensor, count, validity, validityTimeUnit);
        validation.verify(sensor);
        final ZonedDateTime loadFrom = ZonedDateTime.now().minus(validity, validityTimeUnit);
        final Pageable pageable = PageRequest.of(0, count);
        final Page<AbstractSensorData> page = sensorDataRepository.findBySensorAndStateAndCreatedAtAfterOrderByValidTillDesc(sensor,
                                                                                                                             ProcessingState.PERSISTED,
                                                                                                                             loadFrom,
                                                                                                                             pageable);
        return page.getContent();
    }

    public List<AbstractSensorData> load(final Sensor sensor, final int validity, final ChronoUnit validityTimeUnit) {
        logger.debug("load({},{},{})", sensor, validity, validityTimeUnit);
        final ZonedDateTime loadFrom = ZonedDateTime.now().minus(validity, validityTimeUnit);
        return sensorDataRepository.findBySensorAndStateAndCreatedAtAfterOrderByValidTillDesc(sensor, ProcessingState.PERSISTED, loadFrom);
    }

    public long count(final Sensor sensor, final ZonedDateTime validFrom) {
        logger.debug("count({})", sensor);
        validation.verify(sensor);
        return sensorDataRepository.countBySensorAndStateAndCreatedAtAfter(sensor, ProcessingState.PERSISTED, validFrom);
    }

    public Sensor findSensorByUid(final String uid) {
        logger.debug("findByUid({})", uid);
        Assert.hasText(uid, "Sensor uid must not be empty");

        try {
            final Optional<Sensor> instanceByUid = sensorRepository.findByUid(UUID.fromString(uid));
            return instanceByUid.orElseThrow(() -> new DataNotFoundException("Unable to find sensor with given uid"));
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    public Page<Sensor> findAllSensorByInstance(final GamsInstance instance, final Pageable pageRequest) {
        logger.debug("findAllByInstance({})", instance);
        validation.verify(instance);

        try {
            return sensorRepository.findAllByInstance(instance, pageRequest);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    public Sensor findSensorByName(final GamsInstance instance, final String sensorName) {
        logger.debug("findByName({},{})", instance, sensorName);
        validation.verify(instance);
        Assert.hasText(sensorName, "Sensor name must not be empty");

        try {
            final Optional<Sensor> instanceByName = sensorRepository.findByInstanceAndName(instance, sensorName);
            return instanceByName.orElseThrow(() -> new DataNotFoundException("Unable to find sensor with given name"));
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    public Sensor findSensorByAddress(final GamsInstance instance, final String address) {
        logger.debug("findByAddress({},{})", instance, address);
        validation.verify(instance);
        Assert.hasText(address, "Sensor address must not be empty");

        try {
            final Optional<Sensor> instanceByAddress = sensorRepository.findByInstanceAndAddress(instance, address);
            return instanceByAddress.orElseThrow(() -> new DataNotFoundException("Unable to find sensor with given address"));
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    @Transactional
    public void deleteAllSensor(final GamsInstance instance) {
        logger.info("deleteAll({})", instance);
        validation.verify(instance);

        try {
            final List<Sensor> sensorList = sensorRepository.findAllByInstance(instance);
            sensorRepository.deleteAll(sensorList);
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }

    @Transactional
    public Sensor createEventSensor(final GamsInstance instance, final ConfigurationEntity entity, final SensorType type) {
        Assert.notNull(entity, "Entity must not be null");
        return createEventSensor(instance, entity.getUidString(), type);
    }

    protected Sensor createEventSensor(final GamsInstance instance, final String name, final SensorType type) {
        validation.verify(instance);
        Assert.hasText(name, "Name must not be empty");
        Assert.notNull(type, "Sensor Type must not be null");
        return sensorRepository.saveAndFlush(new Sensor(instance, name, type));
    }

    protected Sensor getEventSensor(final GamsInstance instance, final String name) {
        validation.verify(instance);
        Assert.hasText(name, "Name must not be empty");
        return sensorRepository.findByInstanceAndName(instance, name)
                               .orElseThrow(exceptionSupplier(instance, name));
    }

    protected void persisted(final AbstractSensorData data) {
        data.setState(ProcessingState.PERSISTED);
        sensorDataRepository.saveAndFlush(data);
        logger.trace("persisted {}", data);
    }

    protected void processing(final AbstractSensorData data) {
        data.setState(ProcessingState.PROCESSING);
        sensorDataRepository.saveAndFlush(data);
        logger.trace("processing {}", data);
    }

    protected void processed(final AbstractSensorData data) {
        data.setState(ProcessingState.PROCESSED);
        sensorDataRepository.saveAndFlush(data);
        logger.trace("processed {}", data);
    }

    private Supplier<IllegalStateException> exceptionSupplier(final GamsInstance instance, final String name) {
        return () -> new IllegalStateException("Unable to find event sensor '" + name + "' for supplied gams instance '" + instance.getUidAsString() + "'");
    }

}
