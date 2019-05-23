package eu.arrowhead.core.gatekeeper.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class GKSwaggerController {

	/*
	 * Necessary controller due to Swagger UI default path is hard coded and can't be configured. 
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public String redirectDefaultSwaggerUI() {
		return "redirect:/swagger-ui.html";
	}
	
}
