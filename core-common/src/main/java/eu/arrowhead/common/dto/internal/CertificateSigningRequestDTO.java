package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import javax.validation.constraints.NotBlank;

public class CertificateSigningRequestDTO implements Serializable {

    private static final long serialVersionUID = -6810780579000655432L;

    @NotBlank(message = "The encodedCSR is mandatory")
    private String encodedCSR;

    private ZonedDateTime validAfter;
    private ZonedDateTime validBefore;

    public CertificateSigningRequestDTO() {}

    public CertificateSigningRequestDTO(String encodedCSR) {
        this.encodedCSR = encodedCSR;
    }

    public CertificateSigningRequestDTO(String encodedCSR, ZonedDateTime validAfter, ZonedDateTime validBefore) {
        this.encodedCSR = encodedCSR;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    public String getEncodedCSR() {
        return encodedCSR;
    }

    public void setEncodedCSR(String encodedCSR) {
        this.encodedCSR = encodedCSR;
    }

    public ZonedDateTime getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(ZonedDateTime validAfter) {
        this.validAfter = validAfter;
    }

    public ZonedDateTime getValidBefore() {
        return validBefore;
    }

    public void setValidBefore(ZonedDateTime validBefore) {
        this.validBefore = validBefore;
    }
}
