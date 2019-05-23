package eu.arrowhead.core.gatekeeper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class GatekeeperController {
	
	@Value("${sr_port}")
	private long srPort;
	
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = 200, message = "Core service is available"),
			@ApiResponse(code = 401, message = "You are not authorized"),
			@ApiResponse(code = 500, message = "Core service is not available")
	})
	@RequestMapping(method = { RequestMethod.GET }, path = "/echo")
	public String echoSRPort() {
		System.out.println(srPort);
		return String.valueOf(srPort);
	}
}
