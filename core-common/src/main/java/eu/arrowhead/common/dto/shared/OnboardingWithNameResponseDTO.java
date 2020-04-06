package eu.arrowhead.common.dto.shared;

public class OnboardingWithNameResponseDTO extends OnboardingResponseDTO {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    public OnboardingWithNameResponseDTO() {
    }

    public OnboardingWithNameResponseDTO(final ServiceEndpoint deviceRegistry, final ServiceEndpoint systemRegistry,
                                         final ServiceEndpoint serviceRegistry, final ServiceEndpoint orchestrationService,
                                         final CertificateCreationResponseDTO onboardingCertificate, final String intermediateCertificate,
                                         final String rootCertificate) {
        super(deviceRegistry, systemRegistry, serviceRegistry, orchestrationService, onboardingCertificate, intermediateCertificate, rootCertificate);
    }

    // this class exist to keep the structure of <operation>RequestDTO, <operation>ResponseDTO
}
