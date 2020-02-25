package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.CERTIFICATE_AUTHRORITY_URI)
public class CertificateAuthorityController {

	//=================================================================================================
	// members
//
	private static final String GET_CLOUD_COMMON_NAME_HTTP_200_MESSAGE = "Cloud Common Name returned";
	private static final String SIGN_CERTIFICATE_HTTP_200_MESSAGE = "Successful certificate signing";
	private static final String SIGN_CERTIFICATE_HTTP_400_MESSAGE = "Invalid Certificate Signing Request";

	private final Logger logger = LogManager.getLogger(CertificateAuthorityController.class);

	@Autowired
	private CertificateAuthorityService certificateAuthorityService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return the cloud's Common Name", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CLOUD_COMMON_NAME_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_CA_CLOUD_COMMON_NAME_URI)
	public String getCloudCommonName() {
		logger.debug("getCloudCommonName started ...");

		return certificateAuthorityService.getCloudCommonName();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return a signed certificate", response = CertificateSigningResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT})
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SIGN_CERTIFICATE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SIGN_CERTIFICATE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_CA_SIGN_CERTIFICATE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CertificateSigningResponseDTO signCertificate(@RequestBody final CertificateSigningRequestDTO request) {
		return certificateAuthorityService.signCertificate(request);
	}
}
