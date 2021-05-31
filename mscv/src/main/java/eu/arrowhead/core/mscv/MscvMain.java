package eu.arrowhead.core.mscv;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.RefreshableRepositoryImpl;
import eu.arrowhead.core.mscv.handlers.DatabaseKeyVerifier;
import eu.arrowhead.core.mscv.handlers.ExecutionHandler;
import eu.arrowhead.core.mscv.handlers.ExecutionHandlerFactory;
import eu.arrowhead.core.mscv.security.MscvKeyPairProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.SshClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "eu.arrowhead.common.quartz.uricrawler.*")
})
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
public class MscvMain {

    public static final String MSCV_EXECUTOR_SERVICE = "mscv-executor-service";
    public static final String MSCV_THREAD_FACTORY_FORMAT = "mscv-%02d";

    private final Logger logger = LogManager.getLogger();
    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static void main(final String[] args) {
        SpringApplication.run(MscvMain.class, args);
    }

    @Bean
    @Autowired
    public ExecutionHandlerFactory executionHandlerFactory(final List<ExecutionHandler<? extends Target>> handlers) {
        final ExecutionHandlerFactory factory = new ExecutionHandlerFactory();
        for (ExecutionHandler<? extends Target> handler : handlers) {
            factory.register(handler.getType(), handler);
        }
        return factory;
    }

    @Bean(name = MSCV_EXECUTOR_SERVICE, destroyMethod = "shutdownNow")
    public ScheduledExecutorService scheduledExecutorService() {
        final int poolSize = Runtime.getRuntime().availableProcessors();
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true)
                                                                      .setNameFormat(MSCV_THREAD_FACTORY_FORMAT)
                                                                      .build();
        return Executors.newScheduledThreadPool(poolSize, threadFactory);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SshClient sshClient(final DatabaseKeyVerifier keyVerifier,
                               final MscvKeyPairProvider keyPairProvider,
                               @Qualifier(MSCV_EXECUTOR_SERVICE) final ScheduledExecutorService executorService) {
        final SshClient sshClient = SshClient.setUpDefaultClient();
        // add any special configuration
        sshClient.setServerKeyVerifier(keyVerifier);
        sshClient.setKeyIdentityProvider(keyPairProvider);
        sshClient.setScheduledExecutorService(executorService);
        logger.info("Created MINA SshClient: {}", sshClient.getVersion());
        return sshClient;
    }
}
