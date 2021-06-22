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

package eu.arrowhead.common.drivers;

import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

@Service
public class CertificateAuthorityDriver extends AbstractDriver {

    //=================================================================================================
    // members
	
    private final Logger logger = LogManager.getLogger(CertificateAuthorityDriver.class);
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Autowired
    public CertificateAuthorityDriver(final DriverUtilities driverUtilities, final HttpService httpService) {
        super(driverUtilities, httpService);
    }

    //-------------------------------------------------------------------------------------------------
	public CertificateSigningResponseDTO signCertificate(final CertificateSigningRequestDTO request) throws DriverUtilities.DriverException {
        logger.traceEntry("signCertificate: {}", request);
        Assert.notNull(request, "CertificateSigningRequestDTO must not be null");
        final UriComponents uri = driverUtilities.findUri(CoreSystemService.CERTIFICATEAUTHORITY_SIGN_SERVICE);
        final ResponseEntity<CertificateSigningResponseDTO> httpResponse = httpService
                .sendRequest(uri, HttpMethod.POST, CertificateSigningResponseDTO.class, request);
        final CertificateSigningResponseDTO result = httpResponse.getBody();
        return logger.traceExit(result);
    }
}