package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Map;

public class DeviceRegistryOnboardingWithCsrResponseDTO extends DeviceRegistryOnboardingResponseDTO implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    public DeviceRegistryOnboardingWithCsrResponseDTO() {
    }

    public DeviceRegistryOnboardingWithCsrResponseDTO(final long id, final DeviceResponseDTO device, final String endOfValidity,
                                                      final Map<String, String> metadata, final int version, final String createdAt,
                                                      final String updatedAt,
                                                      final CertificateCreationResponseDTO certificateResponse) {
        super(id, device, endOfValidity, metadata, version, createdAt, updatedAt, certificateResponse);
    }

    // this class exist to keep the structure of <operation>RequestDTO, <operation>ResponseDTO
}
