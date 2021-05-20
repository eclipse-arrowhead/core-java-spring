package eu.arrowhead.core.gams.service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import eu.arrowhead.common.database.entity.Event;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.database.entity.TimeoutGuard;
import eu.arrowhead.common.database.repository.TimeoutRepository;
import eu.arrowhead.core.gams.DataValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeoutService {

    private final DataValidation validation = new DataValidation();
    private final Map<Sensor, ScheduledFuture<?>> map = new HashMap<>();

    private final EventService eventService;
    private final TimeoutRepository timeoutRepository;
    private final ScheduledExecutorService executorService;

    @Autowired
    public TimeoutService(final EventService eventService,
                          final TimeoutRepository timeoutRepository,
                          final ScheduledExecutorService executorService) {
        super();
        this.eventService = eventService;
        this.timeoutRepository = timeoutRepository;
        this.executorService = executorService;
    }

    @PostConstruct
    protected void setupTimeoutEvents() {
        for (TimeoutGuard t : timeoutRepository.findAll()) {
            scheduleTimeoutEvent(t);
        }
    }

    public void createTimeoutGuard(final Sensor sensor, final Long timeValue, final ChronoUnit timeUnit) {
        validation.verify(sensor);

        final TimeoutGuard guard = new TimeoutGuard(sensor, timeValue, timeUnit);
        validation.verify(guard);

        timeoutRepository.saveAndFlush(guard);
        scheduleTimeoutEvent(guard);
    }

    public void deleteTimeoutAnalysis(final TimeoutGuard guard) {
        validation.verify(guard);
        final ScheduledFuture<?> future = map.get(guard.getSensor());

        if (Objects.nonNull(future)) {
            future.cancel(false);
        }

        timeoutRepository.delete(guard);
    }

    public void rescheduleTimeoutEvent(final Event event) {
        validation.verify(event);

        final Sensor sensor = event.getSensor();
        final ScheduledFuture<?> future = map.get(sensor);

        if (Objects.nonNull(future)) {
            future.cancel(false);
            final Optional<TimeoutGuard> optionalTimeoutGuard = timeoutRepository.findBySensor(sensor);
            final TimeoutGuard timeoutGuard = optionalTimeoutGuard.orElseThrow();
            scheduleTimeoutEvent(timeoutGuard);
        }
    }

    private void scheduleTimeoutEvent(final TimeoutGuard guard) {
        validation.verify(guard);

        final TimeoutTask timeoutTask = new TimeoutTask(guard);
        schedule(timeoutTask, guard.getTimeValue(), guard.getTimeUnit());
    }

    private void schedule(final TimeoutTask task, final long delay, final ChronoUnit chronoUnit) {
        final TimeUnit timeUnit = TimeUnit.of(chronoUnit);
        final ScheduledFuture<?> schedule = executorService.schedule(task, delay, timeUnit);
        map.put(task.getSensor(), schedule);
    }

    private class TimeoutTask implements Runnable {

        private final TimeoutGuard guard;

        public TimeoutTask(final TimeoutGuard guard) {
            this.guard = guard;
        }

        @Override
        public void run() {
            eventService.createTimeoutEvent(guard);
            schedule(this, guard.getTimeValue(), guard.getTimeUnit());
        }

        private Sensor getSensor() {
            return guard.getSensor();
        }
    }
}
