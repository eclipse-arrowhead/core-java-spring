package eu.arrowhead.core.onboarding;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.onboarding.database.service.OnboardingDBService;
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

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.Objects;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.ONBOARDING_URI)
public class OnboardingController
{

    //=================================================================================================
    // members

    private static final String ONBOARDING_HTTP_200_MESSAGE = "Initial Onboarding successful";
    private static final String ONBOARDING_HTTP_400_MESSAGE = "Request parameter missing";
    private static final String DEVICE_NAME_NULL_ERROR_MESSAGE = " Device name must have value ";
    private static final String CSR_NULL_ERROR_MESSAGE = " CertificateSigningRequest must have value ";

    private static final String ONBOARDING_WITH_NAME_URI = "/name";
    private static final String ONBOARDING_WITH_CSR_URI = "/csr";

    private final Logger logger = LogManager.getLogger(OnboardingController.class);
    private final OnboardingDBService onboardingDBService;

    @Autowired
    public OnboardingController(final OnboardingDBService onboardingDBService)
    {
        this.onboardingDBService = onboardingDBService;
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
    public String echoDevice()
    {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return onboarding certificate", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ONBOARDING_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ONBOARDING_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(ONBOARDING_WITH_NAME_URI)
    @ResponseBody
    public OnboardingWithNameResponseDTO onboardWithName(final HttpServletRequest httpServletRequest,
                                                         @RequestBody final OnboardingWithNameRequestDTO onboardingRequest)
    {
        logger.debug("onboardWithName started ...");

        authenticateRequest(httpServletRequest);
        verifyRequest(onboardingRequest, CommonConstants.ONBOARDING_URI + ONBOARDING_WITH_NAME_URI);

        return onboardingDBService.onboarding(onboardingRequest);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return onboarding certificate", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ONBOARDING_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ONBOARDING_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(ONBOARDING_WITH_CSR_URI)
    @ResponseBody
    public OnboardingWithCsrResponseDTO onboardWithCsr(final HttpServletRequest httpServletRequest,
                                                         @RequestBody final OnboardingWithCsrRequestDTO onboardingRequest)
    {
        logger.debug("onboardWithName started ...");

        authenticateRequest(httpServletRequest);
        verifyRequest(onboardingRequest, CommonConstants.ONBOARDING_URI + ONBOARDING_WITH_NAME_URI);

        return onboardingDBService.onboarding(onboardingRequest);
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private void authenticateRequest(final HttpServletRequest httpServletRequest)
    {
        final X509Certificate[] certificates = (X509Certificate[]) httpServletRequest.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);

        if (Objects.nonNull(certificates))
        {
            // TODO verify certificate with certificate authority
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void verifyRequest(final OnboardingWithNameRequestDTO onboardingRequest, final String origin)
    {
        logger.debug("verifyRequest started...");

        if (onboardingRequest == null)
        {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(onboardingRequest.getDeviceName()))
        {
            throw new BadPayloadException(DEVICE_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void verifyRequest(final OnboardingWithCsrRequestDTO onboardingRequest, final String origin)
    {
        logger.debug("verifyRequest started...");

        if (onboardingRequest == null)
        {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(onboardingRequest.getCertificateSigningRequest()))
        {
            throw new BadPayloadException(CSR_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}