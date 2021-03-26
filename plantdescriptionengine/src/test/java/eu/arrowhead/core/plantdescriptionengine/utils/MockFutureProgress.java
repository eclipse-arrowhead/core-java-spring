package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.FutureProgress;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Mock MockFutureProgress implementation used for testing.
 */
public class MockFutureProgress<R> implements FutureProgress<R> {

    private final R value;

    MockFutureProgress(final R value) {
        this.value = value;
    }

    @Override
    public void onResult(final Consumer<Result<R>> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer.");
        consumer.accept(Result.success(value));
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<R> addProgressListener(final Listener listener) {
        throw new UnsupportedOperationException();
    }

}