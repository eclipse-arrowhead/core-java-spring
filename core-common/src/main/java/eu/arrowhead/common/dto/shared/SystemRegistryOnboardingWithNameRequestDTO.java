package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryOnboardingWithNameRequestDTO extends SystemRegistryRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -635438605292398404L;
    private CertificateCreationRequestDTO certificateCreationRequest;

    //=================================================================================================
    // methods

    public SystemRegistryOnboardingWithNameRequestDTO() {
    }

    public SystemRegistryOnboardingWithNameRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                      final CertificateCreationRequestDTO certificateCreationRequest) {
        super(system, provider, endOfValidity);
        this.certificateCreationRequest = certificateCreationRequest;
    }


    public SystemRegistryOnboardingWithNameRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                      final Map<String, String> metadata, final Integer version,
                                                      final CertificateCreationRequestDTO certificateCreationRequest) {
        super(system, provider, endOfValidity, metadata, version);
        this.certificateCreationRequest = certificateCreationRequest;
    }

    //-------------------------------------------------------------------------------------------------
    public CertificateCreationRequestDTO getCertificateCreationRequest() {
        return certificateCreationRequest;
    }

    public void setCertificateCreationRequest(final CertificateCreationRequestDTO certificateCreationRequest) {
        this.certificateCreationRequest = certificateCreationRequest;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SystemRegistryOnboardingWithNameRequestDTO.class.getSimpleName() + "[", "]")
                .add("certificateRequest=" + certificateCreationRequest)
                .add("parent=" + super.toString())
                .toString();
    }
}