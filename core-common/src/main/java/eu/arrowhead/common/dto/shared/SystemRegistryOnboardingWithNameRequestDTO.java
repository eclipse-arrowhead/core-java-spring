package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryOnboardingWithNameRequestDTO extends SystemRegistryRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -635438605292398404L;
    private CertificateCreationRequestDTO certificateRequest;

    //=================================================================================================
    // methods

    public SystemRegistryOnboardingWithNameRequestDTO() {
    }

    public SystemRegistryOnboardingWithNameRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                      final CertificateCreationRequestDTO certificateRequest) {
        super(system, provider, endOfValidity);
        this.certificateRequest = certificateRequest;
    }


    public SystemRegistryOnboardingWithNameRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                      final Map<String, String> metadata, final Integer version,
                                                      final CertificateCreationRequestDTO certificateRequest) {
        super(system, provider, endOfValidity, metadata, version);
        this.certificateRequest = certificateRequest;
    }

    //-------------------------------------------------------------------------------------------------
    public CertificateCreationRequestDTO getCertificateRequest() {
        return certificateRequest;
    }

    public void setCertificateRequest(final CertificateCreationRequestDTO certificateRequest) {
        this.certificateRequest = certificateRequest;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SystemRegistryOnboardingWithNameRequestDTO.class.getSimpleName() + "[", "]")
                .add("certificateRequest=" + certificateRequest)
                .add("parent=" + super.toString())
                .toString();
    }
}