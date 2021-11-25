/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos;

import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudRelayFormDTO;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.CloudSystemFormDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSBestRelayRequestDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterDirectPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.PingService;
import eu.arrowhead.core.qos.service.RelayTestService;
import eu.arrowhead.core.qos.service.event.EventWatcherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.QOSMONITOR_URI)
public class QoSMonitorController {

	//=================================================================================================
	// members
	private static final String PATH_VARIABLE_ID = "id";

	private static final String QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI =  CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT;
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI = QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_URI = CommonConstants.OP_QOSMONITOR_INTRA_PING_MEASUREMENT + "/{" + PATH_VARIABLE_ID + "}";
	
	private static final String QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI =  CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT;
	private static final String QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT + "/pair_results";
	
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT;
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT + "/pair_results";
	private static final String QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY = CoreCommonConstants.MGMT_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT + "/best_relay";
	
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_DESCRIPTION = "Return requested Ping-Measurements entries by the given parameters";
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_HTTP_200_MESSAGE = "Ping-Measurement entries returned";
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entries";

	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_DESCRIPTION = "Return requested Ping-Measurement entry by system id.";
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";

	private static final String OP_GET_QOSMONITOR_INTRA_PING_MEASUREMENT_BY_SYSTEM_ID_DESCRIPTION = "Return requested Ping-Measurement entry by system id.";
	private static final String OP_GET_QOSMONITOR_INTRA_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String OP_GET_QOSMONITOR_INTRA_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";
	
	private static final String OP_GET_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_BY_ATTRIBUTE = "Return median Ping-Measurement entry by defined attribute.";
	private static final String OP_GET_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_BY_ATTRIBUTE_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String OP_GET_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_BY_ATTRIBUTE_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";
	
	private static final String GET_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_DESCRIPTION = "Return requested Inter-Cloud direct ping measurements entries by the given parameters";
	private static final String GET_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_HTTP_200_MESSAGE = "Ping-Measurement entries returned";
	private static final String GET_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entries";
	
	private static final String QUERY_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_MGMT_DESCRIPTION = "Return requested Inter-Cloud direct ping measurement entry by cloud and system.";
	private static final String POST_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_MGMT_HTTP_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String POST_QOS_MONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_MGMT_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";
	
	private static final String QUERY_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_DESCRIPTION = "Return requested Inter-Cloud direct ping measurement entry by cloud and system.";
	private static final String POST_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_HTTP_200_MESSAGE = "Ping-Measurement entry returned";
	private static final String POST_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_HTTP_400_MESSAGE = "Could not retrieve Ping-Measurement entry";

	private static final String GET_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_DESCRIPTION = "Return requested Inter-Cloud Relay-Echo measurments entries by the given parameters.";
	private static final String GET_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_HTTP_200_MESSAGE = "Relay-Echo measurement entries returned";
	private static final String GET_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Relay-Echo measurement entries";
	
	private static final String QUERY_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_BY_CLOUD_AND_RELAY_MGMT_DESCRIPTION = "Return requested Inter-Cloud Relay-Echo measurment entry by cloud and relay.";
	private static final String POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_HTTP_200_MESSAGE = "Relay-Echo measurement entry returned";
	private static final String POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_HTTP_400_MESSAGE = "Could not retrieve Relay-Echo measurement entry";
	
	private static final String QUERY_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_BEST_RELAY_MGMT_DESCRIPTION = "Return best Inter-Cloud Relay-Echo measurment entry by cloud and attribute.";
	private static final String POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_HTTP_200_MESSAGE = "Relay-Echo measurement entry returned";
	private static final String POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_HTTP_400_MESSAGE = "Could not retrieve Relay-Echo measurement entry";
	
	private static final String QUERY_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_RESULTS_BY_CLOUD_DESCRIPTION = "Return requested Inter-Cloud Relay-Echo measurment results by cloud.";
	private static final String POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_RESULTS_BY_CLOUD_HTTP_200_MESSAGE = "Relay-Echo measurement results returned";
	private static final String POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_RESULTS_BY_CLOUD_HTTP_400_MESSAGE = "Could not retrieve Relay-Echo measurement results";
	
	private static final String GET_PUBLIC_KEY_200_MESSAGE = "Public key returned";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	
	private static final String POST_CONNECT_HTTP_201_MESSAGE = "Connection created";
	private static final String POST_CONNECT_HTTP_400_MESSAGE = "Could not create connection";
	private static final String POST_CONNECT_HTTP_502_MESSAGE = "Error occured when initialize relay communication.";

	private static final String EXTERNAL_PING_MONITOR_NOTIFICATION_DESCRIPTION = "Listens on external ping monitor events";
	private static final String EXTERNAL_PING_MONITOR_NOTIFICATION_HTTP_200_MESSAGE = "External ping monitor event received";
	private static final String EXTERNAL_PING_MONITOR_NOTIFICATION_HTTP_400_MESSAGE = "External ping monitor event has incorrect format";

	@Autowired
	private QoSDBService qosDBService;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private PingService pingService;
	
	@Autowired
	private RelayTestService relayTestService;

	@Autowired
	private EventWatcherService eventWatcherService;

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
				
		final String origin = CommonConstants.QOSMONITOR_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.QOSMONITOR, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_DESCRIPTION, response = QoSIntraPingMeasurementListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSIntraPingMeasurementListResponseDTO getIntraPingMeasurements(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getIntraPingMeasurements get request recieved with page: {} and item_per page: {}", page, size);

		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTRA_PING_MEASUREMENTS_MGMT_URI);
		final QoSIntraPingMeasurementListResponseDTO measurementResponse = qosDBService.getIntraPingMeasurementResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
																												 validParameters.getValidatedDirection(), sortField);

		logger.debug("Measurements  with page: {} and item_per page: {} retrieved successfully", page, size);
		return measurementResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_DESCRIPTION, response = QoSIntraPingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSIntraPingMeasurementResponseDTO getManagementIntraPingMeasurementBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getManagementIntraPingMeasurementBySystemId get request recieved with id: {}", id);

		final String origin = CommonConstants.QOSMONITOR_URI + GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponse = qosDBService.getIntraPingMeasurementBySystemIdResponse(id);

		if (pingMeasurementResponse.getId() == null) {

			logger.debug("PingMeasurement entry with system id: {} is not available.", id);

		}else {

			logger.debug("PingMeasurement entry with system id: {} successfully retrieved.", id);
		}

		return pingMeasurementResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = OP_GET_QOSMONITOR_INTRA_PING_MEASUREMENT_BY_SYSTEM_ID_DESCRIPTION, response = QoSIntraPingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = OP_GET_QOSMONITOR_INTRA_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = OP_GET_QOSMONITOR_INTRA_PING_MEASUREMENT_BY_SYSTEM_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSIntraPingMeasurementResponseDTO getIntraPingMeasurementBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getIntraPingMeasurementBySystemId get request recieved with id: {}", id);
		
		final String origin = CommonConstants.QOSMONITOR_URI + GET_QOSMONITOR_INTRA_PING_MEASUREMENTS_BY_SYSTEM_ID_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponse = qosDBService.getIntraPingMeasurementBySystemIdResponse(id);
		
		logger.debug("PingMeasurement entry with system id: {} successfully retrieved", id);
		return pingMeasurementResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = OP_GET_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_BY_ATTRIBUTE, response = QoSIntraPingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = OP_GET_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_BY_ATTRIBUTE_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = OP_GET_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT_BY_ATTRIBUTE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_QOSMONITOR_INTRA_PING_MEDIAN_MEASUREMENT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSIntraPingMeasurementResponseDTO getIntraPingMedianMeasurement(@PathVariable final String attribute) {
		logger.debug("New getIntraPingMedianMeasurement get request recieved with attribute: {}", attribute);
		
		final QoSIntraPingMeasurementResponseDTO response = pingService.getMedianIntraPingMeasurement(Utilities.convertStringToQoSMeasurementAttribute(attribute));
		logger.debug("PingMeasurement entry successfully retrieved");
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_DESCRIPTION, response = QoSInterDirectPingMeasurementListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterDirectPingMeasurementListResponseDTO getMgmtInterDirectPingMeasurements(@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
																							   		     @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
																							   		     @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
																							   		     @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getInterDirectPingMeasurements request recieved with page: {} and item_per page: {}", page, size);

		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENTS_MGMT_URI);
		final QoSInterDirectPingMeasurementListResponseDTO measurementResponse = qosDBService.getInterDirectPingMeasurementsPageResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
																												 						 validParameters.getValidatedDirection(), sortField);
		logger.debug("Measurements  with page: {} and item_per page: {} retrieved successfully", page, size);
		return measurementResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = QUERY_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_MGMT_DESCRIPTION, response = QoSInterDirectPingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_QOS_MONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterDirectPingMeasurementResponseDTO getMgmtInterDirectPingMeasurementByCloudAndSystem(@RequestBody final CloudSystemFormDTO request) {
		logger.debug("New getMgmtInterDirectPingMeasurementByCloudAndSystem request recieved");
		
		validateCloudSystemForm(request, CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM);
		final QoSInterDirectPingMeasurementResponseDTO response = qosDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(request.getCloud(), request.getSystem().getAddress());
		logger.debug("Measurement retrieved successfully");
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = QUERY_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_DESCRIPTION, response = QoSInterDirectPingMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT_BY_CLOUD_AND_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterDirectPingMeasurementResponseDTO getInterDirectPingMeasurementByCloudAndSystem(@RequestBody final CloudSystemFormDTO request) {
		logger.debug("New getInterDirectPingMeasurementByCloudAndSystem request recieved");
		
		validateCloudSystemForm(request, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_DIRECT_PING_MEASUREMENT);
		final QoSInterDirectPingMeasurementResponseDTO response = qosDBService.getInterDirectPingMeasurementByCloudAndSystemAddressResponse(request.getCloud(), request.getSystem().getAddress());
		logger.debug("Measurement retrieved successfully");
		return response;
	}
			
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_DESCRIPTION, response = QoSInterRelayEchoMeasurementListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterRelayEchoMeasurementListResponseDTO getMgmtInterRelayEchoMeasurements(@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
																							   	   	   @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
																							   	   	   @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
																							   	   	   @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getInterRelayEchoMeasurements request recieved with page: {} and item_per page: {}", page, size);

		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_MGMT_URI);
		final QoSInterRelayEchoMeasurementListResponseDTO response = qosDBService.getInterRelayEchoMeasurementsResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(),
																												  		validParameters.getValidatedDirection(), sortField);
		logger.debug("Measurements  with page: {} and item_per page: {} retrieved successfully", page, size);
		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = QUERY_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_BY_CLOUD_AND_RELAY_MGMT_DESCRIPTION, response = QoSInterRelayEchoMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterRelayEchoMeasurementResponseDTO queryMgmtInterRelayEchoMeasurementByCloudAndRelay(@RequestBody final CloudRelayFormDTO request) {
		logger.debug("New getMgmtInterRelayEchoMeasurementsByCloudAndRelay request recieved");
		
		validateCloudRelayForm(request, CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BY_CLOUD_AND_RELAY);
		final QoSInterRelayEchoMeasurementResponseDTO response = qosDBService.getInterRelayEchoMeasurementByCloudAndRealyResponse(request.getCloud(), request.getRelay());
		logger.debug("Measurement retrieved successfully");
		return response;		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = QUERY_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_BEST_RELAY_MGMT_DESCRIPTION, response = QoSInterRelayEchoMeasurementResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterRelayEchoMeasurementResponseDTO queryMgmtBestInterRelayEchoMeasurementByCloud(@RequestBody final QoSBestRelayRequestDTO request) {
		logger.debug("New getMgmtBestInterRelayEchoMeasurementByCloud request recieved");
		
		validateQoSBestRelayRequest(request, CommonConstants.QOSMONITOR_URI + QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENTS_BEST_RELAY);
		final QoSInterRelayEchoMeasurementResponseDTO response = qosDBService.getBestInterRelayEchoMeasurementByCloudAndAttributeResponse(request.getCloud(),
																																		  Utilities.convertStringToQoSMeasurementAttribute(request.getAttribute()));
		logger.debug("Measurement retrieved successfully");
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = QUERY_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_RESULTS_BY_CLOUD_DESCRIPTION, response = QoSInterRelayEchoMeasurementListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_RESULTS_BY_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT_RESULTS_BY_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSInterRelayEchoMeasurementListResponseDTO queryInterRelayEchoMeasurementByCloud(@RequestBody final CloudRequestDTO request) {
		logger.debug("New queryInterRelayEchoMeasurementByCloud request recieved");
		validateCloudRequest(request, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INTER_RELAY_ECHO_MEASUREMENT);
		
		final QoSInterRelayEchoMeasurementListResponseDTO response = relayTestService.getInterRelayEchoMeasurements(request);
		logger.debug("Measurement retrieved successfully");
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Returns the public key of the QoS Monitor core service as a Base64 encoded text", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_PUBLIC_KEY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_QOSMONITOR_KEY_URI)
	public String getPublicKey() {
		logger.debug("New public key GET request received...");
		
		return "\"" + acquireAndConvertPublicKey() + "\"";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates message queues for testing the connection between this cloud and the requester cloud through the given relay and return the necessary connection informations",
				  response = QoSRelayTestProposalResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_GATEWAY, message = POST_CONNECT_HTTP_502_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_QOSMONITOR_JOIN_RELAY_TEST_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSRelayTestProposalResponseDTO joinRelayTest(@RequestBody final QoSRelayTestProposalRequestDTO request) {
		logger.debug("joinRelayTest started...");
		
		validateRelayTestProposalRequest(request, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_JOIN_RELAY_TEST_URI);
		
		final QoSRelayTestProposalResponseDTO response = relayTestService.joinRelayTest(request);
		
		logger.debug("joinRelayTest finished...");
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates message queues for testing the connection between this cloud and the target cloud through the given relay", 
				  tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CONNECT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CONNECT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_GATEWAY, message = POST_CONNECT_HTTP_502_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_QOSMONITOR_INIT_RELAY_TEST_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void initRelayTest(@RequestBody final QoSMonitorSenderConnectionRequestDTO request) {
		logger.debug("initRelayTest started...");
		
		validateConnectionRequest(request, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_INIT_RELAY_TEST_URI);
		relayTestService.initRelayTest(request);
		
		logger.debug("initRelayTest finished...");
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = EXTERNAL_PING_MONITOR_NOTIFICATION_DESCRIPTION, 
				  tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = EXTERNAL_PING_MONITOR_NOTIFICATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = EXTERNAL_PING_MONITOR_NOTIFICATION_HTTP_400_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.ACCEPTED)
	@PostMapping(path = QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void pingMonitorNotification(@RequestBody final EventDTO request) {
		logger.debug("pingMonitorNotification started...");

		validateEvent(request, CommonConstants.QOSMONITOR_URI + QosMonitorConstants.EXTERNAL_PING_MONITOR_EVENT_NOTIFICATION_URI);
		eventWatcherService.putEventToQueue(request);

		logger.debug("pingMonitorNotification finished...");
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private String acquireAndConvertPublicKey() {
		logger.debug("acquireAndConvertPublicKey started...");
		
		final String origin = CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_KEY_URI;
		
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
	private void validateConnectionRequest(final QoSMonitorSenderConnectionRequestDTO request, final String origin) {
		logger.debug("validateConnectionRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("Connection request is null.");
		}
		
		validateCloudRequest(request.getTargetCloud(), origin);
		validateRelayRequest(request.getRelay(), origin);
		
		if (Utilities.isEmpty(request.getQueueId())) {
			throw new BadPayloadException("Queue id is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getPeerName())) {
			throw new BadPayloadException("Peer name is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getReceiverQoSMonitorPublicKey())) {
			throw new BadPayloadException("Receiver QoS Monitor's public key is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudRelayForm(final CloudRelayFormDTO request, final String origin) {
		if (request == null) {
			throw new InvalidParameterException("CloudRelayFormDTO is null.");
		}		
		validateCloudResponse(request.getCloud(), origin);
		validateRelayResponse(request.getRelay(), origin);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudSystemForm(final CloudSystemFormDTO request, final String origin) {
		if (request == null) {
			throw new InvalidParameterException("CloudRelayFormDTO is null.");
		}		
		validateCloudResponse(request.getCloud(), origin);
		validateSystemResponse(request.getSystem(), origin);
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
	private void validateCloudResponse(final CloudResponseDTO cloud, final String origin) {
		logger.debug("validateCloudResponse started...");
		
		if (cloud == null) {
			throw new BadPayloadException("Cloud is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (cloud.getId() < 1) {
			throw new BadPayloadException("Cloud id less than 1", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new BadPayloadException("Cloud operator is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new BadPayloadException("Cloud name is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateSystemResponse(final SystemResponseDTO system, final String origin) {
		logger.debug("validateCloudResponse started...");
		
		if (system == null) {
			throw new BadPayloadException("System is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (system.getId() < 1) {
			throw new BadPayloadException("System id less than 1", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new BadPayloadException("System name is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new BadPayloadException("System address is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequest(final RelayRequestDTO relay, final String origin) {
		logger.debug("validateRelayRequest started...");
		
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

	//-------------------------------------------------------------------------------------------------
	private void validateRelayResponse(final RelayResponseDTO relay, final String origin) {
		logger.debug("validateRelayRequest started...");
		
		if (relay == null) {
			throw new BadPayloadException("relay is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
			
		if (relay.getId() < 1) {
			throw new BadPayloadException("Relay id less than 1", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(relay.getAddress())) {
			throw new BadPayloadException("Relay address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = relay.getPort();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (relay.getType() == null) {
			throw new BadPayloadException("Relay type is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSBestRelayRequest(final QoSBestRelayRequestDTO request, final String origin) {
		logger.debug("validateQoSBestRelayRequest started...");
		
		if (request == null) {
			throw new BadPayloadException("QoSBestRelayRequestDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		validateCloudResponse(request.getCloud(), origin);
		
		if (Utilities.isEmpty(request.getAttribute())) {
			throw new BadPayloadException("attribute is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void validateEvent(final EventDTO request, final String origin) {
		logger.debug("validateEvent started...");

		if (request == null) {
			throw new BadPayloadException("QoSBestRelayRequestDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(request.getEventType())) {
			throw new BadPayloadException("EventType is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (!checkExternalPingMonitoringNotificationEventType(request.getEventType())) {
			throw new BadPayloadException("EventType is not a valid PingMonitoringEvent-Type", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(request.getPayload())) {
			throw new BadPayloadException("Payload is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(request.getTimeStamp())) {
			throw new BadPayloadException("TimeStamp is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}

		try {
			Utilities.parseUTCStringToLocalZonedDateTime(request.getTimeStamp());
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("TimeStamp format is not accepted as : " + request.getTimeStamp(), HttpStatus.SC_BAD_REQUEST, origin);
		}

	}

	//-------------------------------------------------------------------------------------------------
	private boolean checkExternalPingMonitoringNotificationEventType(final String eventType) {
		logger.debug("checkExternalPingMonitoringNotificationEventType started...");

		for (final QosMonitorEventType type : QosMonitorEventType.values()) {
			if (eventType.equalsIgnoreCase(type.name())) {
				return true;
			}
		}

		return false;
	}
}
