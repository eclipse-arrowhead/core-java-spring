package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import javax.validation.constraints.NotBlank;

public class AddTrustedKeyRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "The publicKey is mandatory")
    private String publicKey;

    @NotBlank(message = "The description is mandatory")
    private String description;

    private ZonedDateTime validAfter;
    private ZonedDateTime validBefore;

    public AddTrustedKeyRequestDTO() {
    }

    public AddTrustedKeyRequestDTO(String publicKey, String description) {
        this(publicKey, description, null, null);
    }

    public AddTrustedKeyRequestDTO(String publicKey, String description, ZonedDateTime validAfter,
            ZonedDateTime validBefore) {
        this.publicKey = publicKey;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
        this.description = description;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public ZonedDateTime getValidBefore() {
        return validBefore;
    }

    public void setValidBefore(ZonedDateTime validBefore) {
        this.validBefore = validBefore;
    }

    public ZonedDateTime getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(ZonedDateTime validAfter) {
        this.validAfter = validAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
