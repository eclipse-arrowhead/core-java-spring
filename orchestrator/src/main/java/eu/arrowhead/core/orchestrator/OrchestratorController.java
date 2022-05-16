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

package eu.arrowhead.core.orchestrator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleRequestDTO;
import eu.arrowhead.common.dto.internal.QoSReservationListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreFlexibleDBService;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.ORCHESTRATOR_URI)
public class OrchestratorController {

	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String OP_ORCH_PROCESS_BY_ID = CommonConstants.OP_ORCH_PROCESS_URI + "/{" + PATH_VARIABLE_ID + "}";
	
	private static final String GET_ORCHESTRATOR_HTTP_200_MESSAGE = "Orchestration by consumer system id returned";
	private static final String GET_ORCHESTRATOR_HTTP_400_MESSAGE = "Could not orchestrate by requested consumer system id";
	private static final String POST_ORCHESTRATIOR_DESCRIPTION = "Start Orchestration process.";
	private static final String POST_ORCHESTRATOR_HTTP_200_MESSAGE = "Returns possible providers of the specified service.";
	private static final String POST_ORCHESTRATOR_HTTP_400_MESSAGE = "Could not run the orchestration process";
	private static final String POST_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_201_MESSAGE = "Created flexible store rules returned.";
	private static final String POST_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_400_MESSAGE = "Could not create flexible store rules.";
	private static final String DELETE_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_200_MESSAGE = "Flexible store rule(s) removed";
	private static final String DELETE_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_400_MESSAGE = "Could not remove flexible store rule(s)";
	private static final String GET_ORCHESTRATOR_QOS_ENABLED_HTTP_200_MESSAGE = "QoS Monitor flag returned";
	private static final String GET_ORCHESTRATOR_QOS_RESERVATIONS_HTTP_200_MESSAGE = "QoS Reservations returned";
	private static final String POST_ORCHESTRATOR_QOS_TEMPORARY_LOCK_HTTP_200_MESSAGE = "Locked Orchestration results (Provider-Service) returned";
	private static final String POST_ORCHESTRATOR_QOS_TEMPORARY_LOCK_HTTP_400_MESSAGE = "Could not return locked Orchestration results (Provider-Service)";
	private static final String POST_ORCHESTRATOR_QOS_CONFIRM_RESERVATION_HTTP_200_MESSAGE = "QoS Reservation request confirmed";
	private static final String POST_ORCHESTRATOR_QOS_CONFIRM_RESERVATION_HTTP_400_MESSAGE = "Could not confirm QoS Reservation request";
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE = " can not be served. Orchestrator runs in NO GATEKEEPER mode.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";
	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE = "Service definition has invalid format. Service definition only contains letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";
	
	private final Logger logger = LogManager.getLogger(OrchestratorController.class);
	
	@Value(CoreCommonConstants.$ORCHESTRATOR_USE_FLEXIBLE_STORE_WD)
	private boolean useFlexibleStore;
	
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;
	
	@Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
	private boolean gatekeeperIsPresent;
	
	@Value(CoreCommonConstants.$QOS_ENABLED_WD)
	private boolean qosEnabled;
	
	@Autowired
	private OrchestratorService orchestratorService;
	
	@Autowired
	private OrchestratorStoreFlexibleDBService orchestratorStoreFlexibleDBService;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;
	
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
		logger.debug("New getLogEntries GET request recevied with page: {} and item_per page: {}", page, size);
				
		final String origin = CommonConstants.ORCHESTRATOR_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.ORCHESTRATOR, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_ORCHESTRATIOR_DESCRIPTION, response = OrchestrationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_ORCH_PROCESS_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestrationResponseDTO orchestrationProcess(@RequestBody final OrchestrationFormRequestDTO request) {
		logger.debug("orchestrationProcess started ...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_URI;
		checkOrchestratorFormRequestDTO(request, origin);
		
	    if (request.getOrchestrationFlags().getOrDefault(Flag.EXTERNAL_SERVICE_REQUEST, false)) {
	    	if (!gatekeeperIsPresent) {
	    		throw new BadPayloadException("External service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
	    	
	    	return orchestratorService.externalServiceRequest(request);
	    } else if (request.getOrchestrationFlags().getOrDefault(Flag.TRIGGER_INTER_CLOUD, false)) {
	    	if (!gatekeeperIsPresent) {
	    		throw new BadPayloadException("Forced inter cloud service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
	    	
	    	return orchestratorService.triggerInterCloud(request);
	    } else if (!request.getOrchestrationFlags().getOrDefault(Flag.OVERRIDE_STORE, false)) { // overrideStore == false
	    	return orchestratorService.orchestrationFromStore(request);
	    } else {
	    	return orchestratorService.dynamicOrchestration(request, false);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_ORCHESTRATIOR_DESCRIPTION, response = OrchestrationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_ORCH_PROCESS_BY_PROXY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestrationResponseDTO orchestrationProcessByProxy(@RequestBody final OrchestrationFormRequestDTO request) { 
		logger.debug("orchestrationProcess started ...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS_BY_PROXY_URI;
		checkOrchestratorFormRequestDTO(request, origin);
		
	    if (request.getOrchestrationFlags().getOrDefault(Flag.TRIGGER_INTER_CLOUD, false)) {
	    	if (!gatekeeperIsPresent) {
	    		throw new BadPayloadException("Forced inter cloud service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
	    	
	    	return orchestratorService.triggerInterCloud(request);
	    } else if (!request.getOrchestrationFlags().getOrDefault(Flag.OVERRIDE_STORE, false)) { // overrideStore == false
	    	return orchestratorService.orchestrationFromStore(request);
	    } else {
	    	return orchestratorService.dynamicOrchestration(request, true);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Start ochestration process from the ochestrator store based on consumer system id.", response = OrchestrationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ORCHESTRATOR_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = OP_ORCH_PROCESS_BY_ID , produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestrationResponseDTO storeOrchestrationProcess(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
		logger.debug("storeOrchestrationProcess started ...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + OP_ORCH_PROCESS_BY_ID;
		
		if (useFlexibleStore) {
			throw new BadPayloadException("Orchestrator use flexible store!", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
    	if (systemId < 1) {
    		throw new BadPayloadException("Consumer system : " + ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return orchestratorService.storeOchestrationProcessResponse(systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Creates the given flexible store rules", response = OrchestratorStoreFlexibleListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreFlexibleListResponseDTO createFlexibleStoreRule(@RequestBody final List<OrchestratorStoreFlexibleRequestDTO> requestList) {
		logger.debug("createFlexibleStoreRule started ...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_CREATE_FLEXIBLE_STORE_RULES_URI;
		if (!useFlexibleStore) {
			throw new BadPayloadException("Orchestrator don't use flexible store!", HttpStatus.SC_BAD_REQUEST, origin);
		}
		checkOrchestratorStoreFlexibleRequestDTOList(requestList, origin);
		
		return orchestratorStoreFlexibleDBService.createOrchestratorStoreFlexibleResponse(requestList);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove flexible store rule by id", tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CommonConstants.OP_ORCH_REMOVE_FLEXIBLE_STORE_RULE_URI)
	public void removeFlexibleStoreRuleById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("removeFlexibleStoreRuleById started...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_REMOVE_FLEXIBLE_STORE_RULE_URI;
		if (!useFlexibleStore) {
			throw new BadPayloadException("Orchestrator don't use flexible store!", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		orchestratorStoreFlexibleDBService.deleteOrchestratorStoreFlexibleById(id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove all flexible store rule", tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_ORCHESTRATOR_FLEXIBLE_STORE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CommonConstants.OP_ORCH_CLEAN_FLEXIBLE_STORE_URI)
	public void cleanFlexibleStore() {
		logger.debug("removeFlexibleStoreRuleById started...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_REMOVE_FLEXIBLE_STORE_RULE_URI;
		if (!useFlexibleStore) {
			throw new BadPayloadException("Orchestrator don't use flexible store!", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		orchestratorStoreFlexibleDBService.deleteAllOrchestratorStoreFlexible();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return QoS Monitor enabled flag.", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_QOS_ENABLED_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_ORCH_QOS_ENABLED_URI)
	public String isQoSEnabled() {
		logger.debug("isQoSEnabled started ...");
		return String.valueOf(qosEnabled);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return all QoSManager reservation entry.", response = QoSReservationListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_QOS_RESERVATIONS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSReservationListResponseDTO getAllQoSReservation() {
		logger.debug("getAllQoSReservation started ...");
		return orchestratorService.getAllQoSReservationResponse();
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return locked Orchestration results (Provider-Service).", response = QoSTemporaryLockResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_QOS_CONFIRM_RESERVATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_QOS_CONFIRM_RESERVATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public QoSTemporaryLockResponseDTO lockProvidersTemporary(@RequestBody final QoSTemporaryLockRequestDTO request) {
		logger.debug("lockProvidersTemporary started ...");
		
		checkQoSReservationRequestDTO(request, CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_TEMPORARY_LOCK_URI);
		return orchestratorService.lockProvidersTemporarily(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Confirm reservation request (Provider-Service).", tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_QOS_TEMPORARY_LOCK_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_QOS_TEMPORARY_LOCK_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void confirmReservation(@RequestBody final QoSReservationRequestDTO request) {
		logger.debug("confirmReservation started ...");
		
		checkQoSReservationRequestDTO(request, CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_QOS_RESERVATIONS_URI);
		orchestratorService.confirmProviderReservation(request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorFormRequestDTO(final OrchestrationFormRequestDTO request, final String origin) {
		logger.debug("checkOrchestratorFormRequestDTO started...");
		
		if (request == null) {
			throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		request.validateCrossParameterConstraints();

		// Requester system
		checkSystemRequestDTO(request.getRequesterSystem(), origin);

		// Requester cloud
		if (request.getRequesterCloud() != null) {
			checkCloudRequestDTO(request.getRequesterCloud(), origin);
		}
		
		// Requested service
		if (request.getRequestedService() != null && Utilities.isEmpty(request.getRequestedService().getServiceDefinitionRequirement())) {
			throw new BadPayloadException("Requested service definition requirement" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		// Preferred Providers
		if (request.getPreferredProviders() != null) {
			for (final PreferredProviderDataDTO provider : request.getPreferredProviders()) {
				checkSystemRequestDTO(provider.getProviderSystem(), origin);
				if (provider.getProviderCloud() != null) {
					checkCloudRequestDTO(provider.getProviderCloud(), origin);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorStoreFlexibleRequestDTOList(final List<OrchestratorStoreFlexibleRequestDTO> dtoList, final String origin) {
		logger.debug("checkOrchestratorStoreFlexibleRequestDTOList started...");
		
		if (dtoList == null) {
			throw new BadPayloadException("Request list" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final OrchestratorStoreFlexibleRequestDTO dto : dtoList) {
			if (dto == null) {
				throw new BadPayloadException("Request list contains null element", HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (dto.getConsumerSystem() == null) {
				throw new BadPayloadException("Request list contains an element without consumer system describer", HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				if (Utilities.isEmpty(dto.getConsumerSystem().getSystemName()) && Utilities.isEmpty(dto.getConsumerSystem().getMetadata())) {
					throw new BadPayloadException("Request list contains an element in which consumerSystemName and consumerSystemMetadata are both empty", HttpStatus.SC_BAD_REQUEST, origin);
				}
				if (!Utilities.isEmpty(dto.getConsumerSystem().getSystemName()) && !cnVerifier.isValid(dto.getConsumerSystem().getSystemName())) {
					throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
				}
			}
			
			if (dto.getProviderSystem() == null) {
				throw new BadPayloadException("Request list contains an element without provider system describer", HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				if (Utilities.isEmpty(dto.getProviderSystem().getSystemName()) && Utilities.isEmpty(dto.getProviderSystem().getMetadata())) {
					throw new BadPayloadException("Request list contains an element in which providerSystemName and consumerSystemMetadata are both empty", HttpStatus.SC_BAD_REQUEST, origin);
				}
				if (!Utilities.isEmpty(dto.getProviderSystem().getSystemName()) && !cnVerifier.isValid(dto.getProviderSystem().getSystemName())) {
					throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
				}
			}
			
			if (Utilities.isEmpty(dto.getServiceDefinitionName())) {
				throw new BadPayloadException("Request list contains an element without serviceDefinition", HttpStatus.SC_BAD_REQUEST, origin);
			} else if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(dto.getServiceDefinitionName())) {
				throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (!Utilities.isEmpty(dto.getServiceInterfaceName()) && !interfaceNameVerifier.isValid(dto.getServiceInterfaceName())) {
				throw new BadPayloadException("Specified interface name is not valid: " + dto.getServiceInterfaceName(), HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (dto.getPriority() != null && dto.getPriority() <= 0) {
				throw new BadPayloadException("Priority must be a positive number", HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequestDTO(final SystemRequestDTO system, final String origin) {
		logger.debug("checkSystemRequestDTO started...");
		
		if (system == null) {
			throw new BadPayloadException("System" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new BadPayloadException("System name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new BadPayloadException("System address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (system.getPort() == null) {
			throw new BadPayloadException("System port" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST,
										  origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkCloudRequestDTO(final CloudRequestDTO cloud, final String origin) {
		logger.debug("checkCloudRequestDTO started...");
		
		if (cloud == null) {
			throw new BadPayloadException("Cloud" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new BadPayloadException("Cloud operator" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new BadPayloadException("Cloud name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkQoSReservationRequestDTO(final QoSTemporaryLockRequestDTO request, final String origin) {
		logger.debug("checkQoSReservationRequestDTO started...");
		
		if (request == null) {
			throw new BadPayloadException("QoSReservationRequestDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getRequester() == null) {
			throw new BadPayloadException("Requester system is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getRequester().getSystemName())) {
			throw new BadPayloadException("Requester system name is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getRequester().getAddress())) {
			throw new BadPayloadException("Requester system address is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getRequester().getPort() == null) {
			throw new BadPayloadException("Requester system port is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request instanceof QoSReservationRequestDTO) {
			final QoSReservationRequestDTO req = (QoSReservationRequestDTO) request;
			if (req.getSelected() == null) {
				throw new BadPayloadException("Selected ORCH result is null", HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (req.getSelected().getProvider() == null) {
				throw new BadPayloadException("Selected provider is null", HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (req.getSelected().getService() == null) {
				throw new BadPayloadException("Selected service is null", HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
}