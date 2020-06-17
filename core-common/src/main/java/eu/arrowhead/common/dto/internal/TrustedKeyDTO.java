package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class TrustedKeyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String createdAt;
    private String description;

    public TrustedKeyDTO() {
    }

    public TrustedKeyDTO(long id, String createdAt, String description) {
        this.id = id;
        this.createdAt = createdAt;
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
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
