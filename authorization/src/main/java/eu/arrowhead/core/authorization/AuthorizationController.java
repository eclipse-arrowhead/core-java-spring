package eu.arrowhead.core.authorization;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.http.HttpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class AuthorizationController {
	
	private static final String ECHO_URI = "/echo";
	private static final String DELETE_SERVICE_URI = "/delete";
	
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Value(CommonConstants.$SERVICE_REGISTRY_ADDRESS_WD)
	private String srAddress;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PORT_WD)
	private int srPort;
	
	@Autowired
	private HttpService httpService;
	
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	@GetMapping(path = DELETE_SERVICE_URI)
	public String deleteThisService() {
		logger.debug("deleteThisService() is called.");
	
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, srAddress, srPort, "serviceregistry/echo");
		final ResponseEntity<String> responseEntity = httpService.sendRequest(uri, HttpMethod.GET, String.class, null, null);
		return "Delete this service. " + responseEntity.getBody();
	}
}