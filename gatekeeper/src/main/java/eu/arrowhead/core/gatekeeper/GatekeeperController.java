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

package eu.arrowhead.core.gatekeeper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudRelaysAssignmentRequestDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDMultiQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryResultDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.RelayListResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.service.GatekeeperService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.GATEKEEPER_URI)
public class GatekeeperController {
	
	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String PATH_VARIABLE_ADDRESS = "address";
	private static final String PATH_VARIABLE_PORT = "port";
	private static final String PATH_VARIABLE_OPERATOR = "operator";
	private static final String PATH_VARIABLE_NAME = "name";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0.";
	
	private static final String CLOUDS_MGMT_URI =  CoreCommonConstants.MGMT_URI + "/clouds";
	private static final String CLOUDS_BY_ID_MGMT_URI = CLOUDS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String CLOUDS_ASSIGN_RELAYS_MGMT_URI =  CLOUDS_MGMT_URI + "/assign";
	
	private static final String RELAYS_MGMT_URI =  CoreCommonConstants.MGMT_URI + "/relays";
	private static final String RELAYS_BY_ID_MGMT_URI = RELAYS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";	
	private static final String RELAYS_BY_ADDRESS_AND_PORT_MGMT_URI = RELAYS_MGMT_URI + "/{" + PATH_VARIABLE_ADDRESS + "}" + "/{" + PATH_VARIABLE_PORT + "}";
	
	private static final String GET_CLOUDS_HTTP_200_MESSAGE = "Cloud entries returned";
	private static final String GET_CLOUDS_HTTP_400_MESSAGE = "Could not retrieve Cloud entries";
	private static final String POST_CLOUDS_MGMT_HTTP_201_MESSAGE = "Cloud entries created";
	private static final String POST_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not create Cloud entries";
	private static final String POST_CLOUDS_ASSIGN_MGMT_HTTP_200_MESSAGE = "Cloud and Relay entries assigned";
	private static final String POST_CLOUDS_ASSIGN_MGMT_HTTP_400_MESSAGE = "Could not assign Cloud and Relay entries";
	private static final String PUT_CLOUDS_MGMT_HTTP_200_MESSAGE = "Cloud entry updated";
	private static final String PUT_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not update Cloud entry";
	private static final String DELETE_CLOUDS_MGMT_HTTP_200_MESSAGE = "Cloud entry removed";
	private static final String DELETE_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not remove Cloud entry";
	
	private static final String GET_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entries returned";
	private static final String GET_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Relay entries";
	private static final String POST_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entries created";
	private static final String POST_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not create Relay entries";
	private static final String PUT_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entry updated";
	private static final String PUT_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not update Relay entry";
	private static final String DELETE_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entry removed";
	private static final String DELETE_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not remove Relay entry";
	
	private static final String POST_INIT_GSD_HTTP_200_MESSAGE = "GSD results returned";
	private static final String POST_INIT_GSD_HTTP_400_MESSAGE = "Could not initiate GSD";
	private static final String POST_INIT_ICN_DESCRIPTION = "Starts the inter cloud negotiation process";
	private static final String POST_INIT_ICN_HTTP_200_MESSAGE = "ICN result returned";
	private static final String POST_INIT_ICN_HTTP_400_MESSAGE = "Could not initiate inter cloud negotiation";
	private static final String POST_INIT_ICN_HTTP_504_MESSAGE = "Timeout occurs in the communication via relay.";
	private static final String GET_PULL_CLOUDS_DESCRIPTION = "Return all registrated clouds for QoS Monitor Core System";
	private static final String POST_COLLECT_SYSTEM_ADDRESSES_DESCRIPTION = "Return all registrated system ip address from a neighbor cloud for QoS Monitor Core System";
	private static final String POST_COLLECT_SYSTEM_ADDRESSES_HTTP_200_MESSAGE = "Addresses returned";
	private static final String POST_COLLECT_SYSTEM_ADDRESSES_HTTP_400_MESSAGE = "Could not collect addresses";
	private static final String POST_COLLECT_ACCESS_TYPES_DESCRIPTION = "Return access types of requested clouds for QoS Monitor Core System";
	private static final String POST_COLLECT_ACCESS_TYPES_HTTP_200_MESSAGE = "Access types returned";
	private static final String POST_COLLECT_ACCESS_TYPES_HTTP_400_MESSAGE = "Could not collect access types";
	private static final String POST_INIT_RELAY_TEST_DESCRIPTION = "Starts a test between this cloud and the target cloud using the specified relay.";
	private static final String POST_INIT_RELAY_TEST_HTTP_200_MESSAGE = "Test started";
	private static final String POST_INIT_RELAY_TEST_HTTP_400_MESSAGE = "Could not start the test";
	private static final String POST_INIT_RELAY_TEST_HTTP_504_MESSAGE = "Timeout occurs in the communication via relay.";
	private static final String GET_GET_CLOUD_DESCRIPTION = "Returns the specified cloud (with all the available gateway relays, even public ones)";
	private static final String GET_GET_CLOUD_HTTP_200_MESSAGE = "Cloud returned";
	private static final String GET_GET_CLOUD_HTTP_400_MESSAGE = "Could not acquire cloud";
	
	private final Logger logger = LogManager.getLogger(GatekeeperController.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	@Autowired
	private GatekeeperService gatekeeperService;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
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
				
		final String origin = CommonConstants.GATEKEEPER_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.GATEKEEPER, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Cloud entries by the given parameters", response = CloudWithRelaysListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CLOUDS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CLOUDS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CLOUDS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysListResponseDTO getClouds(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getClouds get request recieved with page: {} and item_per page: {}", page, size);
				
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.GATEKEEPER_URI + CLOUDS_MGMT_URI);
		final CloudWithRelaysListResponseDTO cloudsResponse = gatekeeperDBService.getCloudsResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
																									validParameters.getValidatedDirection(), sortField);
		
		logger.debug("Clouds  with page: {} and item_per page: {} retrieved successfully", page, size);
		return cloudsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Cloud entry", response = CloudWithRelaysResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CLOUDS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CLOUDS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CLOUDS_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO getCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getCloudById get request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final CloudWithRelaysResponseDTO cloudResponse = gatekeeperDBService.getCloudByIdResponse(id);
		
		logger.debug("Cloud entry with id: {} successfully retrieved", id);
		return cloudResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created Cloud entries", response = CloudWithRelaysListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CLOUDS_MGMT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CLOUDS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysListResponseDTO registerClouds(@RequestBody final List<CloudRequestDTO> dtoList) {
		logger.debug("New registerClouds post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_MGMT_URI;
		
		if (dtoList == null || dtoList.isEmpty()) {
			throw new BadPayloadException("List of CloudRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final CloudRequestDTO dto : dtoList) {
			validateCloudRequestDTO(dto, origin);
		}
		
		final CloudWithRelaysListResponseDTO cloudsResponse = gatekeeperDBService.registerBulkCloudsWithRelaysResponse(dtoList);
		
		logger.debug("registerClouds has been finished");
		return cloudsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated Cloud entry", response = CloudWithRelaysResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_CLOUDS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = CLOUDS_BY_ID_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO updateCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final CloudRequestDTO dto) {
		logger.debug("New updateCloudById put request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		validateCloudRequestDTO(dto, origin);
		final CloudWithRelaysResponseDTO cloudResponse = gatekeeperDBService.updateCloudByIdWithRelaysResponse(id, dto);
		
		logger.debug("Cloud with id '{}' is successfully updated", id);
		return cloudResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return Cloud entry with its Relay entries", response = CloudWithRelaysResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_CLOUDS_ASSIGN_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CLOUDS_ASSIGN_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CLOUDS_ASSIGN_RELAYS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO assignRelaysToCloud(@RequestBody final CloudRelaysAssignmentRequestDTO dto) {
		logger.debug("New assignRelaysToCloud post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_ASSIGN_RELAYS_MGMT_URI;
		
		if (dto == null) {
			throw new BadPayloadException("CloudRelaysAssignmentRequestDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getCloudId() == null || dto.getCloudId() < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ((dto.getGatekeeperRelayIds() == null || dto.getGatekeeperRelayIds().isEmpty()) && (dto.getGatewayRelayIds() == null || dto.getGatewayRelayIds().isEmpty())) {
			throw new BadPayloadException("GatekeeperRelayIds and GatewayRelayIds list couldn't be null or empty at the same time", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getGatekeeperRelayIds() != null) {
			for (final Long id : dto.getGatekeeperRelayIds()) {
				if (id == null || id < 1) {
					throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
				}
			}
		}
		
		if (dto.getGatewayRelayIds() != null) {
			for (final Long id : dto.getGatewayRelayIds()) {
				if (id == null || id < 1) {
					throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
				}
			}
		}
		
		final CloudWithRelaysResponseDTO cloudResponse = gatekeeperDBService.assignRelaysToCloudResponse(dto.getCloudId(), dto.getGatekeeperRelayIds(), dto.getGatewayRelayIds());
		
		logger.debug("assignRelaysToCloud post request successfully finished");
		return cloudResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove requested Cloud entry", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_CLOUDS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CLOUDS_BY_ID_MGMT_URI)
	public void removeCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New removeCloudById delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.GATEKEEPER_URI + CLOUDS_BY_ID_MGMT_URI);
		}
		
		gatekeeperDBService.removeCloudById(id);
		logger.debug("Cloud with id '{}' is successfully removed", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Relay entries by the given parameters", response = RelayListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = RELAYS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayListResponseDTO getRelays(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getRelays get request recieved with page: {} and item_per page: {}", page, size);
		
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.GATEKEEPER_URI + RELAYS_MGMT_URI);
		final RelayListResponseDTO relaysResponse = gatekeeperDBService.getRelaysResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(),
																					      validParameters.getValidatedDirection(), sortField);
		
		logger.debug("Relays  with page: {} and item_per page: {} retrieved successfully", page, size);
		return relaysResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Relay entry", response = RelayResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = RELAYS_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO getRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getRelayById get request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.getRelayByIdResponse(id);
		
		logger.debug("Relay entry with id: {} successfully retrieved", id);
		return relayResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Relay entry", response = RelayResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = RELAYS_BY_ADDRESS_AND_PORT_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO getRelayByAddressAndPort(@PathVariable(value = PATH_VARIABLE_ADDRESS) final String address, @PathVariable(value = PATH_VARIABLE_PORT) final int port) {
		logger.debug("New getRelayByAddressAndPort get request recieved with address: '{}', and port: '{}'", address, port);
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_BY_ADDRESS_AND_PORT_MGMT_URI;
		
		if (Utilities.isEmpty(address)) {
			throw new BadPayloadException("Address is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (isPortOutOfValidRange(port)) {
			throw new BadPayloadException("Port should be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.getRelayByAddressAndPortResponse(address, port);
		
		logger.debug("Relay entry with address: '{}', and port: '{}' successfully retrieved", address, port);
		return relayResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created Relay entries", response = RelayListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = RELAYS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayListResponseDTO registerRelays(@RequestBody final List<RelayRequestDTO> dtoList) { 
		logger.debug("New registerRelays post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_MGMT_URI;
		
		if (dtoList == null || dtoList.isEmpty()) {
			throw new BadPayloadException("List of RelayRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final RelayRequestDTO dto : dtoList) {
			validateRelayRequestDTO(dto, origin);
		}
		
		final RelayListResponseDTO relayResponseListDTO = gatekeeperDBService.registerBulkRelaysResponse(dtoList);
		
		logger.debug("registerRelays has been finished");
		return relayResponseListDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated Relay entry", response = RelayResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = RELAYS_BY_ID_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO updateRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final RelayRequestDTO dto) { 
		logger.debug("New updateRelayById put request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		validateRelayRequestDTO(dto, origin);
		final RelayResponseDTO relayResponse = gatekeeperDBService.updateRelayByIdResponse(id, dto.getAddress(), dto.getPort(), dto.getAuthenticationInfo(), dto.isSecure(), dto.isExclusive(),
																						   Utilities.convertStringToRelayType(dto.getType()));
		
		logger.debug("Relay with id '{}' is successfully updated", id);
		return relayResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove requested Relay entry", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = RELAYS_BY_ID_MGMT_URI)
	public void removeRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New removeRelayById delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.GATEKEEPER_URI + RELAYS_BY_ID_MGMT_URI);
		}
		
		gatekeeperDBService.removeRelayById(id);
		logger.debug("Relay with id '{}' is successfully removed", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return the results of Global Service Discovery request", response = GSDQueryResultDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_INIT_GSD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INIT_GSD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEKEEPER_GSD_SERVICE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public GSDQueryResultDTO initiateGlobalServiceDiscovery(@RequestBody final GSDQueryFormDTO gsdForm) throws InterruptedException {
		logger.debug("New initiateGlobalServiceDiscovery post request received");
		
		validateGSDQueryFormDTO(gsdForm, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GSD_SERVICE);
		final GSDQueryResultDTO gsdQueryResultDTO = gatekeeperService.initGSDPoll(gsdForm);
		
		logger.debug("initiateGlobalServiceDiscovery has been finished");
		return gsdQueryResultDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return the results of a  Multi Global Service Discovery request", response = GSDMultiQueryResultDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_INIT_GSD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INIT_GSD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEKEEPER_MULTI_GSD_SERVICE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public GSDMultiQueryResultDTO initiateMultiGlobalServiceDiscovery(@RequestBody final GSDMultiQueryFormDTO gsdForm) throws InterruptedException { 
		logger.debug("New initiateMultiGlobalServiceDiscovery post request received");
		
		validateMultiGSDQueryFormDTO(gsdForm, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_MULTI_GSD_SERVICE);
		final GSDMultiQueryResultDTO gsdQueryResultDTO = gatekeeperService.initMultiGSDPoll(gsdForm);
		
		logger.debug("initiateMultiGlobalServiceDiscovery has been finished");
		return gsdQueryResultDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_INIT_ICN_DESCRIPTION, response = ICNResultDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_INIT_ICN_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INIT_ICN_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_GATEWAY_TIMEOUT, message = POST_INIT_ICN_HTTP_504_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEKEEPER_ICN_SERVICE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ICNResultDTO initiateInterCloudNegotiation(@RequestBody final ICNRequestFormDTO icnForm) {
		logger.debug("New initiateInterCloudNegotiation request received");
		
		validateICNRequestFormDTO(icnForm, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_ICN_SERVICE);
		final ICNResultDTO result = gatekeeperService.initICN(icnForm);
		
		logger.debug("Inter cloud negotiation has been finished.");
		return result;
	}
	

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_PULL_CLOUDS_DESCRIPTION, response = CloudWithRelaysAndPublicRelaysListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CLOUDS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CLOUDS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_GATEKEEPER_PULL_CLOUDS_SERVICE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysAndPublicRelaysListResponseDTO pullClouds() {
		logger.debug("new pullClouds request received");
		final CloudWithRelaysAndPublicRelaysListResponseDTO cloudsResponse = gatekeeperDBService.getCloudsWithPublicRelaysResponse(-1, -1, null, null);
		logger.debug("pullClouds request successfully finished");
		return cloudsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_COLLECT_SYSTEM_ADDRESSES_DESCRIPTION, response = SystemAddressSetRelayResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_COLLECT_SYSTEM_ADDRESSES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_COLLECT_SYSTEM_ADDRESSES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_SERVICE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemAddressSetRelayResponseDTO collectSystemAddressesOfNeighborCloud(@RequestBody final CloudRequestDTO dto) {
		logger.debug("new collectSystemAddressesOfNeighborCloud request received");
		
		validateCloudRequestDTO(dto, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_COLLECT_SYSTEM_ADDRESSES_SERVICE);
		final SystemAddressSetRelayResponseDTO addresses = gatekeeperService.initSystemAddressCollection(dto);
		
		logger.debug("collectSystemAddressesOfNeighborCloud request successfully finished");
		return addresses;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_COLLECT_ACCESS_TYPES_DESCRIPTION, response = CloudAccessListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_COLLECT_ACCESS_TYPES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_COLLECT_ACCESS_TYPES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEKEEPER_COLLECT_ACCESS_TYPES_SERVICE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudAccessListResponseDTO collectNeighborCloudAccessTypes(@RequestBody final List<CloudRequestDTO> dtos) throws InterruptedException {
		logger.debug("new collectNeighborCloudAccessTypes request received");
		for (final CloudRequestDTO cloudRequestDTO : dtos) {
			validateCloudRequestDTO(cloudRequestDTO, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_COLLECT_ACCESS_TYPES_SERVICE);
		}
		final CloudAccessListResponseDTO accessTypes = gatekeeperService.initAccessTypesCollection(dtos);
		logger.debug("collectNeighborCloudAccessTypes request successfully finished");
		return accessTypes;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_INIT_RELAY_TEST_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_INIT_RELAY_TEST_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INIT_RELAY_TEST_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_GATEWAY, message = POST_INIT_RELAY_TEST_HTTP_504_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_GATEKEEPER_RELAY_TEST_SERVICE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void initRelayTest(@RequestBody final QoSRelayTestProposalRequestDTO request) {
		logger.debug("new initRelayTest request received");
		
		validateQoSRelayTestProposalRequestDTO(request, CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_RELAY_TEST_SERVICE);
		gatekeeperService.initRelayTest(request);
		
		logger.debug("initRelayTest request successfully finished");
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_GET_CLOUD_DESCRIPTION, response = CloudWithRelaysResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_GET_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_GET_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_GATEKEEPER_GET_CLOUD_SERVICE + CommonConstants.OP_GATEKEEPER_GET_CLOUD_SERVICE_SUFFIX, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO getCloudByOperatorAndName(@PathVariable(value = PATH_VARIABLE_OPERATOR) final String operator,
																			  @PathVariable(value = PATH_VARIABLE_NAME) final String name) {
		logger.debug("New getCloudByOperatorAndName get request recieved with operator: '{}', and name: '{}'", operator, name);
		final String origin = CommonConstants.GATEKEEPER_URI + CommonConstants.OP_GATEKEEPER_GET_CLOUD_SERVICE;
		
		if (Utilities.isEmpty(operator)) {
			throw new BadPayloadException("Operator is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(name)) {
			throw new BadPayloadException("Name is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final Cloud cloud = gatekeeperDBService.getCloudByOperatorAndName(operator, name);
		final CloudWithRelaysResponseDTO response = DTOConverter.convertCloudToCloudWithRelaysResponseDTO(cloud);
		final List<Relay> publicGatewayRelays = gatekeeperDBService.getPublicGatewayRelays();

		for (final Relay relay : publicGatewayRelays) {
			response.getGatewayRelays().add(DTOConverter.convertRelayToRelayResponseDTO(relay));
		}
		
		logger.debug("Cloud entry with operator: '{}', and name: '{}' successfully retrieved", operator, name);
		
		return response;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private boolean isPortOutOfValidRange(final int port) {
		logger.debug("isPortOutOfValidRange started...");
		return port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequestDTO(final RelayRequestDTO dto, final String origin) {
		logger.debug("validateRelayRequestDTO started...");
		
		if (dto == null) {
			throw new BadPayloadException("RelayRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final boolean isAddressInvalid = Utilities.isEmpty(dto.getAddress());
		final boolean isPortInvalid = dto.getPort() == null || isPortOutOfValidRange(dto.getPort());
		final boolean isTypeInvalid = Utilities.convertStringToRelayType(dto.getType()) == null;
		final boolean isGatekeeperRelayAndExclusive = dto.isExclusive() && dto.getType().trim().equalsIgnoreCase(RelayType.GATEKEEPER_RELAY.toString());
		final boolean isGeneralRelayAndExclusive = dto.isExclusive() && dto.getType().trim().equalsIgnoreCase(RelayType.GENERAL_RELAY.toString());
		
		if (isAddressInvalid || isPortInvalid || isTypeInvalid || isGatekeeperRelayAndExclusive || isGeneralRelayAndExclusive) {
			String exceptionMsg = "RelayRequestDTO is invalid due to the following reasons:";
			exceptionMsg = isAddressInvalid ? exceptionMsg + " address is empty, " : exceptionMsg;
			exceptionMsg = isPortInvalid ? exceptionMsg + " port is null or should be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX  + "," :
										   exceptionMsg;
			exceptionMsg = isTypeInvalid ? exceptionMsg + " type '" + dto.getType() + "' is not valid," : exceptionMsg;
			exceptionMsg = isGatekeeperRelayAndExclusive ? exceptionMsg + " GATEKEEPER_REALY type couldn't be exclusive," : exceptionMsg;
			exceptionMsg = isGeneralRelayAndExclusive ? exceptionMsg + " GENERAL_REALY type couldn't be exclusive," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudRequestDTO(final CloudRequestDTO dto, final String origin) {
		logger.debug("validateRelayRequestDTO started...");
		
		if (dto == null) {
			throw new BadPayloadException("CloudRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final boolean isOperatorInvalid = Utilities.isEmpty(dto.getOperator());
		final boolean isOperatorIllFormed = !cnVerifier.isValid(dto.getOperator());
		final boolean isNameInvalid = Utilities.isEmpty(dto.getName());
		final boolean isNameIllFormed = !cnVerifier.isValid(dto.getName());
		
		if (isOperatorInvalid || isNameInvalid || isOperatorIllFormed || isNameIllFormed) {
			String exceptionMsg = "CloudRequestDTO is invalid due to the following reasons:";
			exceptionMsg = isOperatorInvalid ? exceptionMsg + " operator is empty," : exceptionMsg;
			exceptionMsg = isOperatorIllFormed ? exceptionMsg + " operator is in wrong format," : exceptionMsg;
			exceptionMsg = isNameInvalid ? exceptionMsg + " name is empty," : exceptionMsg;
			exceptionMsg = isNameIllFormed ? exceptionMsg + " name is in wrong format," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateGSDQueryFormDTO(final GSDQueryFormDTO gsdForm, final String origin) {
		logger.debug("validateGSDQueryFormDTO started...");
		
		if (gsdForm == null) {
			throw new BadPayloadException("GSDQueryFormDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (gsdForm.getRequestedService() == null) {
			throw new BadPayloadException("RequestedService is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(gsdForm.getRequestedService().getServiceDefinitionRequirement())) {
			throw new BadPayloadException("serviceDefinitionRequirement is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (gsdForm.getPreferredClouds() != null && !gsdForm.getPreferredClouds().isEmpty()) {
			for (final CloudRequestDTO cloudDTO : gsdForm.getPreferredClouds()) {
				validateCloudRequestDTO(cloudDTO, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateMultiGSDQueryFormDTO(final GSDMultiQueryFormDTO gsdForm, final String origin) {
		logger.debug("validateMultiGSDQueryFormDTO started...");
		
		if (gsdForm == null) {
			throw new BadPayloadException("GSDMultiQueryFormDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(gsdForm.getRequestedServices())) {
			throw new BadPayloadException("RequestedServices list is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final ServiceQueryFormDTO serviceReq : gsdForm.getRequestedServices()) {
			if (Utilities.isEmpty(serviceReq.getServiceDefinitionRequirement())) {
				throw new BadPayloadException("serviceDefinitionRequirement is empty", HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		if (gsdForm.getPreferredClouds() != null && !gsdForm.getPreferredClouds().isEmpty()) {
			for (final CloudRequestDTO cloudDTO : gsdForm.getPreferredClouds()) {
				validateCloudRequestDTO(cloudDTO, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void validateGSDPollRequestDTO(final GSDPollRequestDTO gsdPollRequest, final String origin) {
		logger.debug("validateGSDPollRequestDTO started...");
		
		if (gsdPollRequest == null) {
			throw new BadPayloadException("GSDPollRequestDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (gsdPollRequest.getRequestedService() == null) {
			throw new BadPayloadException("RequestedService is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(gsdPollRequest.getRequestedService().getServiceDefinitionRequirement())) {
			throw new BadPayloadException("serviceDefinitionRequirement is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (gsdPollRequest.getRequesterCloud() == null) {
			throw new BadPayloadException("RequesterCloud is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final boolean operatorIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getOperator());
		final boolean nameIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getName());
		final boolean authInfoIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getAuthenticationInfo());
		
		if (operatorIsEmpty || nameIsEmpty || authInfoIsEmpty) {
			String exceptionMsg = "GSDPollRequestDTO.CloudRequestDTO is invalid due to the following reasons:";
			exceptionMsg = operatorIsEmpty ? exceptionMsg + " operator is empty, " : exceptionMsg;
			exceptionMsg = nameIsEmpty ? exceptionMsg + " name is empty, " : exceptionMsg;
			exceptionMsg = authInfoIsEmpty ? exceptionMsg + " authInfo is empty, " : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateICNRequestFormDTO(final ICNRequestFormDTO icnForm, final String origin) {
		logger.debug("validateICNRequestFormDTO started...");
		
		if (icnForm == null) {
			throw new BadPayloadException("ICN form is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (icnForm.getRequestedService() == null) {
			throw new BadPayloadException("Requested service is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(icnForm.getRequestedService().getServiceDefinitionRequirement())) {
			throw new BadPayloadException("Requested service definition is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		validateId(icnForm.getTargetCloudId(), origin);
		validateSystemRequestDTO(icnForm.getRequesterSystem(), origin);
		
		for (final SystemRequestDTO preferredSystem : icnForm.getPreferredSystems()) {
			validateSystemRequestDTO(preferredSystem, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateId(final Long id, final String origin) {
		if (id == null || id < 1) {
			throw new BadPayloadException("Invalid id: " + id, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateSystemRequestDTO(final SystemRequestDTO system, final String origin) {
		logger.debug("validateSystemRequestDTO started...");
		
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
			throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
										  HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSRelayTestProposalRequestDTO(final QoSRelayTestProposalRequestDTO request, final String origin) {
		logger.debug("validateQoSRelayTestProposalRequestDTO started...");
		
		if (request == null) {
			throw new BadPayloadException("Relay test proposal is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		validateCloudRequestDTO(request.getTargetCloud(), origin);
		validateRelayRequestDTO(request.getRelay(), origin);
		
		final RelayType relayType = Utilities.convertStringToRelayType(request.getRelay().getType());
		if (RelayType.GATEKEEPER_RELAY == relayType) {
			throw new BadPayloadException("Invalid relay type for testing: " + RelayType.GATEKEEPER_RELAY, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}