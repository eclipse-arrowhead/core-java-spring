package eu.arrowhead.core.gams.rest.controller;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import javax.servlet.http.HttpServletRequest;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gams.Constants;
import eu.arrowhead.core.gams.RestValidation;
import eu.arrowhead.core.gams.rest.dto.PublishSensorDataRequest;
import eu.arrowhead.core.gams.service.InstanceService;
import eu.arrowhead.core.gams.service.MapeKService;
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

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.GAMS_URI + Constants.PATH_PARAMETER_UID)
public class SensorDataController {

    //=================================================================================================
    // members
    private static final String PUBLISH_SENSOR_UID_URI = Constants.PATH_SENSOR + Constants.PATH_PARAMETER_SENSOR;
    private static final String PUBLISH_SENSOR_ADDRESS_URI = Constants.PATH_ADDRESS;
    private static final String PUBLISH_SENSOR_DESCRIPTION = "Publish sensor data to an instance";
    private static final String PUBLISH_SENSOR_SUCCESS = "Sensor data published";
    private static final String PUBLISH_SENSOR_BAD_REQUEST = "Unable to publish sensor data";
    private static final String PUBLISH_SENSOR_NOT_FOUND = "Sensor or instance not found";

    private final Logger logger = LogManager.getLogger(SensorDataController.class);
    private final RestValidation validation = new RestValidation();

    private final InstanceService instanceService;
    private final SensorService sensorService;
    private final MapeKService mapeKService;

    @Autowired
    public SensorDataController(InstanceService instanceService, final SensorService sensorService, final MapeKService mapeKService) {
        this.instanceService = instanceService;
        this.sensorService = sensorService;
        this.mapeKService = mapeKService;
    }

    //=================================================================================================
    // methods

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
    @PostMapping(PUBLISH_SENSOR_UID_URI)
    @ResponseBody
    public void publish(@PathVariable(Constants.PARAMETER_UID) final String instanceUid,
                        @PathVariable(Constants.PARAMETER_SENSOR) final String sensorUid,
                        @RequestBody final PublishSensorDataRequest request,
                        final HttpServletRequest servletRequest) {
        logger.debug("publish started ...");
        final String origin = CommonConstants.GAMS_URI + "/" + instanceUid + Constants.PATH_SENSOR + "/" + sensorUid;

        validation.verify(request, origin);

        final GamsInstance instance = instanceService.findByUid(instanceUid);
        final Sensor sensor = sensorService.findSensorByUid(sensorUid);
        publish(instance, sensor, request, servletRequest.getRemoteAddr(), origin);
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
    @PostMapping(PUBLISH_SENSOR_ADDRESS_URI)
    @ResponseBody
    public void publish(@PathVariable(Constants.PARAMETER_UID) final String instanceUid,
                        @RequestBody final PublishSensorDataRequest request,
                        final HttpServletRequest servletRequest) {
        logger.debug("publish started ...");
        final String origin = CommonConstants.GAMS_URI + "/" + instanceUid + Constants.PATH_ADDRESS + "/" + servletRequest.getRemoteAddr();

        validation.verify(request, origin);

        final GamsInstance instance = instanceService.findByUid(instanceUid);
        final Sensor sensor = sensorService.findSensorByAddress(instance, servletRequest.getRemoteAddr());
        publish(instance, sensor, request, servletRequest.getRemoteAddr(), origin);
    }

    private void publish(final GamsInstance instance, final Sensor sensor, final PublishSensorDataRequest request, final String address, final String origin) {
        validation.verifyEquals(sensor.getInstance(), instance, origin);
        validation.verify(request, origin);

        final ZonedDateTime timestamp;
        try {
            timestamp = Utilities.parseUTCStringToLocalZonedDateTime(request.getTimestamp());
            mapeKService.publish(sensor, timestamp, request.getData(), address);
        } catch (DateTimeParseException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }
}