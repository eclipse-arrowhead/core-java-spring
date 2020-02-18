package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class CertificateSigningResponseDTO implements Serializable {

    private static final long serialVersionUID = -6810780579000655432L;

    private String certificatePem;

    public CertificateSigningResponseDTO() {}

    public CertificateSigningResponseDTO(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }
}
