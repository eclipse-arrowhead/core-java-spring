/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.certificate_authority;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import eu.arrowhead.common.dto.internal.AddTrustedKeyResponseDTO;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.internal.AddTrustedKeyRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateCheckResponseDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.internal.IssuedCertificatesResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckRequestDTO;
import eu.arrowhead.common.dto.internal.TrustedKeyCheckResponseDTO;
import eu.arrowhead.common.dto.internal.TrustedKeysResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, allowedHeaders = {
		HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION })
@RestController
@RequestMapping(CommonConstants.CERTIFICATE_AUTHRORITY_URI)
public class CertificateAuthorityController {

	// =================================================================================================
	// members
	//
	private static final String PATH_VARIABLE_ID = "id";

	private static final String OP_CA_MGMT_CERTIFICATES_URI = CoreCommonConstants.MGMT_URI + "/certificates";
	private static final String OP_CA_MGMT_CERTIFICATE_DELETE_URI = CoreCommonConstants.MGMT_URI + "/certificates/{"
			+ PATH_VARIABLE_ID + "}";
	private static final String OP_CA_MGMT_TRUSTED_KEYS_URI = CoreCommonConstants.MGMT_URI + "/keys";
	private static final String OP_CA_MGMT_TRUSTED_KEY_DELETE_URI = CoreCommonConstants.MGMT_URI + "/keys/{"
			+ PATH_VARIABLE_ID + "}";

	private static final String SIGN_CERTIFICATE_HTTP_200_MESSAGE = "Successful certificate signing";
	private static final String SIGN_CERTIFICATE_HTTP_400_MESSAGE = "Invalid Certificate Signing Request";
	private static final String CHECK_CERTIFICATE_HTTP_200_MESSAGE = "Certificate is valid";
	private static final String CHECK_CERTIFICATE_HTTP_400_MESSAGE = "Invalid Certificate Check Request";
	private static final String CHECK_TRUSTED_KEY_HTTP_200_MESSAGE = "Public key is trusted";
	private static final String CHECK_TRUSTED_KEY_HTTP_400_MESSAGE = "Invalid Trusted Key Check Request";
	private static final String GET_ISSUED_CERTIFICATES_HTTP_200_MESSAGE = "Issued certificates returned";
	private static final String GET_TRUSTED_KEYS_HTTP_200_MESSAGE = "Trusted public keys returned";
	private static final String ADD_TRUSTED_KEY_HTTP_201_MESSAGE = "Trusted public key added";
	private static final String ADD_TRUSTED_KEY_HTTP_400_MESSAGE = "Invalid Add Trusted Key Request";
	private static final String DELETE_TRUSTED_KEY_HTTP_204_MESSAGE = "Trusted public key removed";

	private final Logger logger = LogManager.getLogger(CertificateAuthorityController.class);

	@Autowired
	private CertificateAuthorityService certificateAuthorityService;

	// =================================================================================================
	// methods

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Check certificate validity", response = CertificateCheckResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CHECK_CERTIFICATE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CHECK_CERTIFICATE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@PostMapping(path = CommonConstants.OP_CA_CHECK_CERTIFICATE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public CertificateCheckResponseDTO checkCertificate(@Valid @RequestBody final CertificateCheckRequestDTO request,
			BindingResult bindingResult) {
		handleBindingResult(bindingResult);
		return certificateAuthorityService.checkCertificate(request);
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return a signed certificate", response = CertificateSigningResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SIGN_CERTIFICATE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SIGN_CERTIFICATE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@PostMapping(path = CommonConstants.OP_CA_SIGN_CERTIFICATE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public CertificateSigningResponseDTO signCertificate(@Valid @RequestBody final CertificateSigningRequestDTO request,
			BindingResult bindingResult, HttpServletRequest httpServletRequest) {
		handleBindingResult(bindingResult);
		final String requestedByCN = CertificateAuthorityUtils.getRequesterCommonName(httpServletRequest);
		return certificateAuthorityService.signCertificate(request, requestedByCN);
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Get issued Certificates", response = IssuedCertificatesResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ISSUED_CERTIFICATES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@GetMapping(path = OP_CA_MGMT_CERTIFICATES_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	public IssuedCertificatesResponseDTO getIssuedCertificates(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("Get IssuedCertificates request recieved with page: {} and item_per page: {}", page, size);

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Only both or none of page and size may be defined.",
						HttpStatus.SC_BAD_REQUEST,
						CommonConstants.CERTIFICATE_AUTHRORITY_URI + OP_CA_MGMT_CERTIFICATES_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction,
				CommonConstants.CERTIFICATE_AUTHRORITY_URI + OP_CA_MGMT_CERTIFICATES_URI);
		return certificateAuthorityService.getCertificates(validatedPage, validatedSize, validatedDirection, sortField);
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Revoke issued Certificate", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ISSUED_CERTIFICATES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@DeleteMapping(path = OP_CA_MGMT_CERTIFICATE_DELETE_URI)
	public ResponseEntity<String> revokeCertificate(@PathVariable long id, HttpServletRequest httpServletRequest) {
		final String requestedByCN = CertificateAuthorityUtils.getRequesterCommonName(httpServletRequest);
		if (id <= 0) {
			throw new BadPayloadException("Invalid id");
		}

		if (certificateAuthorityService.revokeCertificate(id, requestedByCN)) {
			return new ResponseEntity<String>("Certificate revoked", org.springframework.http.HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Certificate revocation failed",
					org.springframework.http.HttpStatus.BAD_REQUEST);
		}
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Check if the given public key is trusted", response = TrustedKeyCheckResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CHECK_TRUSTED_KEY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CHECK_TRUSTED_KEY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@PostMapping(path = CommonConstants.OP_CA_CHECK_TRUSTED_KEY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public TrustedKeyCheckResponseDTO checkTrustedKey(@Valid @RequestBody final TrustedKeyCheckRequestDTO request,
			BindingResult bindingResult) {
		handleBindingResult(bindingResult);
		return certificateAuthorityService.checkTrustedKey(request);
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Get trusted public keys", response = TrustedKeysResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_TRUSTED_KEYS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@GetMapping(path = OP_CA_MGMT_TRUSTED_KEYS_URI)
	public TrustedKeysResponseDTO getTrustedKeys(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New TrustedKeys get request recieved with page: {} and item_per page: {}", page, size);

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Only both or none of page and size may be defined.",
						HttpStatus.SC_BAD_REQUEST,
						CommonConstants.CERTIFICATE_AUTHRORITY_URI + OP_CA_MGMT_TRUSTED_KEYS_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction,
				CommonConstants.CERTIFICATE_AUTHRORITY_URI + OP_CA_MGMT_TRUSTED_KEYS_URI);
		return certificateAuthorityService.getTrustedKeys(validatedPage, validatedSize, validatedDirection, sortField);
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Add trusted public key", response = AddTrustedKeyResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = ADD_TRUSTED_KEY_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ADD_TRUSTED_KEY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@PutMapping(path = OP_CA_MGMT_TRUSTED_KEYS_URI)
	public ResponseEntity<AddTrustedKeyResponseDTO> addTrustedKey(@Valid @RequestBody final AddTrustedKeyRequestDTO request,
																  BindingResult bindingResult) {
		handleBindingResult(bindingResult);
		final AddTrustedKeyResponseDTO response = certificateAuthorityService.addTrustedKey(request);
		return new ResponseEntity<AddTrustedKeyResponseDTO>(response, org.springframework.http.HttpStatus.CREATED);
	}

	// -------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Delete trusted public key", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_TRUSTED_KEY_HTTP_204_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE) })
	@DeleteMapping(path = OP_CA_MGMT_TRUSTED_KEY_DELETE_URI)
	public ResponseEntity<String> deleteTrustedKey(@PathVariable long id) {
		if (id <= 0) {
			throw new BadPayloadException("Invalid id");
		}
		certificateAuthorityService.deleteTrustedKey(id);
		return new ResponseEntity<String>("OK", org.springframework.http.HttpStatus.NO_CONTENT);
	}

	private void handleBindingResult(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			final List<ObjectError> allErrors = bindingResult.getAllErrors();
			for (ObjectError error : allErrors) {
				logger.debug(error.toString());
			}
			throw new BadPayloadException(allErrors.get(0).getDefaultMessage());
		}
	}
}
