package eu.arrowhead.core.authorization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {

	private Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@GetMapping(path = "/")
	public String deleteThisService() {
		logger.debug("deleteThisService() is called.");
		return "Delete this service.";
	}
}
