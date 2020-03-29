package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryOnboardingWithCsrRequestDTO extends SystemRegistryRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -635438605292398404L;
    private String certificateSigningRequest;

    //=================================================================================================
    // methods

    public SystemRegistryOnboardingWithCsrRequestDTO() {
    }

    public SystemRegistryOnboardingWithCsrRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                     final String certificateSigningRequest) {
        super(system, provider, endOfValidity);
        this.certificateSigningRequest = certificateSigningRequest;
    }

    public SystemRegistryOnboardingWithCsrRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                     final Map<String, String> metadata, final Integer version, final String certificateSigningRequest) {
        super(system, provider, endOfValidity, metadata, version);
        this.certificateSigningRequest = certificateSigningRequest;
    }

    //-------------------------------------------------------------------------------------------------
    public String getCertificateRequest() {
        return certificateSigningRequest;
    }

    public void setCertificateRequest(final String certificateSigningRequest) {
        this.certificateSigningRequest = certificateSigningRequest;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SystemRegistryOnboardingWithCsrRequestDTO.class.getSimpleName() + "[", "]")
                .add("certificateSigningRequest=" + certificateSigningRequest)
                .add("parent=" + super.toString())
                .toString();
    }
}