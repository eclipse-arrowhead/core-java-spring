package eu.arrowhead.core.gams;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GamsConfiguration {

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
