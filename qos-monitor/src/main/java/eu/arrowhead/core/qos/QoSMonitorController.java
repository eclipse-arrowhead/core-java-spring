package eu.arrowhead.core.qos;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.internal.PingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.PingMeasurementResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
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

	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";

	@Autowired
	private QoSDBService qoSDBService;

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
		final PingMeasurementListResponseDTO measurementResponse = qoSDBService.getPingMeasurementResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
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

		final PingMeasurementResponseDTO pingMeasurementResponse = qoSDBService.getPingMeasurementBySystemIdResponse(id);

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

		final PingMeasurementResponseDTO pingMeasurementResponse = qoSDBService.getPingMeasurementBySystemIdResponse(id);

		logger.debug("PingMeasurement entry with system id: {} successfully retrieved", id);
		return pingMeasurementResponse;
	}
}