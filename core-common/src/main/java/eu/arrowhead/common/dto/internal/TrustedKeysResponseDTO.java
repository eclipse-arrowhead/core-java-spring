package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrustedKeysResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<TrustedKeyDTO> trustedKeys;

    public TrustedKeysResponseDTO(List<TrustedKeyDTO> trustedKeyDTOs) {
        setTrustedKeys(trustedKeyDTOs);
    }

    public TrustedKeysResponseDTO() {
        setTrustedKeys(new ArrayList<TrustedKeyDTO>());
    }

    public List<TrustedKeyDTO> getTrustedKeys() {
        return trustedKeys;
    }

    public void setTrustedKeys(List<TrustedKeyDTO> trustedKeys) {
        this.trustedKeys = trustedKeys;
    }
}
