package eu.arrowhead.common.dto.choreographer;

import java.util.List;

public class ChoreographerActionRequestDTO {

    private String actionName;

    private String nextActionName;

    private List<ChoreographerActionStepRequestDTO> actionSteps;

    public ChoreographerActionRequestDTO() {
    }

    public ChoreographerActionRequestDTO(String actionName, String nextActionName, List<ChoreographerActionStepRequestDTO> actionSteps) {
        this.actionName = actionName;
        this.nextActionName = nextActionName;
        this.actionSteps = actionSteps;
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

    public List<ChoreographerActionStepRequestDTO> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(List<ChoreographerActionStepRequestDTO> actionSteps) {
        this.actionSteps = actionSteps;
    }
}
