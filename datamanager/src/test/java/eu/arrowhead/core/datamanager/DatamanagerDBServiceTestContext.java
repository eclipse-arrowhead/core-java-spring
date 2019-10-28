package eu.arrowhead.core.datamanager;

import eu.arrowhead.core.datamanager.database.service.DatamanagerDBService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatamanagerDBServiceTestContext {

    @Bean
    @Primary // This bean is primary only in test context
    public DatamanagerDBService mockDatamanagerDBService() {
        return Mockito.mock(DatamanagerDBService.class);
    }

}
