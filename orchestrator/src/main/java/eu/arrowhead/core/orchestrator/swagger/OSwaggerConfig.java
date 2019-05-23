package eu.arrowhead.core.orchestrator.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.common.swagger.DefaultSwaggerConfig;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class OSwaggerConfig extends DefaultSwaggerConfig{

	public OSwaggerConfig() {
		super("Orchestrator");
		}
	
	@Bean
	public Docket customizeSwagger() {
		return configureSwaggerForCoreSystem(this.getClass().getPackageName());
	}
	
}
