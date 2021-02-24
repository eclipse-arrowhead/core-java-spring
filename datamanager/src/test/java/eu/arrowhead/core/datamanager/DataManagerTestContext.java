package eu.arrowhead.core.datamanager;

import eu.arrowhead.common.CommonConstants;
//import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
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
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DataManagerMain.class),
        })
@SpringBootApplication()
public class DataManagerTestContext {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public DataManagerDBService mockDataManagerDBService() {
        return Mockito.mock(DataManagerDBService.class);
    }

}
