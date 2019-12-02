package eu.arrowhead.common.dto.shared;



import java.io.Serializable;
import java.util.List;

public class ChoreographerActionPlanResponseDTO implements Serializable {

    private long id;

    private String actionPlanName;

    private List<ChoreographerActionResponseDTO> actions;

    private String createdAt;

    private String updatedAt;

    public ChoreographerActionPlanResponseDTO() {
    }

    public ChoreographerActionPlanResponseDTO(long id, String actionPlanName, List<ChoreographerActionResponseDTO> actions, String createdAt, String updatedAt) {
        this.id = id;
        this.actionPlanName = actionPlanName;
        this.actions = actions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionPlanName() {
        return actionPlanName;
    }

    public void setActionPlanName(String actionPlanName) {
        this.actionPlanName = actionPlanName;
    }

    public List<ChoreographerActionResponseDTO> getActions() {
        return actions;
    }

    public void setActions(List<ChoreographerActionResponseDTO> actions) {
        this.actions = actions;
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

