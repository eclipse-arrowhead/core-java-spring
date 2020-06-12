package eu.arrowhead.core.gams;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.gams.database.repositories.DoubleSensorDataRepository;
import eu.arrowhead.core.gams.database.repositories.GamsInstanceRepository;
import eu.arrowhead.core.gams.database.repositories.LongSensorDataRepository;
import eu.arrowhead.core.gams.database.repositories.SensorDataRepository;
import eu.arrowhead.core.gams.database.repositories.SensorRepository;
import eu.arrowhead.core.gams.database.repositories.StringSensorDataRepository;
import org.mockito.Mockito;
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
    public DoubleSensorDataRepository doubleAbstractSensorDataRepository() {
        return Mockito.mock(DoubleSensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public GamsInstanceRepository gamsInstanceRepository() {
        return Mockito.mock(GamsInstanceRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public LongSensorDataRepository longSensorDataRepository() {
        return Mockito.mock(LongSensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public SensorDataRepository sensorDataRepository() {
        return Mockito.mock(SensorDataRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public SensorRepository sensorRepository() {
        return Mockito.mock(SensorRepository.class);
    }
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public StringSensorDataRepository stringSensorDataRepository() {
        return Mockito.mock(StringSensorDataRepository.class);
    }

}