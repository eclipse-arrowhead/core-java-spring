package eu.arrowhead.core.authorization;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {

	@GetMapping(name = "/")
	public String deleteThisService() {
		return "Delete this service.";
	}
}
