package eu.arrowhead.core.plantdescriptionengine.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RetryFuture {

    private static final Logger logger = LoggerFactory.getLogger(RetryFuture.class);
    private final String retryMessage;
    private final int delayBetweenRetries;
    private final int maxRetries;

    public RetryFuture(
        final int retryDelayMillis,
        final int maxRetries,
        final String retryMessage
    ) {
        this.delayBetweenRetries = retryDelayMillis;
        this.maxRetries = maxRetries;
        this.retryMessage = retryMessage;
    }

    public RetryFuture(
        final int delayBetweenRetries,
        final int maxRetries
    ) {
        this(delayBetweenRetries, maxRetries, null);
    }

    private <T> Future<T> run(final RetryTask<T> task, final int remainingRetries) {
        return task.run()
            .flatMapCatch(Throwable.class, e -> {
                if (remainingRetries == 0) {
                    throw e;
                }
                if (retryMessage != null) {
                    logger.info(retryMessage);
                }
                TimeUnit.MILLISECONDS.sleep(delayBetweenRetries);
                return run(task, remainingRetries - 1);
            });
    }

    public <T> Future<T> run(final RetryTask<T> task) {
        Objects.requireNonNull(task, "Expected task");
        return run(task, maxRetries);
    }

    @FunctionalInterface
    public interface RetryTask<T> {
        Future<T> run();
    }
}
