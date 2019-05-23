package eu.arrowhead.core.eventhandler.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.common.swagger.DefaultSwaggerConfig;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class EHSwaggerConfig extends DefaultSwaggerConfig{

	public EHSwaggerConfig() {
		super("Event Handler");
	}
	
	@Bean
	public Docket customizeSwagger() {
		return configureSwaggerForCoreSystem(this.getClass().getPackageName());
	}
	
}
