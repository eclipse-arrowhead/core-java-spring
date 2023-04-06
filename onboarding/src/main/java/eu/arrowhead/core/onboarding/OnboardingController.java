/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.onboarding;

import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_CERTIFICATE_AND_CSR;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_CERTIFICATE_AND_NAME;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_SHARED_SECRET_AND_CSR;
import static eu.arrowhead.common.CommonConstants.OP_ONBOARDING_WITH_SHARED_SECRET_AND_NAME;

import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.onboarding.service.OnboardingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.ONBOARDING_URI)
public class OnboardingController {

    //=================================================================================================
    // members

    private static final String ONBOARDING_HTTP_200_MESSAGE = "Initial Onboarding successful";
    private static final String ONBOARDING_HTTP_400_MESSAGE = "Request parameter missing";
    private static final String COMMON_NAME_NULL_ERROR_MESSAGE = " Common name must have value ";
    private static final String CSR_NULL_ERROR_MESSAGE = " CertificateSigningRequest must have value ";

    private final Logger logger = LogManager.getLogger(OnboardingController.class);
    private final OnboardingService onboardingService;
    private final CommonDBService commonDBService;
    private final SSLProperties sslProperties;

    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String, Object> arrowheadContext;

    @Value(value = "${sharedSecret:#{null}}")
    private Optional<String> sharedSecret;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Autowired
    public OnboardingController(final OnboardingService onboardingService, final CommonDBService commonDBService, final SSLProperties sslProperties) {
    	this.onboardingService = onboardingService;
    	this.commonDBService = commonDBService;
    	this.sslProperties = sslProperties;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core device availability", response = String.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echoOnboarding() {
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
				
		final String origin = CommonConstants.ONBOARDING_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.ONBOARDINGCONTROLLER, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Onboarding with certificate and device name", response = OnboardingWithNameResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ONBOARDING_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ONBOARDING_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(OP_ONBOARDING_WITH_CERTIFICATE_AND_NAME)
    @ResponseBody
    public OnboardingWithNameResponseDTO onboardWithCertificateAndName(final HttpServletRequest httpServletRequest,
                                                                       @RequestBody final OnboardingWithNameRequestDTO onboardingRequest) {
        logger.debug("onboardWithCertificateAndName started ...");

        authenticateCertificate(httpServletRequest);
        verifyRequest(onboardingRequest, CommonConstants.ONBOARDING_URI + OP_ONBOARDING_WITH_CERTIFICATE_AND_NAME);

        final String host = httpServletRequest.getRemoteHost();
        final String address = httpServletRequest.getRemoteAddr();
        return onboardingService.onboarding(onboardingRequest, host, address);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Onboarding with shared secret and device name", response = OnboardingWithNameResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ONBOARDING_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ONBOARDING_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(OP_ONBOARDING_WITH_SHARED_SECRET_AND_NAME)
    @ResponseBody
    public OnboardingWithNameResponseDTO onboardWithSharedSecretAndName(final HttpServletRequest httpServletRequest,
                                                                        @RequestBody final OnboardingWithNameRequestDTO onboardingRequest,
                                                                        @RequestHeader final HttpHeaders headers) {
        logger.debug("onboardWithSharedSecretAndName started ...");

        authenticateSharedSecret(headers.getFirst(HttpHeaders.AUTHORIZATION));
        verifyRequest(onboardingRequest, CommonConstants.ONBOARDING_URI + OP_ONBOARDING_WITH_SHARED_SECRET_AND_NAME);

        final String host = httpServletRequest.getRemoteHost();
        final String address = httpServletRequest.getRemoteAddr();
        return onboardingService.onboarding(onboardingRequest, host, address);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Onboarding with certificate and certificate signing request", response = OnboardingWithCsrResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ONBOARDING_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ONBOARDING_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(OP_ONBOARDING_WITH_CERTIFICATE_AND_CSR)
    @ResponseBody
    public OnboardingWithCsrResponseDTO onboardWithCertificateAndSigningRequest(final HttpServletRequest httpServletRequest,
                                                                                @RequestBody final OnboardingWithCsrRequestDTO onboardingRequest) {
        logger.debug("onboardWithCertificateAndSigningRequest started ...");

        authenticateCertificate(httpServletRequest);
        verifyRequest(onboardingRequest, CommonConstants.ONBOARDING_URI + OP_ONBOARDING_WITH_CERTIFICATE_AND_CSR);

        return onboardingService.onboarding(onboardingRequest);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Onboarding with certificate and certificate signing request", response = OnboardingWithCsrResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ONBOARDING_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ONBOARDING_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(OP_ONBOARDING_WITH_SHARED_SECRET_AND_CSR)
    @ResponseBody
    public OnboardingWithCsrResponseDTO onboardWithSharedSecretAndSigningRequest(@RequestBody final OnboardingWithCsrRequestDTO onboardingRequest,
                                                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization) {
        logger.debug("onboardWithSharedSecretAndSigningRequest started ...");

        authenticateSharedSecret(authorization);
        verifyRequest(onboardingRequest, CommonConstants.ONBOARDING_URI + OP_ONBOARDING_WITH_SHARED_SECRET_AND_CSR);

        return onboardingService.onboarding(onboardingRequest);
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private void authenticateCertificate(final HttpServletRequest httpServletRequest) {
        final X509Certificate[] certificates = (X509Certificate[]) httpServletRequest.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);

        if (sslProperties.isSslEnabled()) {
            if (Objects.nonNull(certificates) && certificates.length > 0) {

                // TODO verification should be done by CA
                final X509Certificate cert = certificates[0];
                final String clientCN = Utilities.getCertCNFromSubject(cert.getSubjectDN().getName());
                final String requestTarget = Utilities.stripEndSlash(httpServletRequest.getRequestURL().toString());

                if (!Utilities.isKeyStoreCNArrowheadValid(clientCN)) {
                    logger.debug("{} is not a valid common name, access denied!", clientCN);
                    throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
                }

                // All requests from the local cloud are allowed
                if (!Utilities.isKeyStoreCNArrowheadValid(clientCN, getServerCloudCN())) {
                    logger.debug("{} is unauthorized to access {}", clientCN, requestTarget);
                    throw new AuthException(clientCN + " is unauthorized to access " + requestTarget);
                }
            } else {
                logger.debug("No client certificate given!");
                throw new AuthException("Client certificate in needed!");
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void authenticateSharedSecret(final String authorization) {
        if (sharedSecret.isEmpty()) {
            throw new InvalidParameterException("Shared Secret is disabled by this service");
        }

        if (Utilities.isEmpty(authorization)) {
            throw new AuthException("Basic Authentication is needed for this method", HttpStatus.SC_UNAUTHORIZED);
        }

        // Header is in the format "Basic 5tyc0uiDat4"
        // We need to extract data before decoding it back to original string
        final String[] authParts = authorization.split("\\s+");
        final String authInfo = authParts[1];

        // Decode the data back to original string
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(authInfo);
        } catch (final IllegalArgumentException e) {
            logger.error(e.getMessage());
            throw new ArrowheadException("Unable to decode authorization header");
        }

        final String expectedSecret = sharedSecret.get();
        final String[] basicAuth = new String(bytes).split(":");

        if (!(basicAuth.length == 2 && expectedSecret.equals(basicAuth[1]))) {
            throw new AuthException("Basic Authentication failed with the given secret", HttpStatus.SC_UNAUTHORIZED);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void verifyRequest(final OnboardingWithNameRequestDTO onboardingRequest, final String origin) {
        logger.debug("verifyRequest started...");

        if (Objects.isNull(onboardingRequest) || Objects.isNull(onboardingRequest.getCreationRequestDTO())) {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(onboardingRequest.getCreationRequestDTO().getCommonName())) {
            throw new BadPayloadException(COMMON_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void verifyRequest(final OnboardingWithCsrRequestDTO onboardingRequest, final String origin) {
        logger.debug("verifyRequest started...");

        if (Objects.isNull(onboardingRequest)) {
            throw new BadPayloadException("Request is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(onboardingRequest.getCertificateSigningRequest())) {
            throw new BadPayloadException(CSR_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private String getServerCloudCN() {
        final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
        final String[] serverFields = serverCN.split("\\.", 2); // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
        Assert.isTrue(serverFields.length >= 2, "Server common name is invalid: " + serverCN);

        return serverFields[1];
    }
}