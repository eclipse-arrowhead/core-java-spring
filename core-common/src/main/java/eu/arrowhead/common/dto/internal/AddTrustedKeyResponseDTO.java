package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class AddTrustedKeyResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private ZonedDateTime validAfter;
    private ZonedDateTime validBefore;

    public AddTrustedKeyResponseDTO(long id) {
        this(id, null, null);
    }

    public AddTrustedKeyResponseDTO(long id, ZonedDateTime validAfter, ZonedDateTime validBefore) {
        this.id = id;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
