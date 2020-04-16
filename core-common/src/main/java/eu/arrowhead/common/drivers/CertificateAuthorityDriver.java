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
import org.springframework.web.util.UriComponents;

@Service
public class CertificateAuthorityDriver extends AbstractDriver {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(CertificateAuthorityDriver.class);

    @Autowired
    public CertificateAuthorityDriver(final DriverUtilities driverUtilities, final HttpService httpService) {
        super(driverUtilities, httpService);
    }

    public CertificateSigningResponseDTO signCertificate(final CertificateSigningRequestDTO request) throws DriverUtilities.DriverException {
        logger.traceEntry("signCertificate: {}", request);
        final UriComponents uri = driverUtilities.findUri(CoreSystemService.CERTIFICATE_AUTHORITY_SIGN_SERVICE);
        final ResponseEntity<CertificateSigningResponseDTO> httpResponse = httpService
                .sendRequest(uri, HttpMethod.POST, CertificateSigningResponseDTO.class, request);
        final CertificateSigningResponseDTO result = httpResponse.getBody();
        return logger.traceExit(result);
    }
}
