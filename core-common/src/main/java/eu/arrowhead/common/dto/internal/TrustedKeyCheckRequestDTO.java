package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

public class TrustedKeyCheckRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @NotBlank(message = "The publicKey is mandatory")
    private String publicKey;

    public TrustedKeyCheckRequestDTO() {}

    public TrustedKeyCheckRequestDTO(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
