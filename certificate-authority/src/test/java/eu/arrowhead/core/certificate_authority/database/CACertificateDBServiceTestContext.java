package eu.arrowhead.core.certificate_authority.database;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CACertificateDBServiceTestContext {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public CACertificateDBService mockCACertificateDBService() {
        return Mockito.mock(CACertificateDBService.class);
    }
}
