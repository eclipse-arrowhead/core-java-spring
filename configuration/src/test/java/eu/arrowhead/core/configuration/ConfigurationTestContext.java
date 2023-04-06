package eu.arrowhead.core.configuration;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.service.CommonDBService;
//import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.configuration.database.service.ConfigurationDBService;

@Configuration
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*database.*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ConfigurationMain.class),
        })
@SpringBootApplication()
public class ConfigurationTestContext {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public ConfigurationDBService mockConfigurationDBService() {
        return Mockito.mock(ConfigurationDBService.class);
    }

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public CommonDBService mockCommonDBService() {
		return Mockito.mock(CommonDBService.class);
	}
}
