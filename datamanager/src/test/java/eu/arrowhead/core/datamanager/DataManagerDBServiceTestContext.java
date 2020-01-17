package eu.arrowhead.core.datamanager;

import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataManagerDBServiceTestContext {

    @Bean
    @Primary // This bean is primary only in test context
    public DataManagerDBService mockDatamanagerDBService() {
        return Mockito.mock(DataManagerDBService.class);
    }

}
