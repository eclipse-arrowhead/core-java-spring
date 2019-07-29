package eu.arrowhead.common.dto.choreographer;

import eu.arrowhead.common.database.entity.ChoreographerAction;

import java.util.List;

public class ChoreographerActionPlanResponseDTO {

    private long id;

    private String name;

    private String createdAt;

    private String updatedAt;

    public ChoreographerActionPlanResponseDTO() {}

    public ChoreographerActionPlanResponseDTO(long id, String name, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
