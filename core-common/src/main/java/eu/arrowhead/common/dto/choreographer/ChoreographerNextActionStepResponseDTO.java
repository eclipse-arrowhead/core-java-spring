package eu.arrowhead.common.dto.choreographer;

public class ChoreographerNextActionStepResponseDTO {

    private long id;
    private String actionStepName;

    public ChoreographerNextActionStepResponseDTO() {
    }

    public ChoreographerNextActionStepResponseDTO(long id, String actionStepName) {
        this.id = id;
        this.actionStepName = actionStepName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionStepName() {
        return actionStepName;
    }

    public void setActionStepName(String actionStepName) {
        this.actionStepName = actionStepName;
    }
}
