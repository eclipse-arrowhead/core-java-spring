package eu.arrowhead.core.gateway;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.GATEWAY_URI)
public class GatewayController {
	
	//=================================================================================================
	// members
	private static final String ACTIVE_SESSIONS_MGMT_URI = CommonConstants.MGMT_URI + "/sessions";
	
	private static final String GET_ACTIVE_SESSIONS_HTTP_200_MESSAGE = "Sessions returned";
	private static final String GET_ACTIVE_SESSIONS_HTTP_400_MESSAGE = "Could not return sessions";
	
	private static final String POST_CONNECT_HTTP_201_MESSAGE = "Connection created";
	private static final String POST_CONNECT_HTTP_400_MESSAGE = "Could not create connection";
	
	private static final String GET_PUBLIC_KEY_200_MESSAGE = "Public key returned";
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the public key of the Gateway core service as a Base64 encoded text", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_PUBLIC_KEY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_GATEWAY_KEY_URI)
	public String getPublicKey() {
		return acquireAndConvertPublicKey();
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return active Gateway sessions", response = Object.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = GET_ACTIVE_SESSIONS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ACTIVE_SESSIONS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ACTIVE_SESSIONS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public Object getActiveSessions() {
		
		//TODO: implement
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates a Socket and Message queue between the given Relay and Provider and return the necesarry connection informations", response = GatewayProviderConnectionResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_GATEWAY_CONNECT_PROVIDER_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public GatewayProviderConnectionResponseDTO connectProvider(@RequestBody final GatewayProviderConnectionRequestDTO request) {
		
		//TODO: implement
		
		return new GatewayProviderConnectionResponseDTO();
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates a ServerSocket between the given Relay and Consumer and return the ServerSocket port", response = Integer.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_GATEWAY_CONNECT_CONSUMER_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public Integer connectConsumer(@RequestBody final GatewayConsumerConnectionRequestDTO request) {
		
		//TODO: implement
		
		return -1;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private String acquireAndConvertPublicKey() {
		final String origin = CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_KEY_URI;
		
		if (!secure) {
			throw new ArrowheadException("Gateway core service runs in insecure mode.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Public key is not available.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}
}