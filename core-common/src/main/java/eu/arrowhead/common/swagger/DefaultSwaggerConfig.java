/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.swagger;

import java.util.Collections;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.common.base.Predicates;

import eu.arrowhead.common.CoreCommonConstants;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class DefaultSwaggerConfig implements WebMvcConfigurer {
	
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
		          									  .apis(Predicates.not(RequestHandlerSelectors.basePackage(CoreCommonConstants.SWAGGER_COMMON_PACKAGE)))
		          									  .paths(PathSelectors.any())
		          									  .paths(Predicates.not(PathSelectors.regex(CoreCommonConstants.SERVER_ERROR_URI)))
		          									  .build()
		          									  .tags(new Tag(CoreCommonConstants.SWAGGER_TAG_ALL, ""),
		          											new Tag(CoreCommonConstants.SWAGGER_TAG_CLIENT, ""),
		          											new Tag(CoreCommonConstants.SWAGGER_TAG_MGMT, ""),
		          											new Tag(CoreCommonConstants.SWAGGER_TAG_PRIVATE, ""))
		          									  .useDefaultResponseMessages(false)
		          									  .apiInfo(apiInfo());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    	registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/swagger/");
    	
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private ApiInfo apiInfo() {
		return new ApiInfo("Arrowhead " + coreSystemName + " Core System API",
						   "This page shows the REST interfaces offered by the " + coreSystemName + " Core System.",
						   "",
						   "",
						   new Contact("Arrowhead Consortia", "https://github.com/eclipse-arrowhead", ""),
						   "",
						   "",
						   Collections.emptyList());
	}
}