package eu.arrowhead.common.dto.internal;

import java.util.List;

public class ChoreographerActionPlanRequestWithExistingActionDTO {

    private String actionPlanName;

    private List<ChoreographerActionRequestDTO> actions;

    public ChoreographerActionPlanRequestWithExistingActionDTO() {}

    public ChoreographerActionPlanRequestWithExistingActionDTO(String actionPlanName, List<ChoreographerActionRequestDTO> actions) {
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
