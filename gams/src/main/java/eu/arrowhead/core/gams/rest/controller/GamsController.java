package eu.arrowhead.core.gams.rest.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.core.gams.Constants;
import eu.arrowhead.core.gams.RestValidation;
import eu.arrowhead.core.gams.rest.dto.CreateInstanceRequest;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceDto;
import eu.arrowhead.core.gams.service.InstanceService;
import eu.arrowhead.core.gams.utility.Converter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.GAMS_URI)
public class GamsController {

    //=================================================================================================
    // members

    private static final String CREATE_INSTANCE_URI = Constants.PATH_ROOT;
    private static final String CREATE_INSTANCE_DESCRIPTION = "Create new GAMS instance";
    private static final String CREATE_INSTANCE_SUCCESS = "New GAMS instance created";
    private static final String CREATE_INSTANCE_CONFLICT = "GAMS instance exists already";
    private static final String CREATE_INSTANCE_BAD_REQUEST = "Unable to create new GAMS instance";

    private final Logger logger = LogManager.getLogger(GamsController.class);
    private final RestValidation validation = new RestValidation();

    private final InstanceService instanceService;

    @Autowired
    public GamsController(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core device availability", response = String.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echo() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_INSTANCE_DESCRIPTION, response = GamsInstanceDto.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_INSTANCE_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_CONFLICT, message = CREATE_INSTANCE_CONFLICT, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_INSTANCE_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_INSTANCE_URI)
    @ResponseBody
    public GamsInstanceDto create(@RequestBody final CreateInstanceRequest createInstanceRequest) {
        logger.debug("create started ...");
        validation.verify(createInstanceRequest, createOrigin(CREATE_INSTANCE_URI));
        final GamsInstance instance = instanceService.create(createInstanceRequest);

        return Converter.convert(instance);
    }

    //-------------------------------------------------------------------------------------------------
    private String createOrigin(final String path) {
        return CommonConstants.GAMS_URI + path;
    }
}