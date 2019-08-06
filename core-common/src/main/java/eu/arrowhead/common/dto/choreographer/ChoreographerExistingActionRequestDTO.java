package eu.arrowhead.common.dto.choreographer;

public class ChoreographerExistingActionRequestDTO {

    private String actionName;

    private String nextActionName;

    public ChoreographerExistingActionRequestDTO() {}

    public ChoreographerExistingActionRequestDTO(String actionName, String nextActionName) {
        this.actionName = actionName;
        this.nextActionName = nextActionName;
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
}
