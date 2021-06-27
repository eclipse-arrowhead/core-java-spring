package eu.arrowhead.core.gams;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GamsConfiguration {

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService executorService() {
        final Thread.UncaughtExceptionHandler exceptionHandler = (t, ex) -> LogManager.getLogger().fatal(ex);
        final ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true)
                                                                .setNameFormat("executor-%d")
                                                                .setUncaughtExceptionHandler(exceptionHandler)
                                                                .build();
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1, factory);
    }
}
