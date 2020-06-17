package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrustedKeysResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long count;
    private List<TrustedKeyDTO> trustedKeys;

    public TrustedKeysResponseDTO(List<TrustedKeyDTO> trustedKeyDTOs, int count) {
        this.count = count;
        setTrustedKeys(trustedKeyDTOs);
    }

    public TrustedKeysResponseDTO() {
        setTrustedKeys(new ArrayList<>());
    }

    public List<TrustedKeyDTO> getTrustedKeys() {
        return trustedKeys;
    }

    public void setTrustedKeys(List<TrustedKeyDTO> trustedKeys) {
        this.trustedKeys = trustedKeys;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
