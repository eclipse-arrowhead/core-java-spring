package eu.arrowhead.common.swagger;

import java.util.Collections;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Controller
public class DefaultSwaggerConfig {

	private final String coreSystemName;

	public DefaultSwaggerConfig(String coreSystemName) {
		this.coreSystemName = coreSystemName;
	}
	
	/*
	 * Necessary controller due to Swagger UI default path is hard coded and can't be configured. 
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public String redirectDefaultSwaggerUI() {
		return "redirect:/swagger-ui.html";
	}
	
	public Docket configureSwaggerForCoreSystem(String swaggerPackage) {
		return new Docket(DocumentationType.SWAGGER_2)  
		          .select()                                  
		          .apis(RequestHandlerSelectors.any())
		          .apis(Predicates.not(RequestHandlerSelectors.basePackage(swaggerPackage)))
		          .paths(PathSelectors.any())
		          .paths(Predicates.not(PathSelectors.regex("/error")))
		          .build()
		          .useDefaultResponseMessages(false)
		          .apiInfo(apiInfo());
	}
	
	private ApiInfo apiInfo() {
		return new ApiInfo(
				"Arrowhead " + coreSystemName + " Core System API",
				"This page shows the REST interfaces offered by the " + coreSystemName + " Core System.",
				"",
				"",
				new Contact("Arrowhead Consortia", "https://github.com/arrowhead-f", ""),
				"",
				"",
				Collections.emptyList());
	}
}
