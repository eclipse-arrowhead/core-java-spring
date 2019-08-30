package eu.arrowhead.core.gateway;

import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.service.GatewayService;
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
	
	private final Logger logger = LogManager.getLogger(GatewayController.class);
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	private ConcurrentHashMap<String,ActiveSessionDTO> activeSessions; 
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private GatewayService gatewayService;
	
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
		logger.debug("New public key GET request received...");
		
		return acquireAndConvertPublicKey();
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return active Gateway sessions", response = ActiveSessionDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ACTIVE_SESSIONS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ACTIVE_SESSIONS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ACTIVE_SESSIONS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public List<ActiveSessionDTO> getActiveSessions() {
		logger.debug("getActiveSessions started...");
		
		return List.copyOf(activeSessions.values());
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
		logger.debug("connectProvider started...");
		
		validateProviderConnectionRequest(request, CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CONNECT_PROVIDER_URI);
		
		final GatewayProviderConnectionResponseDTO response = gatewayService.connectProvider(request);
		
		logger.debug("connectProvider finished...");
		return response;
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
		logger.debug("connectConsumer started...");

		validateConsumerConnectionRequest(request, CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CONNECT_CONSUMER_URI);

		final int serverPort = gatewayService.connectConsumer(request);
		
		logger.debug("connectConsumer finished...");
		return serverPort;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private String acquireAndConvertPublicKey() {
		logger.debug("acquireAndConvertPublicKey started...");
		
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
	
	//-------------------------------------------------------------------------------------------------
	private void validateProviderConnectionRequest(final GatewayProviderConnectionRequestDTO request, final String origin) {
		logger.debug("validateProviderConnectionRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("request is null.");
		}
		
		validateRelay(request.getRelay(), origin);
		validateSystem(request.getConsumer(), origin);
		validateSystem(request.getProvider(), origin);
		validateCloud(request.getConsumerCloud(), origin);
		validateCloud(request.getProviderCloud(), origin);
		
		if (Utilities.isEmpty(request.getServiceDefinition())) {
			throw new InvalidParameterException("Service definition is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getConsumerGWPublicKey())) {
			throw new InvalidParameterException("Consumer gateway public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateConsumerConnectionRequest(final GatewayConsumerConnectionRequestDTO request, final String origin) {
		logger.debug("validateConsumerConnectionRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("request is null.");
		}
		
		validateRelay(request.getRelay(), origin);
		validateSystem(request.getConsumer(), origin);
		validateSystem(request.getProvider(), origin);
		validateCloud(request.getConsumerCloud(), origin);
		validateCloud(request.getProviderCloud(), origin);
		
		if (Utilities.isEmpty(request.getServiceDefinition())) {
			throw new InvalidParameterException("Service definition is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getProviderGWPublicKey())) {
			throw new InvalidParameterException("Provider gateway public key is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getQueueId())) {
			throw new InvalidParameterException("Queue id is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getPeerName())) {
			throw new InvalidParameterException("Peer name is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelay(final RelayRequestDTO relay, final String origin) {
		logger.debug("validateRelay started...");
		
		if (relay == null) {
			throw new BadPayloadException("Relay is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
			
		if (Utilities.isEmpty(relay.getAddress())) {
			throw new BadPayloadException("Relay address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (relay.getPort() == null) {
			throw new BadPayloadException("Relay port is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = relay.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(relay.getType())) {
			throw new BadPayloadException("Relay type is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayType type = Utilities.convertStringToRelayType(relay.getType());
		if (type == null || type == RelayType.GATEKEEPER_RELAY) {
			throw new BadPayloadException("Relay type is invalid", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateSystem(final SystemRequestDTO system, final String origin) {
		logger.debug("validateSystem started...");
		
		if (system == null) {
			throw new BadPayloadException("System is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new BadPayloadException("System name is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new BadPayloadException("System address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (system.getPort() == null) {
			throw new BadPayloadException("System port is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloud(final CloudRequestDTO cloud, final String origin) {
		logger.debug("validateCloud started...");
		
		if (cloud == null) {
			throw new BadPayloadException("Cloud is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new BadPayloadException("Cloud operator is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new BadPayloadException("Cloud name is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}