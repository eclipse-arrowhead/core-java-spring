package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

public class OnboardingWithCsrResponseDTO extends OnboardingResponseDTO implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    public OnboardingWithCsrResponseDTO() {
    }

    public OnboardingWithCsrResponseDTO(final ServiceEndpoint deviceRegistry, final ServiceEndpoint systemRegistry,
                                        final ServiceEndpoint serviceRegistry, final ServiceEndpoint orchestrationService,
                                        final CertificateCreationResponseDTO onboardingCertificate, final String intermediateCertificate,
                                        final String rootCertificate) {
        super(deviceRegistry, systemRegistry, serviceRegistry, orchestrationService, onboardingCertificate, intermediateCertificate, rootCertificate);
    }

    // this class exist to keep the structure of <operation>RequestDTO, <operation>ResponseDTO
}
