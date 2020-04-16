package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

public class CertificateSigningRequestDTO implements Serializable {

    private static final long serialVersionUID = -6810780579000655432L;

    @NotBlank(message = "The encodedCSR is mandatory")
    private String encodedCSR;

    public CertificateSigningRequestDTO() {}

    public CertificateSigningRequestDTO(String encodedCSR) {
        this.encodedCSR = encodedCSR;
    }

    public String getEncodedCSR() {
        return encodedCSR;
    }

    public void setEncodedCSR(String encodedCSR) {
        this.encodedCSR = encodedCSR;
    }
}
