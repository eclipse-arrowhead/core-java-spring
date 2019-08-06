package eu.arrowhead.common.dto.choreographer;

import java.util.List;

public class ChoreographerActionPlanRequestWithExistingActionDTO {

    private String actionPlanName;

    private List<ChoreographerExistingActionRequestDTO> actions;

    public ChoreographerActionPlanRequestWithExistingActionDTO() {}

    public ChoreographerActionPlanRequestWithExistingActionDTO(String actionPlanName, List<ChoreographerExistingActionRequestDTO> actions) {
        this.actionPlanName = actionPlanName;
        this.actions = actions;
    }

    public String getActionPlanName() {
        return actionPlanName;
    }

    public void setActionPlanName(String actionPlanName) {
        this.actionPlanName = actionPlanName;
    }

    public List<ChoreographerExistingActionRequestDTO> getActions() {
        return actions;
    }

    public void setActions(List<ChoreographerExistingActionRequestDTO> actions) {
        this.actions = actions;
    }
}
