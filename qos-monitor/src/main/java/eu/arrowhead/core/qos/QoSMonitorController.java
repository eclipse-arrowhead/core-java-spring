package eu.arrowhead.core.qos;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

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
import org.springframework.web.bind.annotation.PathVariable;
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
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.PingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.RelayTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.QOS_MONITOR_URI)
public class QoSMonitorController {

	//=================================================================================================
	// members
	private static final String PATH_VARIABLE_ID = "id";

	private static final String PING_MEASUREMENTS = "/ping/measurements";
	private static final String QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI =  CoreCommonConstants.MGMT_URI + PING_MEASUREMENTS;
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI = QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_URI = CommonConstants.OP_QOS_MONITOR_PING_MEASUREMENT + "/{" + PATH_VARIABLE_ID + "}";

	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_MGMT_DESCRIPTION = "Return requested Ping-Measurements entries by the given parameters";
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_MGMT_HTTP_200_MESSAGE = "Ping-Measurement entries returned";
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entries";

	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_DESCRIPTION = "Return requested Ping-Measurement entry by system id.";
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";

	private static final String OP_GET_QOS_MONITOR_PING_MEASUREMENT_BY_SYSTEM_ID_DESCRIPTION = "Return requested Ping-Measurement entry by system id.";
	private static final String OP_GET_QOS_MONITOR_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String OP_GET_QOS_MONITOR_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";

	private static final String GET_PUBLIC_KEY_200_MESSAGE = "Public key returned";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	
	private static final String POST_CONNECT_HTTP_201_MESSAGE = "Connection created";
	private static final String POST_CONNECT_HTTP_400_MESSAGE = "Could not create connection";

	@Autowired
	private QoSDBService qosDBService;
	
	@Autowired
	private RelayTestService relayTestService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	private final Logger logger = LogManager.getLogger(QoSMonitorController.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		logger.debug("echoService started...");

		return "Got it!";
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_QOS_MONITOR_PING_MEASUREMENTS_MGMT_DESCRIPTION, response = PingMeasurementListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_QOS_MONITOR_PING_MEASUREMENTS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_QOS_MONITOR_PING_MEASUREMENTS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public PingMeasurementListResponseDTO getPingMeasurements(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getMeasurements get request recieved with page: {} and item_per page: {}", page, size);

		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.QOS_MONITOR_URI + QOS_MONITOR_PING_MEASUREMENTS_MGMT_URI);
		final PingMeasurementListResponseDTO measurementResponse = qosDBService.getPingMeasurementResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
																												 validParameters.getValidatedDirecion(), sortField);

		logger.debug("Measurements  with page: {} and item_per page: {} retrieved successfully", page, size);
		return measurementResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_DESCRIPTION, response = PingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public PingMeasurementResponseDTO getManagementPingMeasurementBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getPingMeasurementBySystemId get request recieved with id: {}", id);

		final String origin = CommonConstants.QOS_MONITOR_URI + GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		final PingMeasurementResponseDTO pingMeasurementResponse = qosDBService.getPingMeasurementBySystemIdResponse(id);

		if (pingMeasurementResponse.getId() == null) {

			logger.debug("PingMeasurement entry with system id: {} is not available.", id);

		}else {

			logger.debug("PingMeasurement entry with system id: {} successfully retrieved.", id);
		}

		return pingMeasurementResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = OP_GET_QOS_MONITOR_PING_MEASUREMENT_BY_SYSTEM_ID_DESCRIPTION, response = PingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = OP_GET_QOS_MONITOR_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = OP_GET_QOS_MONITOR_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public PingMeasurementResponseDTO getPingMeasurementBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getPingMeasurementBySystemId get request recieved with id: {}", id);

		final String origin = CommonConstants.QOS_MONITOR_URI + GET_QOS_MONITOR_PING_MEASUREMENTS_BY_SYSTEM_ID_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		final PingMeasurementResponseDTO pingMeasurementResponse = qosDBService.getPingMeasurementBySystemIdResponse(id);

		logger.debug("PingMeasurement entry with system id: {} successfully retrieved", id);
		return pingMeasurementResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the public key of the QoS Monitor core service as a Base64 encoded text", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_PUBLIC_KEY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_QOS_MONITOR_KEY_URI)
	public String getPublicKey() {
		logger.debug("New public key GET request received...");
		
		return acquireAndConvertPublicKey();
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates a Message queue for testing the connection between this cloud and requester cloud through the given Relay and return the necessary connection informations",
				  response = QoSRelayTestProposalResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_QOS_MONITOR_JOIN_RELAY_TEST_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSRelayTestProposalResponseDTO joinRelayTest(@RequestBody final QoSRelayTestProposalRequestDTO request) {
		logger.debug("joinRelayTest started...");
		
		validateRelayTestProposalRequest(request, CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_JOIN_RELAY_TEST_URI);
		
		final QoSRelayTestProposalResponseDTO response = relayTestService.joinRelayTest(request);
		
		logger.debug("joinRelayTest finished...");
		return response;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private String acquireAndConvertPublicKey() {
		logger.debug("acquireAndConvertPublicKey started...");
		
		final String origin = CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_KEY_URI;
		
		if (!secure) {
			throw new ArrowheadException("QoS Monitor core service runs in insecure mode.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Public key is not available.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayTestProposalRequest(final QoSRelayTestProposalRequestDTO request, final String origin) {
		logger.debug("validateRelayTestProposalRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("Relay test proposal is null.");
		}
		
		validateCloudRequest(request.getRequesterCloud(), origin);
		validateRelayRequest(request.getRelay(), origin);
		
		if (Utilities.isEmpty(request.getSenderQoSMonitorPublicKey())) {
			throw new BadPayloadException("Sender QoS Monitor's public key is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudRequest(final CloudRequestDTO cloud, final String origin) {
		logger.debug("validateCloudRequest started...");
		
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
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequest(final RelayRequestDTO relay, final String origin) {
		logger.debug("validateRelayRequest started...");
		
		if (relay == null) {
			throw new BadPayloadException("relay is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
			
		if (Utilities.isEmpty(relay.getAddress())) {
			throw new BadPayloadException("Relay address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (relay.getPort() == null) {
			throw new BadPayloadException("Relay port is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = relay.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
										  HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(relay.getType())) {
			throw new BadPayloadException("Relay type is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayType type = Utilities.convertStringToRelayType(relay.getType());
		if (type == null || type == RelayType.GATEKEEPER_RELAY) {
			throw new BadPayloadException("Relay type is invalid", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}

}