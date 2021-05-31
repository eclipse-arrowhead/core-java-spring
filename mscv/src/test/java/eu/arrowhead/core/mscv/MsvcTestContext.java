package eu.arrowhead.core.mscv;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.repository.mscv.MipCategoryRepository;
import eu.arrowhead.common.database.repository.mscv.MipDomainRepository;
import eu.arrowhead.common.database.repository.mscv.MipRepository;
import eu.arrowhead.common.database.repository.mscv.ScriptRepository;
import eu.arrowhead.common.database.repository.mscv.SshTargetRepository;
import eu.arrowhead.common.database.repository.mscv.StandardRepository;
import eu.arrowhead.common.database.repository.mscv.TargetRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryListRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationEntryRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionDetailRepository;
import eu.arrowhead.common.database.repository.mscv.VerificationExecutionRepository;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.mscv.handlers.DatabaseKeyVerifier;
import eu.arrowhead.core.mscv.handlers.ExecutionHandler;
import eu.arrowhead.core.mscv.handlers.ExecutionHandlerFactory;
import eu.arrowhead.core.mscv.security.MscvKeyPairProvider;
import org.apache.sshd.client.SshClient;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*database.*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MscvMain.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MscvApplicationInitListener.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = HttpService.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DriverUtilities.class),
        })
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class MsvcTestContext {

    @Bean
    @Autowired
    public ExecutionHandlerFactory executionHandlerFactory(final List<ExecutionHandler<? extends Target>> handlers) {
        final ExecutionHandlerFactory factory = new ExecutionHandlerFactory();
        for (ExecutionHandler<? extends Target> handler : handlers) {
            factory.register(handler.getType(), handler);
        }
        return factory;
    }

    @Bean(name = MscvMain.MSCV_EXECUTOR_SERVICE, destroyMethod = "shutdownNow")
    public ScheduledExecutorService scheduledExecutorService() {
        return Mockito.mock(ScheduledExecutorService.class);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SshClient sshClient(final DatabaseKeyVerifier keyVerifier,
                               final MscvKeyPairProvider keyPairProvider,
                               @Qualifier(MscvMain.MSCV_EXECUTOR_SERVICE) final ScheduledExecutorService executorService) {
        return Mockito.mock(SshClient.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public CommonDBService mockCommonDBService() {
        return Mockito.mock(CommonDBService.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public MipCategoryRepository mipCategoryRepository() {
        return Mockito.mock(MipCategoryRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public MipDomainRepository mipDomainRepository() {
        return Mockito.mock(MipDomainRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public MipRepository mipRepository() {
        return Mockito.mock(MipRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public ScriptRepository scriptRepository() {
        return Mockito.mock(ScriptRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public SshTargetRepository sshTargetRepository() {
        return Mockito.mock(SshTargetRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public StandardRepository standardRepository() {
        return Mockito.mock(StandardRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public TargetRepository targetRepository() {
        return Mockito.mock(TargetRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public VerificationEntryListRepository verificationEntryListRepository() {
        return Mockito.mock(VerificationEntryListRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public VerificationEntryRepository verificationEntryRepository() {
        return Mockito.mock(VerificationEntryRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public VerificationExecutionDetailRepository verificationExecutionDetailRepository() {
        return Mockito.mock(VerificationExecutionDetailRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public VerificationExecutionRepository verificationExecutionRepository() {
        return Mockito.mock(VerificationExecutionRepository.class);
    }

    @Bean
    @Primary // This bean is primary only in test context
    public HttpService httpService() {
        return Mockito.mock(HttpService.class);
    }
    @Bean
    @Primary // This bean is primary only in test context
    public DriverUtilities driverUtilities() {
        return Mockito.mock(DriverUtilities.class);
    }


}