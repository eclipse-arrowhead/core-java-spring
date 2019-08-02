package eu.arrowhead.common.dto.choreographer;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionResponseDTO implements Serializable {

    private long id;

    private String actionName;

    private String nextActionName;

    private List<ChoreographerActionStepResponseDTO> actionSteps;

    private String createdAt;

    private String updatedAt;

    public ChoreographerActionResponseDTO() {
    }

    public ChoreographerActionResponseDTO(long id, String actionName, String nextActionName, List<ChoreographerActionStepResponseDTO> actionSteps, String createdAt, String updatedAt) {
        this.id = id;
        this.actionName = actionName;
        this.nextActionName = nextActionName;
        this.actionSteps = actionSteps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getNextActionName() {
        return nextActionName;
    }

    public void setNextActionName(String nextActionName) {
        this.nextActionName = nextActionName;
    }

    public List<ChoreographerActionStepResponseDTO> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(List<ChoreographerActionStepResponseDTO> actionSteps) {
        this.actionSteps = actionSteps;
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
