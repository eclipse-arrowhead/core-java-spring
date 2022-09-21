package eu.arrowhead.core.translator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.translator.services.fiware.FiwareService;
import eu.arrowhead.core.translator.services.fiware.common.FiwareEntity;
import eu.arrowhead.core.translator.services.fiware.common.FiwareUrlServices;
import eu.arrowhead.core.translator.services.translator.TranslatorService;
import eu.arrowhead.core.translator.services.translator.common.TranslatorHubDTO;
import eu.arrowhead.core.translator.services.translator.common.TranslatorSetupDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, allowedHeaders = {
                HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION })
@RestController
@RequestMapping(CommonConstants.TRANSLATOR_URI)
public class TranslatorController {

    // =================================================================================================
    // members
	
    private final Logger logger = LogManager.getLogger(TranslatorController.class);

    private static final String MEDIA_TYPE_APPLICATION_SENML = "application/senml+json";

    private static final String TRANSLATOR_FIWARE_SERVICES_URL_ENTITIES = "/translator/v2/entities";
    private static final String TRANSLATOR_FIWARE_SERVICES_URL_TYPES = "/translator/v2/types";

    private static final String PATH_VARIABLE_ID = "id";
    private static final String PATH_ENTITY_ID = "entityId";
    private static final String PATH_ENTITY_TYPE = "entityType";
    private static final String PATH_SERVICE_NAME = "serviceName";

    private static final String PATH_NEW_TRANSLATOR_ROOT = "/";
    private static final String PATH_NEW_TRANSLATOR_BY_ID = PATH_NEW_TRANSLATOR_ROOT + "{" + PATH_VARIABLE_ID + "}";
    private static final String PATH_NEW_TRANSLATOR_ALL = PATH_NEW_TRANSLATOR_ROOT + "all";

    private static final String PATH_TRANSLATOR_PLUGIN_ENTITY_AND_SERVICE = "/plugin/service/{" + PATH_ENTITY_ID
                    + "}/{" + PATH_SERVICE_NAME + "}";

    private static final String PATH_TRANSLATOR_FIWARE_ROOT = "/v2";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES = PATH_TRANSLATOR_FIWARE_ROOT + "/entities";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID = PATH_TRANSLATOR_FIWARE_ROOT + "/entities/{"
                    + PATH_ENTITY_ID + "}";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID = PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID
                    + "/attrs";
    private static final String PATH_TRANSLATOR_FIWARE_TYPES = PATH_TRANSLATOR_FIWARE_ROOT + "/types";
    private static final String PATH_TRANSLATOR_FIWARE_TYPES_BY_TYPE = PATH_TRANSLATOR_FIWARE_TYPES + "/{"
                    + PATH_ENTITY_TYPE + "}";

    @Autowired
    private FiwareService fiwareService;;
    
    @Autowired
    private TranslatorService translatorService;
    
    @Autowired
    private CommonDBService commonDBService;


    // =================================================================================================
    // methods

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(CommonConstants.ECHO_URI)
    @ResponseBody
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
				
		final String origin = CommonConstants.TRANSLATOR_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.TRANSLATOR, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Simple test method to see if the http server where this resource is registered works or not", response = String.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_NEW_TRANSLATOR_ROOT, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getTranslatorIt() {
            return "This is the new Translator Arrowhead Core System";
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "This method initiates the creation of a new translation hub, if none exists already, between two systems.", response = TranslatorHubDTO.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_CONFLICT, message = CoreCommonConstants.SWAGGER_HTTP_409_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @PostMapping(path = PATH_NEW_TRANSLATOR_ROOT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public TranslatorHubDTO postTranslator(@RequestBody final TranslatorSetupDTO setup,
                    final HttpServletRequest request) {
            return translatorService.createTranslatorHub(setup);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to check for a specific hub provided his translatorId", response = TranslatorHubDTO.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_CONFLICT, message = CoreCommonConstants.SWAGGER_HTTP_409_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_NEW_TRANSLATOR_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TranslatorHubDTO getTranslatorHub2(@PathVariable(value = PATH_VARIABLE_ID) final int translatorId) {
            return translatorService.getTranslatorHubDTO(translatorId);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to remove a hub", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @DeleteMapping(path = PATH_NEW_TRANSLATOR_BY_ID)
    @ResponseStatus(org.springframework.http.HttpStatus.ACCEPTED)
    @ResponseBody
    public void removeTrasnlatorHub2(@PathVariable(value = PATH_VARIABLE_ID) final int translatorId) {
            translatorService.deleteHub(translatorId);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to check all active hubs", response = ArrayList.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_NEW_TRANSLATOR_ALL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayList<TranslatorHubDTO> getHubsList() {
            return translatorService.getHubsList();
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to get Service from a System @ Translator-Plugin", response = Object.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, message = CoreCommonConstants.SWAGGER_HTTP_415_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_PLUGIN_ENTITY_AND_SERVICE, produces = { MediaType.TEXT_PLAIN_VALUE,
                    MediaType.APPLICATION_JSON_VALUE, MEDIA_TYPE_APPLICATION_SENML })
    @ResponseBody
    public Object pluginGetEntityValue(final HttpServletRequest request,
                    @PathVariable(value = PATH_ENTITY_ID) final String entityId,
                    @PathVariable(value = PATH_SERVICE_NAME) final String serviceName) {
            if (request.getHeader(HttpHeaders.ACCEPT) == null)
                    throw new ArrowheadException("No ACCEPT header", HttpStatus.SC_BAD_REQUEST);
            return fiwareService.pluginEntityService(entityId, serviceName, request.getHeader(HttpHeaders.ACCEPT));
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE endpoints", response = FiwareUrlServices.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FiwareUrlServices fiwareGetIt() {
            return new FiwareUrlServices(TRANSLATOR_FIWARE_SERVICES_URL_ENTITIES,
                            TRANSLATOR_FIWARE_SERVICES_URL_TYPES, null, null);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE list Entities", response = FiwareEntity[].class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayList<FiwareEntity> fiwareListEntities(@RequestParam(required = false) final String id,
                    @RequestParam(required = false) final String type, @RequestParam(required = false) final String idPattern,
                    @RequestParam(required = false) final String typePattern, @RequestParam(required = false) final String q,
                    @RequestParam(required = false) final String mq, @RequestParam(required = false) final String georel,
                    @RequestParam(required = false) final String geometry, @RequestParam(required = false) final String coords,
                    @RequestParam(required = false) final Integer limit, @RequestParam(required = false) final Integer offset,
                    @RequestParam(required = false) final String attrs, @RequestParam(required = false) final String metadata,
                    @RequestParam(required = false) final String orderBy, @RequestParam(required = false) final String options,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams) {
            final List<FiwareEntity> fiwareResponse = Arrays.asList(fiwareService.listEntities(allRequestParams));
            final ArrayList<FiwareEntity> ahResponse = fiwareService.getArrowheadServices(id, type);

            ahResponse.addAll(fiwareResponse);

            return ahResponse;
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE create Entity", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = { @ApiResponse(code = HttpStatus.SC_CREATED, message = ""),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY, message = ""),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @PostMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void fiwareCreateEntity(final HttpServletResponse response, @RequestParam(required = false) final String options,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams,
                    @RequestBody final FiwareEntity entity) {
            response.setStatus(fiwareService.createEntity(allRequestParams, entity));
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE retrieve Entity", response = FiwareEntity.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FiwareEntity fiwareRetrieveEntity(@PathVariable(value = PATH_ENTITY_ID) final String entityId,
                    @RequestParam(required = false) final String type, @RequestParam(required = false) final String attrs,
                    @RequestParam(required = false) final String metadata, @RequestParam(required = false) final String options,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams) {
            return fiwareService.queryEntity(entityId, allRequestParams);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE retrieve Entity Attributes", response = Object.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object fiwareRetrieveEntityAttributes(@PathVariable(value = PATH_ENTITY_ID) final String entityId,
                    @RequestParam(required = false) final String type, @RequestParam(required = false) final String attrs,
                    @RequestParam(required = false) final String metadata, @RequestParam(required = false) final String options,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams) {
            return fiwareService.retrieveEntityAttributes(entityId, allRequestParams);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE update/append Entity Attributes", tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @PostMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void fiwareupdateAppendEntityAttributes(final HttpServletResponse response,
                    @PathVariable(value = PATH_ENTITY_ID) final String entityId,
                    @RequestParam(required = false) final String type, @RequestParam(required = false) final String attrs,
                    @RequestParam(required = false) final String metadata, @RequestParam(required = false) final String options,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams,
                    @RequestBody final Object attributes) {
            response.setStatus(
                            fiwareService.updateOrAppendEntityAttributes(entityId, allRequestParams, attributes));
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE remove Entity", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @DeleteMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID)
    @ResponseBody
    public void fiwareRemoveEntity(final HttpServletResponse response,
                    @PathVariable(value = PATH_ENTITY_ID) final String entityId,
                    @RequestParam(required = false) final String type,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams) {
            response.setStatus(fiwareService.removeEntity(entityId, allRequestParams));
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE list Entity Types", response = Object[].class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_TYPES, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object[] fiwareListEntityTypes(@RequestParam(required = false) final Integer limit,
                    @RequestParam(required = false) final Integer offset, @RequestParam(required = false) final String options,
                    @RequestParam(required = false) final Map<String, Object> allRequestParams) {
            return fiwareService.queryTypesList(allRequestParams);
    }

    // -------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE Retrieve Entity Type", response = Object.class, tags = {
                    CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_TYPES_BY_TYPE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object fiwareRetrieveEntityType(@PathVariable(value = PATH_ENTITY_TYPE) final String entityType) {
            return fiwareService.retrieveEntityType(entityType);
    }

    // =================================================================================================
    // assistant methods
    // -------------------------------------------------------------------------------------------------
}
