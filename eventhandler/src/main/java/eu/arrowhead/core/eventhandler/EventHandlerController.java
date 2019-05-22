package eu.arrowhead.core.eventhandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventHandlerController {

	@Value("${check_interval}")
	private long checkInterval;
	
	@RequestMapping(method = { RequestMethod.GET }, name = "/")
	public String echoTimeout() {
		System.out.println(checkInterval);
		return String.valueOf(checkInterval);
	}
}