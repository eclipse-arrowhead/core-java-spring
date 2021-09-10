package eu.arrowhead.core.gams.rest.controller;

import java.util.List;
import java.util.stream.Collectors;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.GamsInstance;
import eu.arrowhead.common.database.entity.Sensor;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gams.Constants;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceDto;
import eu.arrowhead.core.gams.rest.dto.GamsInstanceListDto;
import eu.arrowhead.core.gams.rest.dto.SensorDto;
import eu.arrowhead.core.gams.rest.dto.SensorListDto;
import eu.arrowhead.core.gams.service.InstanceService;
import eu.arrowhead.core.gams.service.SensorService;
import eu.arrowhead.core.gams.utility.Converter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_MGMT, CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.GAMS_URI + CoreCommonConstants.MGMT_URI)
public class GamsManagementController {

    private static final String GET_INSTANCES_HTTP_200_MESSAGE = "Instances returned";
    private static final String GET_INSTANCES_URI = Constants.PATH_ROOT;
    private static final String GET_SENSORS_HTTP_200_MESSAGE = "Sensors returned";
    private static final String GET_SENSORS_URI = Constants.PATH_PARAMETER_UID;

    private final Logger logger = LogManager.getLogger();
    private final InstanceService instanceService;
    private final SensorService sensorService;

    @Autowired
    public GamsManagementController(final InstanceService instanceService, final SensorService sensorService) {
        super();
        this.instanceService = instanceService;
        this.sensorService = sensorService;
    }

    @ApiOperation(value = "Return all gams instances", response = GamsInstanceListDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_INSTANCES_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(GET_INSTANCES_URI)
    @ResponseBody
    public GamsInstanceListDto getInstances(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("getInstances started ...");

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, CommonConstants.GAMS_URI + CoreCommonConstants.MGMT_URI + GET_INSTANCES_URI);
        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!GamsInstance.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        final Page<GamsInstance> instances = instanceService.getAll(pageParameters.createPageRequest(validatedSortField));
        final List<GamsInstanceDto> data = instances.stream()
                                                    .map(Converter::convert)
                                                    .collect(Collectors.toList());
        return new GamsInstanceListDto(data, data.size(), instances.getNumber(), instances.getTotalPages());
    }

    @ApiOperation(value = "Return all sensors", response = SensorListDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SENSORS_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(GET_SENSORS_URI)
    @ResponseBody
    public SensorListDto getSensors(
            @PathVariable(Constants.PARAMETER_UID) final String instanceUid,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("getSensors started ...");

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, CommonConstants.GAMS_URI + CoreCommonConstants.MGMT_URI + GET_SENSORS_URI);
        final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();

        if (!Sensor.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
            throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
        }

        final GamsInstance instance = instanceService.findByUid(instanceUid);
        final Page<Sensor> sensors = sensorService.findAllSensorByInstance(instance, pageParameters.createPageRequest(validatedSortField));
        final List<SensorDto> data = sensors.stream()
                                               .map(Converter::convert)
                                               .collect(Collectors.toList());
        return new SensorListDto(data, data.size(), sensors.getNumber(), sensors.getTotalPages());
    }
}
