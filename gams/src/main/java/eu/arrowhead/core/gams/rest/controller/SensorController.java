package eu.arrowhead.core.gams.rest.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.core.gams.Validation;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.InstanceDto;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import eu.arrowhead.core.gams.service.SensorService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.core.gams.Constants.PATH_PARAMETER_SENSOR;
import static eu.arrowhead.core.gams.Constants.PATH_PARAMETER_UID;
import static eu.arrowhead.core.gams.Constants.PATH_ROOT;
import static eu.arrowhead.core.gams.Constants.PATH_SENSOR;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.GAMS_URI + PATH_PARAMETER_UID)
public class SensorController {

    //=================================================================================================
    // members
    private static final String QUALIFY_SENSOR_URI = PATH_SENSOR + PATH_PARAMETER_SENSOR;

    private static final String CREATE_SENSOR_URI = PATH_ROOT;
    private static final String CREATE_SENSOR_DESCRIPTION = "Register a sensor to an instance";
    private static final String CREATE_SENSOR_SUCCESS = "New sensor registered";
    private static final String CREATE_SENSOR_CONFLICT = "Sensor registered to gams instance already";
    private static final String CREATE_SENSOR_BAD_REQUEST = "Unable to register sensor";

    private static final String PUBLISH_SENSOR_URI = QUALIFY_SENSOR_URI;
    private static final String PUBLISH_SENSOR_DESCRIPTION = "Publish sensor data to an instance";
    private static final String PUBLISH_SENSOR_SUCCESS = "Sensor data published";
    private static final String PUBLISH_SENSOR_BAD_REQUEST = "Unable to publish sensor";
    private static final String PUBLISH_SENSOR_NOT_FOUND = "Sensor or instance not found";

    private final Logger logger = LogManager.getLogger(SensorController.class);
    private final Validation validation = new Validation();

    private final SensorService sensorService;

    @Autowired
    public SensorController(final SensorService sensorService) {
        this.sensorService = sensorService;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_SENSOR_DESCRIPTION, response = InstanceDto.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_SENSOR_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_CONFLICT, message = CREATE_SENSOR_CONFLICT, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_SENSOR_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_SENSOR_URI)
    @ResponseBody
    public InstanceDto create(@RequestBody final CreateSensorRequest createSensorRequest) {
        logger.debug("create started ...");

        validation.verify(createSensorRequest, createOrigin(CREATE_SENSOR_URI));
        return sensorService.create(createSensorRequest);
    }


    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = PUBLISH_SENSOR_DESCRIPTION, response = Void.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = PUBLISH_SENSOR_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = PUBLISH_SENSOR_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUBLISH_SENSOR_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })@PostMapping(PUBLISH_SENSOR_URI)
    @ResponseBody
    public void publish(@RequestBody final PublishSensorDataRequest publishSensorDataRequest) {
        logger.debug("publish started ...");

        validation.verify(publishSensorDataRequest, createOrigin(PUBLISH_SENSOR_URI));
        sensorService.publish(publishSensorDataRequest);
    }

    //-------------------------------------------------------------------------------------------------
    private String createOrigin(final String path) {
        return CommonConstants.GAMS_URI + PATH_PARAMETER_UID + path;
    }
}