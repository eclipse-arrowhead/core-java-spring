package eu.arrowhead.common.dto.choreographer;

import java.io.Serializable;

public class ChoreographerNextActionStepResponseDTO implements Serializable {

    private long id;
    private String stepName;

    public ChoreographerNextActionStepResponseDTO() {
    }

    public ChoreographerNextActionStepResponseDTO(long id, String stepName) {
        this.id = id;
        this.stepName = stepName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
}
