package eu.arrowhead.core.serviceregistry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceRegistryController {

	@Value("${ping_timeout}")
	private long pingTimeout;
	
	@RequestMapping(method = { RequestMethod.GET }, name = "/")
	public String echoTimeout() {
		System.out.println(pingTimeout);
		return String.valueOf(pingTimeout);
	}
}