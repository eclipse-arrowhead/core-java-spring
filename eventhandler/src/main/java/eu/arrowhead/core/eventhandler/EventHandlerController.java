package eu.arrowhead.core.eventhandler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class EventHandlerController {

private static final String ECHO_URI = "/echo";
	
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = 200, message = "Core service is available"),
			@ApiResponse(code = 401, message = "You are not authorized"),
			@ApiResponse(code = 500, message = "Core service is not available")
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
}