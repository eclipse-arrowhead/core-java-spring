package eu.arrowhead.common.swagger;

import java.util.Collections;

import com.google.common.base.Predicates;

import eu.arrowhead.common.CommonConstants;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class DefaultSwaggerConfig {
	
	//=================================================================================================
	// members

	private final String coreSystemName;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public DefaultSwaggerConfig(final String coreSystemName) {
		this.coreSystemName = coreSystemName;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Docket configureSwaggerForCoreSystem(final String coreSystemSwaggerPackage) {
		return new Docket(DocumentationType.SWAGGER_2).select()                                  
		          									  .apis(RequestHandlerSelectors.any())
		          									  .apis(Predicates.not(RequestHandlerSelectors.basePackage(coreSystemSwaggerPackage)))
		          									  .apis(Predicates.not(RequestHandlerSelectors.basePackage(CommonConstants.SWAGGER_COMMON_PACKAGE)))
		          									  .paths(PathSelectors.any())
		          									  .paths(Predicates.not(PathSelectors.regex(CommonConstants.SERVER_ERROR_URI)))
		          									  .build()
		          									  .useDefaultResponseMessages(false)
		          									  .apiInfo(apiInfo());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private ApiInfo apiInfo() {
		return new ApiInfo("Arrowhead " + coreSystemName + " Core System API",
						   "This page shows the REST interfaces offered by the " + coreSystemName + " Core System.",
						   "",
						   "",
						   new Contact("Arrowhead Consortia", "https://github.com/arrowhead-f", ""),
						   "",
						   "",
						   Collections.emptyList());
	}
}