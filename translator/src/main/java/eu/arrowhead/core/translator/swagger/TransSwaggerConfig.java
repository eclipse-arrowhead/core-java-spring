package eu.arrowhead.core.translator.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.swagger.DefaultSwaggerConfig;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class TransSwaggerConfig extends DefaultSwaggerConfig {

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public TransSwaggerConfig() {
        super(CommonConstants.CORE_SYSTEM_TRANSLATOR);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    public Docket customizeSwagger() {
        return configureSwaggerForCoreSystem(this.getClass().getPackageName());
    }
}
