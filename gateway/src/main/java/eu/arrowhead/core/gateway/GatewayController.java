/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.gateway;

import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.ActiveSessionCloseErrorDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.service.ActiveSessionListDTO;
import eu.arrowhead.core.gateway.service.GatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.GATEWAY_URI)
public class GatewayController {
	
	//=================================================================================================
	// members
	private static final String ACTIVE_SESSIONS_MGMT_URI = CoreCommonConstants.MGMT_URI + "/sessions";
	private static final String CLOSE_SESSION_MGMT_URI = ACTIVE_SESSIONS_MGMT_URI + "/close";
	
	private static final String GET_ACTIVE_SESSIONS_HTTP_200_MESSAGE = "Sessions returned";
	private static final String GET_ACTIVE_SESSIONS_HTTP_400_MESSAGE = "Could not return sessions";
	
	private static final String POST_CONNECT_HTTP_201_MESSAGE = "Connection created";
	private static final String POST_CONNECT_HTTP_400_MESSAGE = "Could not create connection";
	private static final String POST_CONNECT_HTTP_502_MESSAGE = "Error occured when initialize relay communication.";
	
	private static final String POST_CLOSE_SESSION_HTTP_200_MESSAGE = "Session closed";
	private static final String POST_CLOSE_SESSION_400_MESSAGE = "Could not close session";

	private static final String POST_CLOSE_SESSIONS_HTTP_200_MESSAGE = "Sessions closed";
	private static final String POST_CLOSE_SESSIONS_400_MESSAGE = "Could not close sessions";
	
	private static final String GET_PUBLIC_KEY_200_MESSAGE = "Public key returned";
	
	private final Logger logger = LogManager.getLogger(GatewayController.class);
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP)
	private ConcurrentHashMap<String,ActiveSessionDTO> activeSessions; 
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	@Autowired
	private GatewayService gatewayService;
	
	@Value(CoreCommonConstants.$GATEWAY_MIN_PORT_WD)
	private int minPort;
	
	@Value(CoreCommonConstants.$GATEWAY_MAX_PORT_WD)
	private int maxPort;

	@Autowired
	private CommonDBService commonDBService;

	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested log entries by the given parameters", response = LogEntryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_QUERY_LOG_ENTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public LogEntryListResponseDTO getLogEntries(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = Logs.FIELD_NAME_ID) final String sortField,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOG_LEVEL, required = false) final String logLevel,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_FROM, required = false) final String from,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_TO, required = false) final String to,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOGGER, required = false) final String loggerStr) { 
		logger.debug("New getLogEntries GET request received with page: {} and item_per page: {}", page, size);
				
		final String origin = CommonConstants.GATEWAY_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.GATEWAY, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the public key of the Gateway core service as a Base64 encoded text", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_PUBLIC_KEY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_GATEWAY_KEY_URI)
	public String getPublicKey() {
		logger.debug("New public key GET request received...");
		
		return "\"" + acquireAndConvertPublicKey() + "\"";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return active Gateway sessions", response = ActiveSessionListDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ACTIVE_SESSIONS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ACTIVE_SESSIONS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ACTIVE_SESSIONS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ActiveSessionListDTO getActiveSessions(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size) {
		logger.debug("getActiveSessions started...");
		
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, CommonConstants.SORT_ORDER_ASCENDING, CommonConstants.GATEWAY_URI + ACTIVE_SESSIONS_MGMT_URI);
		if (activeSessions.isEmpty()) {
			return new ActiveSessionListDTO(List.of(), 0);
		}
		
		final List<ActiveSessionDTO> sessionList = new ArrayList<>(activeSessions.size());
		sessionList.addAll(activeSessions.values());
		sessionList.sort((s1, s2) -> createZonedDateTimeFromStringDateTime(s1.getSessionStartedAt()).compareTo(createZonedDateTimeFromStringDateTime(s2.getSessionStartedAt())));
		
		final ActiveSessionListDTO response = new ActiveSessionListDTO(sessionList, sessionList.size());
		if (validParameters.getValidatedPage() >= 0 && validParameters.getValidatedSize() >= 1) {			
			final int start = validParameters.getValidatedPage() * validParameters.getValidatedSize();
			final int end = start + validParameters.getValidatedSize() > sessionList .size() ? sessionList .size() : start + validParameters.getValidatedSize();	
			response.setData(sessionList.subList(start, end));
		} else if (validParameters.getValidatedPage() == -1 && validParameters.getValidatedSize() == -1) {
			// Do nothing
		} else {
			throw new BadPayloadException("Page parameter has to be equals or greater than zero and size parameter has to be equals or greater than one.", HttpStatus.SC_BAD_REQUEST,
										  CommonConstants.GATEWAY_URI + ACTIVE_SESSIONS_MGMT_URI);			
		}
		
		logger.debug("getActiveSessions finished...");
		return response;
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Closing the requested active gateway session", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_CLOSE_SESSION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CLOSE_SESSION_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CLOSE_SESSION_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void closeActiveSession(@RequestBody final ActiveSessionDTO request) {
		logger.debug("closeActiveSession started...");
		
		validateActiveSessionDTO(request, CommonConstants.GATEWAY_URI + CLOSE_SESSION_MGMT_URI);		
		gatewayService.closeSession(request);
		
		logger.debug("closeActiveSession finished...");
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Closing the requested active gateway sessions", tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_CLOSE_SESSIONS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CLOSE_SESSIONS_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEWAY_CLOSE_SESSIONS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ActiveSessionCloseErrorDTO> closeActiveSessions(@RequestBody final List<Integer> ports) { 
		logger.debug("closeActiveSessions started...");
		
		if (Utilities.isEmpty(ports)) {
			throw new BadPayloadException("Ports list is null or empty.", HttpStatus.SC_BAD_REQUEST, CommonConstants.GATEWAY_URI + CommonConstants.OP_GATEWAY_CLOSE_SESSIONS);
		}
		
		final List<ActiveSessionCloseErrorDTO> response = new ArrayList<>(); 
		for (final int port : new HashSet<>(ports)) {
			String error = validateActiveSessionPort(port);
			
			if (error == null) {
				error = gatewayService.closeSession(port);
			}

			if (error != null) {
				response.add(new ActiveSessionCloseErrorDTO(port, error));
			}
		}
		
		logger.debug("closeActiveSessions finished...");
		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates a Socket and Message queue between the given Relay and Provider and return the necessary connection informations",
				  response = GatewayProviderConnectionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_GATEWAY, message = POST_CONNECT_HTTP_502_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
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
	@ApiOperation(value = "Creates a ServerSocket between the given Relay and Consumer and return the ServerSocket port", response = Integer.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_GATEWAY, message = POST_CONNECT_HTTP_502_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
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
			throw new BadPayloadException("Service definition is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getConsumerGWPublicKey())) {
			throw new BadPayloadException("Consumer gateway public key is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
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
			throw new BadPayloadException("Service definition is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getProviderGWPublicKey())) {
			throw new BadPayloadException("Provider gateway public key is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getQueueId())) {
			throw new BadPayloadException("Queue id is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getPeerName())) {
			throw new BadPayloadException("Peer name is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateActiveSessionDTO(final ActiveSessionDTO dto, final String origin) {
		logger.debug("validateActiveSessionDTO started...");
		
		if (dto == null) {
			throw new BadPayloadException("ActiveSessionDTO is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getQueueId() == null || Utilities.isEmpty(dto.getQueueId())) {
			throw new BadPayloadException("queueId is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getPeerName() == null || Utilities.isEmpty(dto.getPeerName())) {
			throw new BadPayloadException("peerName is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getServiceDefinition() == null || Utilities.isEmpty(dto.getServiceDefinition())) {
			throw new BadPayloadException("serviceDefinition id is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getRequestQueue() == null || Utilities.isEmpty(dto.getRequestQueue())) {
			throw new BadPayloadException("requestQueue is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getRequestControlQueue() == null || Utilities.isEmpty(dto.getRequestControlQueue())) {
			throw new BadPayloadException("requestControlQueue is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getResponseQueue() == null || Utilities.isEmpty(dto.getResponseQueue())) {
			throw new BadPayloadException("responseQueue is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getResponseControlQueue() == null || Utilities.isEmpty(dto.getResponseControlQueue())) {
			throw new BadPayloadException("responseControlQueue is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getSessionStartedAt() == null || Utilities.isEmpty(dto.getSessionStartedAt())) {
			throw new BadPayloadException("sessionStartedAt is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		validateSystem(dto.getConsumer(), origin);
		validateSystem(dto.getProvider(), origin);
		validateCloud(dto.getConsumerCloud(), origin);
		validateCloud(dto.getProviderCloud(), origin);
		validateRelay(dto.getRelay(), origin);
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
			throw new BadPayloadException("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST,
										  origin);
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
			throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST,
										  origin);
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
			throw new BadPayloadException("Cloud name is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	///-------------------------------------------------------------------------------------------------
	private ZonedDateTime createZonedDateTimeFromStringDateTime(final String dateTime) {
		logger.debug("createZonedDateTimeFromStringDateTime started...");
		
		final String _dateTime = dateTime.substring(0, dateTime.length() - 1); // remove Z
		final String[] dateTimeSplit = _dateTime.split("T");
		final String[] date = dateTimeSplit[0].split("-");
		final String[] time = dateTimeSplit[1].split(":");
		return ZonedDateTime.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]), Integer.valueOf(time[0]), Integer.valueOf(time[1]), Integer.valueOf(time[2]), 0,
								ZoneOffset.UTC);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String validateActiveSessionPort(final int port) {
		logger.debug("validateActiveSessionPort started...");
		
		if (port < minPort || port > maxPort) {
			return "Invalid active session port.";
		}
		
		return null;
	}
}