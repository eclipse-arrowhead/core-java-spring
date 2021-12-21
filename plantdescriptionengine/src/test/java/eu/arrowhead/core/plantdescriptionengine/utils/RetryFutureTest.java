package eu.arrowhead.core.plantdescriptionengine.utils;

import static org.junit.Assert.fail;
import org.junit.Test;
import eu.arrowhead.core.plantdescriptionengine.utils.RetryFuture.RetryTask;
import se.arkalix.util.concurrent.Future;

public class RetryFutureTest {

    @Test
    public void shouldRetry() {
        
        final RetryFuture retry = new RetryFuture(0, 3, "failed");
        final FailNTimes failTwice = new FailNTimes(2);
        retry.run(failTwice)
            .onFailure(e -> fail());
    }

    @Test
    public void shouldFailAfterMaxRetries() {
        final RetryFuture retry = new RetryFuture(0, 3, "failed");
        final FailNTimes failFourTimes = new FailNTimes(4);
        retry.run(failFourTimes)
            .ifSuccess(result -> fail());
    }

    @Test
    public void shouldNotRequireErrorMessage() {
        final RetryFuture retry = new RetryFuture(0, 3);
        final FailNTimes failFourTimes = new FailNTimes(4);
        retry.run(failFourTimes)
            .ifSuccess(result -> fail());
    }

    class FailNTimes implements RetryTask<Void> {
        private int n;

        FailNTimes(int n) {
            this.n = n;
        }

        @Override
        public Future<Void> run() {
            if (n > 0) {
                n--;
                return Future.failure(new RuntimeException("Fail"));
            } else {
                return Future.success(null);
            }
        }
    }
}
