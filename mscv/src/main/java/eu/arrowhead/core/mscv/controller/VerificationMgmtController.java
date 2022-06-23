package eu.arrowhead.core.mscv.controller;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.VerificationResultDto;
import eu.arrowhead.common.dto.shared.mscv.VerificationResultListResponseDto;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.mscv.Constants;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.TargetService;
import eu.arrowhead.core.mscv.service.VerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_TARGET_NAME;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_TARGET_NAME_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_LAYER;
import static eu.arrowhead.core.mscv.Constants.PATH_OS;
import static eu.arrowhead.core.mscv.Constants.PATH_TARGET_NAME;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, Constants.SWAGGER_TAG_VERIFICATION_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class VerificationMgmtController {

    private static final String VERIFICATION_URI = "/results";

    private static final String READ_VERIFICATION_URI = VERIFICATION_URI +
            PATH_TARGET_NAME + PARAMETER_TARGET_NAME_PATH +
            PATH_OS + PARAMETER_OS_PATH +
            PATH_LAYER + PARAMETER_LAYER_PATH;
    private static final String READ_VERIFICATION_DESCRIPTION = "Get the latest verification result";

    private static final String FIND_ALL_VERIFICATION_URI = VERIFICATION_URI +
            PATH_TARGET_NAME + PARAMETER_TARGET_NAME_PATH +
            PATH_OS + PARAMETER_OS_PATH;
    private static final String FIND_ALL_VERIFICATION_DESCRIPTION = "Get verification results";

    private static final String VERIFICATION_NOT_FOUND = "MSCV verification results not found";
    private static final String VERIFICATION_SUCCESS = "MSCV verification results returned";
    private static final String VERIFICATION_BAD_REQUEST = "Unable to find MSCV verification results";
    private static final String EXECUTION_DATE_FIELD = "executionDate";

    private final Logger logger = LogManager.getLogger();
    private final VerificationService verificationService;
    private final TargetService targetService;
    private final Validation validation;

    @Autowired
    public VerificationMgmtController(final VerificationService verificationService, final TargetService targetService) {
        this.verificationService = verificationService;
        this.targetService = targetService;
        validation = new Validation();
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_VERIFICATION_DESCRIPTION, response = VerificationResultDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = VERIFICATION_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = VERIFICATION_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = VERIFICATION_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_VERIFICATION_URI)
    @ResponseBody
    public VerificationResultDto read(@PathVariable(PARAMETER_TARGET_NAME) final String name,
                                      @PathVariable(PARAMETER_OS) final OS os,
                                      @PathVariable(PARAMETER_LAYER) final Layer layer) {
        logger.debug("read started ...");
        final String origin = createMgmtOrigin(READ_VERIFICATION_URI);
        validation.verifyLayer(layer, origin);

        final Target target = verifyAndFindTarget(origin, name, os);

        return verificationService.getDetailResults(target, layer);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = FIND_ALL_VERIFICATION_DESCRIPTION, response = VerificationResultListResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = VERIFICATION_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = VERIFICATION_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(FIND_ALL_VERIFICATION_URI)
    @ResponseBody
    public VerificationResultListResponseDto find(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer pages,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @PathVariable(PARAMETER_TARGET_NAME) final String name,
            @PathVariable(PARAMETER_OS) final OS os,
            @ApiParam(value = "Filter for start date in ISO8601 format.")
            @RequestParam(name = "dateFrom", required = false) final String fromString,
            @ApiParam(value = "Filter for to date in ISO8601 format.")
            @RequestParam(name = "dateTo", required = false) final String toString,
            @ApiParam(value = "Filter for list name. Partial match ignoring case")
            @RequestParam(name = "list", required = false) final String listNameString) {
        logger.debug("find started ...");

        final String origin = createMgmtOrigin(FIND_ALL_VERIFICATION_URI);
        final Target target = verifyAndFindTarget(origin, name, os);
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(pages, size, CoreCommonConstants.SORT_ORDER_DESCENDING, origin);

        final Pageable page = pageParameters.createPageable(EXECUTION_DATE_FIELD);
        final ZonedDateTime from;
        final ZonedDateTime to;
        try {
            from = ZonedDateTime.parse(fromString);
            to = ZonedDateTime.parse(toString);
        } catch(final Exception ex) {
            throw new BadPayloadException(ex.getMessage(), HttpStatus.SC_BAD_REQUEST, createMgmtOrigin(FIND_ALL_VERIFICATION_URI));
        }

        final List<VerificationEntryList> list;
        if (Utilities.notEmpty(listNameString)) {
            final VerificationEntryList probe = new VerificationEntryList();
            probe.setName(listNameString);
            list = verificationService.findListByProbe(probe);
        } else {
            list = null;
        }

        return verificationService.findDetailResults(page, target, from, to, list);
    }

    private Target verifyAndFindTarget(final String origin, final String name, final OS os) {
        logger.debug("verifyAndFindTarget started ...");
        validation.verifyName(name, origin);
        validation.verifyOs(os, origin);

        final Optional<Target> optionalTarget = targetService.find(name, os);
        return optionalTarget.orElseThrow(notFoundException(VERIFICATION_BAD_REQUEST, origin));
    }

    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }

}
