package eu.arrowhead.core.gams.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.AbstractSensorData;
import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.TimeoutGuard;
import eu.arrowhead.common.database.repository.EventRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gams.DataValidation;
import eu.arrowhead.core.gams.GamsProperties;
import eu.arrowhead.core.gams.dto.AbstractActionWrapper;
import eu.arrowhead.core.gams.dto.EventType;
import eu.arrowhead.core.gams.dto.GamsPhase;
import eu.arrowhead.core.gams.dto.ProcessingState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class EventService {

    private static final String DELIMITER = "-";
    private final Logger logger = LogManager.getLogger();
    private final EventRepository repository;
    private final ScheduledExecutorService executorService;
    private final GamsProperties properties;
    private final DataValidation validation;

    @Autowired
    public EventService(final GamsProperties properties,
                        final ScheduledExecutorService executorService,
                        final EventRepository repository) {
        super();
        this.properties = properties;
        this.executorService = executorService;
        this.repository = repository;

        validation = new DataValidation();
    }

    @PostConstruct
    public void setup() {
        final GamsProperties.TimeProperties expiration = properties.getEventExpiration();
        executorService.scheduleAtFixedRate(this::expireEvents, 0, expiration.getValue(), TimeUnit.of(expiration.getUnit()));
    }

    public long countValidEvents() {
        return repository.countValid();
    }

    @Transactional
    public void createMonitorEventWithDelay(final Sensor sensor, final AbstractSensorData<?> data) {
        validation.verify(sensor);
        final GamsInstance instance = sensor.getInstance();

        // only create the event if there is no event ready for processing
        if (!(instance.getDelay() > 0 && repository.hasValidEvent(sensor, ProcessingState.PERSISTED, GamsPhase.MONITOR, EventType.SENSOR_DATA))) {
            createEvent(sensor, data, GamsPhase.MONITOR, EventType.SENSOR_DATA, instance.getDelay(), instance.getDelayTimeUnit());
        } else {
            logger.debug("A Monitor event is already scheduled to examine new sensor data... skipping");
        }
    }

    @Transactional
    public void createMonitorEvent(final Sensor sensor, final AbstractSensorData<?> data) {
        createEvent(sensor, data, GamsPhase.MONITOR, EventType.SENSOR_DATA, 0L, ChronoUnit.SECONDS);
    }

    @Transactional
    public void createAnalyseEvent(final Sensor sensor, final AbstractSensorData<?> data) {
        createEvent(sensor, data, GamsPhase.ANALYZE, EventType.ANALYSIS, 0L, ChronoUnit.SECONDS);
    }

    @Transactional
    public void createPlanEvent(final Sensor sensor, final AbstractSensorData<?> data) {
        createEvent(sensor, data, GamsPhase.PLAN, EventType.METRIC, 0L, ChronoUnit.SECONDS);
    }

    @Transactional
    public void createExecuteEvent(final Sensor sensor, final AbstractSensorData<?> data) {
        createEvent(sensor, data, GamsPhase.EXECUTE, EventType.PLAN, 0L, ChronoUnit.SECONDS);
    }

    @Transactional
    public void createTimeoutEvent(final TimeoutGuard analysis) {
        validation.verify(analysis);

        final Sensor sensor = analysis.getSensor();

        final Event event = new Event(sensor, GamsPhase.PLAN, EventType.TIMEOUT, 0, ChronoUnit.SECONDS);
        event.setCreatedAt(ZonedDateTime.now());
        event.setSource(analysis.getClass().getSimpleName() + DELIMITER + analysis.getId());
        event.setData(String.valueOf(analysis.getCreatedAt()));

        repository.saveAndFlush(event);
    }

    @Transactional
    public void createFailureEvent(final Event source, final String message) {
        logger.warn(source.getMarker(), "Creating new Failure Event for {}", source);
        validation.verify(source);
        Assert.hasText(message, "Error message " + DataValidation.NOT_EMPTY);

        source.setState(ProcessingState.FAILED);
        repository.saveAndFlush(source);

        final Event event = new Event(source.getSensor(), GamsPhase.FAILURE, EventType.FAILURE, 0, ChronoUnit.SECONDS);
        event.setCreatedAt(source.getCreatedAt());
        event.setSource(source.getSource());
        event.setData(message);

        repository.saveAndFlush(event);
    }

    @Transactional
    public void createFailureEvent(final Event source, final Exception ex) {
        validation.verify(source);
        Assert.notNull(ex, "Exception " + DataValidation.NOT_NULL);

        createFailureEvent(source, ex.getClass().getSimpleName() + DELIMITER + ex.getMessage());
    }

    @Transactional
    public void createFailureEvent(final AbstractActionWrapper wrapper, final Exception ex) {
        Assert.notNull(wrapper, "AbstractActionWrapper " + DataValidation.NOT_NULL);
        Assert.notNull(ex, "Exception " + DataValidation.NOT_NULL);
        final String message = "ActionExecution Failed: " + ex.getMessage();
        createFailureEvent(wrapper.getSourceEvent(), message.substring(0, 64));
    }

    @Transactional
    public boolean hasLoad() {
        return repository.countValid() >= properties.getQueueSize();
    }

    @Transactional
    public Iterable<Event> loadEvent(final int limit) {
        return repository.findValidEvent(ProcessingState.PERSISTED, ProcessingState.IN_QUEUE, limit);
    }

    @Transactional
    public void persisted(final Event event) {
        event.setState(ProcessingState.PERSISTED);
        repository.saveAndFlush(event);
        logger.debug("Persisted {}", event::shortToString);
    }

    @Transactional
    public void processing(final Event event) {
        event.setState(ProcessingState.PROCESSING);
        repository.saveAndFlush(event);
        logger.debug("Processing {}", event::shortToString);
    }

    @Transactional
    public void processed(final Event event) {
        event.setState(ProcessingState.PROCESSED);
        repository.saveAndFlush(event);
        logger.debug("Processed {}", event::shortToString);
    }

    @Transactional
    public void expireEvents() {
        repository.expireEvents();
    }

    private void createEvent(final Sensor sensor,
                             final AbstractSensorData<?> data,
                             final GamsPhase phase,
                             final EventType type,
                             final Long delay,
                             final ChronoUnit unit) {
        logger.debug("Creating new Event for {}", data::shortToString);

        validation.verify(sensor);
        validation.verify(data);
        try {
            final Event event = new Event(sensor, phase, type, delay, unit);
            event.setCreatedAt(data.getCreatedAt());
            event.setSource(data.getClass().getSimpleName() + DELIMITER + data.getId());
            event.setData(String.valueOf(data.getData()));

            repository.saveAndFlush(event);
            logger.info("Persisted new {} which will be valid from '{}'", event.shortToString(), event.getValidFrom());
        } catch (Exception e) {
            throw new ArrowheadException(e.getMessage());
        }
    }
}
