package eu.arrowhead.core.datamanager;

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
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
import eu.arrowhead.core.datamanager.service.HistorianService;
import eu.arrowhead.core.datamanager.service.ProxyService;

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
    
    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public HistorianService mockHistorianService() {
        return Mockito.mock(HistorianService.class);
    }

    
    //-------------------------------------------------------------------------------------------------
	@Bean
    @Primary // This bean is primary only in test context
    public ProxyService mocProxyService() {
        return Mockito.mock(ProxyService.class);
    }
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public CommonDBService mockCommonDBService() {
		return Mockito.mock(CommonDBService.class);
	}
}