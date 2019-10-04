package eu.arrowhead.core.choreographer;

import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChoreographerDBServiceTestContext {

    @Bean
    @Primary // This bean is primary only in test context
    public ChoreographerDBService mockChoreographerDBService() {
        return Mockito.mock(ChoreographerDBService.class);
    }

}
