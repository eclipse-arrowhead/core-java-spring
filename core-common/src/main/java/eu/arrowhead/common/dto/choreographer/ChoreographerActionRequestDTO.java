package eu.arrowhead.common.dto.choreographer;

import java.util.List;

public class ChoreographerActionRequestDTO {

    private String actionName;

    private String nextActionName;

    private List<ChoreographerActionStepRequestDTO> actions;

    public ChoreographerActionRequestDTO() {
    }

    public ChoreographerActionRequestDTO(String actionName, String nextActionName, List<ChoreographerActionStepRequestDTO> actions) {
        this.actionName = actionName;
        this.nextActionName = nextActionName;
        this.actions = actions;
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

    public List<ChoreographerActionStepRequestDTO> getActions() {
        return actions;
    }

    public void setActions(List<ChoreographerActionStepRequestDTO> actions) {
        this.actions = actions;
    }
}
