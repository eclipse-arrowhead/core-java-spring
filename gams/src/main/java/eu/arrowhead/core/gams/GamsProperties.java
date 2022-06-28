package eu.arrowhead.core.gams;

import java.time.temporal.ChronoUnit;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("gams")
public class GamsProperties {

    @Positive
    private Integer queueSize = 25;
    @Positive
    private Integer batchSize = 5;
    @NotNull
    private TimeProperties eventExpiration = new TimeProperties(1L, ChronoUnit.HOURS);
    @NotNull
    private WorkerProperties workers = new WorkerProperties(2, Runtime.getRuntime().availableProcessors() + 1, 1_000L, 30_000L, 2_000L);

    public GamsProperties() {
        super();
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    public TimeProperties getEventExpiration() {
        return eventExpiration;
    }

    public void setEventExpiration(final TimeProperties eventExpiration) {
        this.eventExpiration = eventExpiration;
    }

    public WorkerProperties getWorkers() {
        return workers;
    }

    public void setWorkers(final WorkerProperties workers) {
        this.workers = workers;
    }

    public static class TimeProperties {

        @PositiveOrZero
        private Long value;
        @NotNull
        private ChronoUnit unit = ChronoUnit.MINUTES;

        public TimeProperties() {
            super();
        }

        public TimeProperties(final Long value, final ChronoUnit unit) {
            this.value = value;
            this.unit = unit;
        }

        public Long getValue() {
            return value;
        }

        public void setValue(final Long value) {
            this.value = value;
        }

        public ChronoUnit getUnit() {
            return unit;
        }

        public void setUnit(final ChronoUnit unit) {
            this.unit = unit;
        }
    }

    public static class WorkerProperties {
        @Positive
        private int minimum;
        @Positive
        private int maximum;
        @Positive
        private long loopWait;
        @Positive
        private long threadWait;
        @Positive
        private long delay;

        public WorkerProperties() {
        }

        public WorkerProperties(final int minimum, final int maximum, final long loopWait, final long threadWait, final long delay) {
            this.minimum = minimum;
            this.maximum = maximum;
            this.loopWait = loopWait;
            this.threadWait = threadWait;
            this.delay = delay;
        }

        public int getMinimum() {
            return minimum;
        }

        public void setMinimum(final int minimum) {
            this.minimum = minimum;
        }

        public int getMaximum() {
            return maximum;
        }

        public void setMaximum(final int maximum) {
            this.maximum = maximum;
        }

        public long getLoopWait() {
            return loopWait;
        }

        public void setLoopWait(final long loopWait) {
            this.loopWait = loopWait;
        }

        public long getThreadWait() {
            return threadWait;
        }

        public void setThreadWait(final long threadWait) {
            this.threadWait = threadWait;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(final long delay) {
            this.delay = delay;
        }
    }
}
