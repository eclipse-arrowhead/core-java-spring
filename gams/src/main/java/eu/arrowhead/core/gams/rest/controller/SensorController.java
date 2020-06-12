package eu.arrowhead.core.gams.rest.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.core.gams.Validation;
import eu.arrowhead.core.gams.database.entities.GamsInstance;
import eu.arrowhead.core.gams.database.entities.Sensor;
import eu.arrowhead.core.gams.rest.dto.CreateSensorRequest;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceDto;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import eu.arrowhead.core.gams.rest.dto.SensorDto;
import eu.arrowhead.core.gams.service.InstanceService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.core.gams.Constants.PARAMETER_SENSOR;
import static eu.arrowhead.core.gams.Constants.PARAMETER_UID;
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

    private final InstanceService instanceService;
    private final SensorService sensorService;

    @Autowired
    public SensorController(InstanceService instanceService, final SensorService sensorService) {
        this.instanceService = instanceService;
        this.sensorService = sensorService;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_SENSOR_DESCRIPTION, response = GamsInstanceDto.class,
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
    public SensorDto create(@PathVariable(PARAMETER_UID) final String instanceUid, @RequestBody final CreateSensorRequest createSensorRequest) {
        logger.debug("create started ...");
        final String origin = CommonConstants.GAMS_URI + "/" + instanceUid;

        validation.verify(createSensorRequest, origin);

        final GamsInstance instance = instanceService.findByUid(instanceUid);
        return sensorService.create(instance, createSensorRequest);
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
    })
    @PostMapping(PUBLISH_SENSOR_URI)
    @ResponseBody
    public void publish(@PathVariable(PARAMETER_UID) final String instanceUid,
                        @PathVariable(PARAMETER_SENSOR) final String sensorUid,
                        @RequestBody final PublishSensorDataRequest publishSensorDataRequest) {
        logger.debug("publish started ...");
        final String origin = CommonConstants.GAMS_URI + "/" + instanceUid + PATH_SENSOR + "/" + sensorUid;

        validation.verify(publishSensorDataRequest, origin);

        final GamsInstance instance = instanceService.findByUid(instanceUid);
        final Sensor sensor = sensorService.findByUid(sensorUid);

        validation.verifyEquals(sensor.getInstance(), instance, origin);

        sensorService.publish(sensor, publishSensorDataRequest);
    }

    //-------------------------------------------------------------------------------------------------
    private String createOrigin(final String uid, final String path) {
        return CommonConstants.GAMS_URI + "/" + uid + path;
    }
}