package eu.arrowhead.core.deviceregistry.swagger;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.swagger.DefaultSwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SRSwaggerConfig extends DefaultSwaggerConfig {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SRSwaggerConfig() {
		super(CommonConstants.CORE_SYSTEM_DEVICE_REGISTRY);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	public Docket customizeSwagger() {
		return configureSwaggerForCoreSystem(this.getClass().getPackageName());
	}
}