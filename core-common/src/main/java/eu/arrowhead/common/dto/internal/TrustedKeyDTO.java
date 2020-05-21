package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class TrustedKeyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private ZonedDateTime createdAt;
    private String description;

    public TrustedKeyDTO() {
    }

    public TrustedKeyDTO(long id, ZonedDateTime createdAt, String description) {
        this.id = id;
        this.createdAt = createdAt;
        this.description = description;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
