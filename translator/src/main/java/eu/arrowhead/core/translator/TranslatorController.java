package eu.arrowhead.core.translator;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.translator.services.fiware.FiwareService;
import eu.arrowhead.core.translator.services.fiware.common.FiwareEntity;
import eu.arrowhead.core.translator.services.fiware.common.FiwareUrlServices;
import eu.arrowhead.core.translator.services.translator.TranslatorService;
import eu.arrowhead.core.translator.services.translator.common.TranslatorSetup;
import eu.arrowhead.core.translator.services.translator.common.TranslatorHubAccess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.TRANSLATOR_URI)
public class TranslatorController {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(TranslatorController.class);

    private static final String MEDIA_TYPE_APPLICATION_SENML = "application/senml+json";
    
    private static final String TRANSLATOR_FIWARE_SERVICES_URL_ENTITIES = "/translator/v2/entities";
    private static final String TRANSLATOR_FIWARE_SERVICES_URL_TYPES = "/translator/v2/types";

    private static final String PATH_VARIABLE_ID = "id";
    private static final String PATH_ENTITY_ID = "entityId";
    private static final String PATH_ENTITY_TYPE = "entityType";
    private static final String PATH_ATTRIBUTE_NAME = "attrName";
    private static final String PATH_SERVICE_NAME = "serviceName";

    private static final String PATH_TRANSLATOR_ROOT = "/";
    private static final String PATH_TRANSLATOR_ALL = "/all";
    private static final String PATH_TRANSLATOR_BY_ID = "/{" + PATH_VARIABLE_ID + "}";

    private static final String PATH_TRANSLATOR_PLUGIN_ENTITY_AND_SERVICE = "/plugin/service/{" + PATH_ENTITY_ID + "}/{" + PATH_SERVICE_NAME + "}";

    private static final String PATH_TRANSLATOR_FIWARE_ROOT = "/v2";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES = PATH_TRANSLATOR_FIWARE_ROOT + "/entities";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID = PATH_TRANSLATOR_FIWARE_ROOT + "/entities/{" + PATH_ENTITY_ID + "}";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID = PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID + "/attrs";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID_AND_ATTRIBUTE = PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID + "/{" + PATH_ATTRIBUTE_NAME + "}";
    private static final String PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTE_VALUE_BY_ID_AND_ATTRIBUTE = PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID_AND_ATTRIBUTE + "/value";
    private static final String PATH_TRANSLATOR_FIWARE_TYPES = PATH_TRANSLATOR_FIWARE_ROOT + "/types";
    private static final String PATH_TRANSLATOR_FIWARE_TYPES_BY_TYPE = PATH_TRANSLATOR_FIWARE_TYPES + "/{" + PATH_ENTITY_TYPE + "}";
    private static final String PATH_TRANSLATOR_FIWARE_NOTIFICATION = PATH_TRANSLATOR_FIWARE_ROOT + "/notification";

    @Autowired
    private FiwareService fiwareService;
    @Autowired
    private TranslatorService translatorService;

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(CommonConstants.ECHO_URI)
    @ResponseBody
    public String echoService() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Simple test method to see if the http server where this resource is registered works or not", response = String.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_ROOT, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getIt() {
        return "This is the Translator Arrowhead Core System";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "This method initiates the creation of a new translation hub, if none exists already, between two systems.", response = TranslatorHubAccess.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = PATH_TRANSLATOR_ROOT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TranslatorHubAccess postTranslator(@RequestBody final TranslatorSetup setup, @RequestHeader("host") String hostPort) {
        String host = hostPort.contains(":")?hostPort.substring(0, hostPort.indexOf(":")):hostPort;

        try {
            return translatorService.createTranslationHub(setup,host);
        } catch (Exception ex) {
            logger.warn("Exception: " + ex.getLocalizedMessage());
            throw new ArrowheadException(ex.getLocalizedMessage(), HttpStatus.SC_BAD_REQUEST);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to check all active hubs", response = ArrayList.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_ALL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayList<TranslatorHubAccess> getTranslatorList(@RequestHeader("host") String hostPort) {
        String host = hostPort.contains(":")?hostPort.substring(0, hostPort.indexOf(":")):hostPort;
        return translatorService.getAllHubs(host);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to check for a specific hub provided his translatorId", response = TranslatorHubAccess.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TranslatorHubAccess getTranslator(@RequestHeader("host") String hostPort, @PathVariable(value = PATH_VARIABLE_ID) final int translatorId) {
        String host = hostPort.contains(":")?hostPort.substring(0, hostPort.indexOf(":")):hostPort;
        try {
            return translatorService.getHub(translatorId, host);
        } catch (Exception ex) {
            logger.warn("Exception: " + ex.getLocalizedMessage());
            throw new ArrowheadException(ex.getLocalizedMessage(), HttpStatus.SC_BAD_REQUEST);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Public method to get Service from a System @ Translator-Plugin", response = Object.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, message = CoreCommonConstants.SWAGGER_HTTP_415_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_PLUGIN_ENTITY_AND_SERVICE, produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE, MEDIA_TYPE_APPLICATION_SENML})
    @ResponseBody
    public Object pluginGetEntityValue(
            HttpServletRequest request,
            @PathVariable(value = PATH_ENTITY_ID) final String entityId,
            @PathVariable(value = PATH_SERVICE_NAME) final String serviceName
    ) {
        if (request.getHeader(HttpHeaders.ACCEPT) == null) throw new ArrowheadException("No ACCEPT header", HttpStatus.SC_BAD_REQUEST);
        return fiwareService.pluginEntityService(entityId, serviceName, request.getHeader(HttpHeaders.ACCEPT));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE endpoints", response = FiwareUrlServices.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FiwareUrlServices fiwareGetIt() {
        return new FiwareUrlServices(TRANSLATOR_FIWARE_SERVICES_URL_ENTITIES, TRANSLATOR_FIWARE_SERVICES_URL_TYPES, null, null);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE list Entities", response = FiwareEntity[].class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayList<FiwareEntity> fiwareListEntities(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String idPattern,
            @RequestParam(required = false) String typePattern,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String mq,
            @RequestParam(required = false) String georel,
            @RequestParam(required = false) String geometry,
            @RequestParam(required = false) String coords,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String attrs,
            @RequestParam(required = false) String metadata,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) Map<String, Object> allRequestParams
    ) {
        List<FiwareEntity> fiwareResponse = Arrays.asList(fiwareService.listEntities(allRequestParams));
        ArrayList<FiwareEntity> ahResponse = fiwareService.getArrowheadServices(id, type);

        ahResponse.addAll(fiwareResponse);
        
        return ahResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE create Entity", tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_CREATED, message = ""),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY, message = ""),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void fiwareCreateEntity(
            HttpServletResponse response,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) Map<String, Object> allRequestParams,
            @RequestBody final FiwareEntity entity
    ) {
        response.setStatus(fiwareService.createEntity(allRequestParams, entity));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE retrieve Entity", response = FiwareEntity.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FiwareEntity fiwareRetrieveEntity(
            @PathVariable(value = PATH_ENTITY_ID) final String entityId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String attrs,
            @RequestParam(required = false) String metadata,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) Map<String, Object> allRequestParams
    ) {
        return fiwareService.queryEntity(entityId, allRequestParams);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE retrieve Entity Attributes", response = Object.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object fiwareRetrieveEntityAttributes(
            @PathVariable(value = PATH_ENTITY_ID) final String entityId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String attrs,
            @RequestParam(required = false) String metadata,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) Map<String, Object> allRequestParams
    ) {
        return fiwareService.retrieveEntityAttributes(entityId, allRequestParams);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE update/append Entity Attributes", tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_ATTRIBUTES_BY_ID, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void fiwareupdateAppendEntityAttributes(
            HttpServletResponse response,
            @PathVariable(value = PATH_ENTITY_ID) final String entityId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String attrs,
            @RequestParam(required = false) String metadata,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) Map<String, Object> allRequestParams,
            @RequestBody final Object attributes
    ) {
        response.setStatus(fiwareService.updateOrAppendEntityAttributes(entityId, allRequestParams, attributes));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE remove Entity", tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = PATH_TRANSLATOR_FIWARE_ENTITIES_BY_ID)
    @ResponseBody
    public void fiwareRemoveEntity(
            HttpServletResponse response,
            @PathVariable(value = PATH_ENTITY_ID) final String entityId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Map<String, Object> allRequestParams
    ) {
        response.setStatus(fiwareService.removeEntity(entityId, allRequestParams));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE list Entity Types", response = Object[].class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_TYPES, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object[] fiwareListEntityTypes(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String options,
            @RequestParam(required = false) Map<String, Object> allRequestParams
    ) {
        return fiwareService.queryTypesList(allRequestParams);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "FIWARE Retrieve Entity Type", response = Object.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PATH_TRANSLATOR_FIWARE_TYPES_BY_TYPE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object fiwareRetrieveEntityType(
            @PathVariable(value = PATH_ENTITY_TYPE) final String entityType
    ) {
        return fiwareService.retrieveEntityType(entityType);
    }

    //=================================================================================================
    // assistant methods
    //-------------------------------------------------------------------------------------------------
}
