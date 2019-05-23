package eu.arrowhead.common.swagger;

import java.util.Collections;

import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class DefaultSwaggerConfig {
	
	public static Docket createSwaggerForCoreSystem(String coreSystemName, String swaggerPackage) { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.any())
          .apis(Predicates.not(RequestHandlerSelectors.basePackage(swaggerPackage)))
          .paths(PathSelectors.any())
          .paths(Predicates.not(PathSelectors.regex("/error")))
          .build()
          .useDefaultResponseMessages(false)
          .apiInfo(apiInfo(coreSystemName));
	}
    
    private static ApiInfo apiInfo(String coreSystemName) {
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
