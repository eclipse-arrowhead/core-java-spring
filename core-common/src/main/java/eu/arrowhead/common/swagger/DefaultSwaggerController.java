package eu.arrowhead.common.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.arrowhead.common.CommonConstants;

@Controller
public class DefaultSwaggerController {
	
	/*
	 * Necessary controller due to Swagger UI default path is hard coded and can't be configured. 
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public String redirectDefaultSwaggerUI() {
		return "redirect:" + CommonConstants.SWAGGER_UI_URI;
	}

}
