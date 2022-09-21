package eu.arrowhead.core.gams.mock;

import java.util.Objects;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.AbstractEntity;
import eu.arrowhead.common.database.repository.ActionPlanRepository;
import eu.arrowhead.common.database.repository.AggregationRepository;
import eu.arrowhead.common.database.repository.AnalysisRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.DoubleSensorDataRepository;
import eu.arrowhead.common.database.repository.EventRepository;
import eu.arrowhead.common.database.repository.GamsInstanceRepository;
import eu.arrowhead.common.database.repository.KnowledgeRepository;
import eu.arrowhead.common.database.repository.LogsRepository;
import eu.arrowhead.common.database.repository.LongSensorDataRepository;
import eu.arrowhead.common.database.repository.PolicyRepository;
import eu.arrowhead.common.database.repository.SensorDataRepository;
import eu.arrowhead.common.database.repository.SensorRepository;
import eu.arrowhead.common.database.repository.StringSensorDataRepository;
import eu.arrowhead.common.database.repository.TimeoutGuardRepository;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.gams.GamsApplicationInitListener;
import eu.arrowhead.core.gams.GamsMain;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;

@Configuration
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*database.*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GamsMain.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GamsApplicationInitListener.class),
        })
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class GamsTestContext {

    //=================================================================================================
    // methods


    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public CommonDBService mockCommonDBService() {
        return Mockito.mock(CommonDBService.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public CloudRepository mockCloudRepository() {
        return Mockito.mock(CloudRepository.class);
    }
    
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public LogsRepository mockLogsRepository() {
        return Mockito.mock(LogsRepository.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public DoubleSensorDataRepository doubleAbstractSensorDataRepository() {
        return mock(DoubleSensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public GamsInstanceRepository gamsInstanceRepository() {
        return mock(GamsInstanceRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public LongSensorDataRepository longSensorDataRepository() {
        return mock(LongSensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public SensorDataRepository sensorDataRepository() {
        return mock(SensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public SensorRepository sensorRepository() {
        return mock(SensorRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public EventRepository eventRepository() {
        return mock(EventRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public KnowledgeRepository knowledgeRepository() {
        return mock(KnowledgeRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public ActionPlanRepository actionPlanRepository() {
        return mock(ActionPlanRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public StringSensorDataRepository stringSensorDataRepository() {
        return mock(StringSensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public AggregationRepository aggregationRepository() {
        return mock(AggregationRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public AnalysisRepository analysisRepository() {
        return mock(AnalysisRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public PolicyRepository policyRepository() {
        return mock(PolicyRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public TimeoutGuardRepository timeoutGuardRepository() {
        return mock(TimeoutGuardRepository.class);
    }


    private <T extends JpaRepository<?, ?>> T mock(final Class<T> clz) {

        final T mock = Mockito.mock(clz);
        final Answer<?> answer = invocation -> {
            final Object obj = invocation.getArgument(0);

            if(Objects.nonNull(obj) && obj.getClass().isAssignableFrom(AbstractEntity.class)) {
                final AbstractEntity argument = (AbstractEntity) obj;
                argument.onCreate();
            }

            return obj;
        };

        Mockito.when(mock.save(Mockito.any()))
               .then(answer);

        Mockito.when(mock.saveAndFlush(Mockito.any()))
               .then(answer);

        return mock;
    }
}