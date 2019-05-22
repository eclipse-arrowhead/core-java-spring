package eu.arrowhead.core.serviceregistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceRegistryController {

	@Value("${ping_timeout}")
	private long pingTimeout;
	
	private Logger logger = LogManager.getLogger(ServiceRegistryController.class);
	
	@RequestMapping(method = { RequestMethod.GET }, name = "/")
	public String echoTimeout() {
		logger.debug("echoTimeout() is called.");
        return String.valueOf(pingTimeout);
	}
}