package eu.arrowhead.common.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import eu.arrowhead.common.CommonConstants;

@Controller
public class DefaultSwaggerController {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	/*
	 * Necessary controller due to Swagger UI default path is hard coded and can't be configured. 
	 */
	@GetMapping(path = "/")
	public String redirectDefaultSwaggerUI() {
		return "redirect:" + CommonConstants.SWAGGER_UI_URI;
	}
}