package eu.arrowhead.core.orchestrator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrchestratorController {
	
	@Value("${sr_address}")
	private String srAddress;
	
	@RequestMapping(method = { RequestMethod.GET }, name = "/")
	public String echoTimeout() {
		System.out.println(srAddress);
		return srAddress;
	}
	

}
