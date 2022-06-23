package eu.arrowhead.core.gams.service;

import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.repository.EventRepository;
import eu.arrowhead.core.gams.GamsProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private final static long ADD_WORKERS_START_DELAY = 30L;
    private final static long ADD_WORKERS_INTERVAL_DELAY = 300L;
    private final static TimeUnit ADD_WORKERS_TIME_UNIT = TimeUnit.SECONDS;

    private final static long REMOVE_WORKERS_START_DELAY = 60L;
    private final static long REMOVE_WORKERS_INTERVAL_DELAY = 60L;
    private final static TimeUnit REMOVE_WORKERS_TIME_UNIT = TimeUnit.SECONDS;


    private final Logger logger = LogManager.getLogger();

    private final MapeKService mapeK;
    private final EventService eventService;
    private final ScheduledExecutorService executorService;
    private final GamsProperties properties;

    private final Stack<ScheduledFuture<?>> workers;
    private final AtomicLong mTime;
    private final Lock lock;

    @Autowired
    public EventConsumer(final MapeKService mapeK, final EventService eventService,
                         final EventRepository repository,
                         final ScheduledExecutorService executorService,
                         final GamsProperties properties) {
        this.mapeK = mapeK;
        this.eventService = eventService;
        this.executorService = executorService;
        this.properties = properties;

        workers = new Stack<>();
        mTime = new AtomicLong(System.currentTimeMillis());
        lock = new ReentrantLock(true);
    }

    @PostConstruct
    public void setup() {
        lock.lock();
        try {
            final GamsProperties.WorkerProperties workerProperties = properties.getWorkers();

            logger.info("Initiating Event consumers and starting {} Event Workers with a delay of {}",
                        workerProperties.getMinimum(),
                        workerProperties.getDelay() / 1000);
            executorService.scheduleAtFixedRate(this::verifyLoad, ADD_WORKERS_START_DELAY, ADD_WORKERS_INTERVAL_DELAY, ADD_WORKERS_TIME_UNIT);
            executorService.scheduleAtFixedRate(this::requestRemoveWorker, REMOVE_WORKERS_START_DELAY, REMOVE_WORKERS_INTERVAL_DELAY, REMOVE_WORKERS_TIME_UNIT);

            while (this.workers.size() < workerProperties.getMinimum()) {
                this.workers.push(
                        executorService.scheduleAtFixedRate(createEventsWorker(),
                                                            workerProperties.getDelay(), workerProperties.getLoopWait(),
                                                            TimeUnit.MILLISECONDS)
                );
            }
        } finally {
            lock.unlock();
        }
    }

    private Runnable createEventsWorker() {
        return new EventWorker(properties,eventService,this);
    }

    private void verifyLoad() {
        if (eventService.hasLoad()) {
            requestNewWorker();
        }
    }

    @Transactional
    public void processEvent(final Event event) {
        try {
            // make sure that only events for different sensors run mapek concurrently to prevent concurrent processing of sensor data
            synchronized (event.getSensor().getUidAsString().intern()) {
                eventService.processing(event);
                switch (event.getPhase()) {
                    case MONITOR:
                        mapeK.monitor(event);
                        break;
                    case ANALYZE:
                        mapeK.analyze(event);
                        break;
                    case PLAN:
                        mapeK.plan(event);
                        break;
                    case EXECUTE:
                        mapeK.execute(event);
                        break;
                    case FAILURE:
                        mapeK.failure(event);
                        break;
                    default:
                        eventService.createFailureEvent(event, "Unknown EventType " + event.getPhase());
                        break;
                }
                eventService.processed(event);
            }
        } catch (final Exception e) {
            logger.error(event.getPhase().getMarker(), "{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            eventService.createFailureEvent(event, e.getClass().getSimpleName());
        }
    }

    private void requestNewWorker() {
        if (!lock.tryLock()) {
            return;
        }

        try {
            final GamsProperties.WorkerProperties workerProperties = properties.getWorkers();
            final long curTime = System.currentTimeMillis();

            if (workers.size() >= workerProperties.getMaximum()) {
                mTime.set(curTime);
                return;
            }

            logger.debug("Adding new EventWorker");
            if ((mTime.get() + workerProperties.getThreadWait()) <= curTime) {
                workers.push(
                        executorService.scheduleAtFixedRate(createEventsWorker(),
                                                            0, workerProperties.getLoopWait(),
                                                            TimeUnit.MILLISECONDS)
                );
                mTime.set(curTime);
            }
        } finally {
            lock.unlock();
        }
    }

    private void requestRemoveWorker() {
        if (!lock.tryLock()) {
            return;
        }

        try {
            final GamsProperties.WorkerProperties workerProperties = properties.getWorkers();
            final long curTime = System.currentTimeMillis();

            if (workers.size() <= workerProperties.getMinimum()) {
                mTime.set(curTime);
                return;
            }

            logger.debug("Removing unneeded EventWorker");
            if ((mTime.get() + workerProperties.getThreadWait()) <= curTime) {
                final ScheduledFuture<?> future = workers.pop();
                if (Objects.nonNull(future)) {
                    future.cancel(false);
                    mTime.set(curTime);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
