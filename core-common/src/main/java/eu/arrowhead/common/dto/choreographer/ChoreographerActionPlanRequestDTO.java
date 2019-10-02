package eu.arrowhead.common.dto.choreographer;

import java.util.List;

public class ChoreographerActionPlanRequestDTO {

    private String actionPlanName;

    private List<ChoreographerActionRequestDTO> actions;

    public ChoreographerActionPlanRequestDTO() {}

    public ChoreographerActionPlanRequestDTO(String actionPlanName, List<ChoreographerActionRequestDTO> actions) {
        this.actionPlanName = actionPlanName;
        this.actions = actions;
    }

    public String getActionPlanName() {
        return actionPlanName;
    }

    public void setActionPlanName(String actionPlanName) {
        this.actionPlanName = actionPlanName;
    }

    public List<ChoreographerActionRequestDTO> getActions() {
        return actions;
    }

    public void setActions(List<ChoreographerActionRequestDTO> actions) {
        this.actions = actions;
    }
}
