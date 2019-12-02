package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepRequestDTO implements Serializable {

    private String actionStepName;

    private List<String> usedServiceNames;

    private List<String> nextActionStepNames;

    public ChoreographerActionStepRequestDTO() {}

    public ChoreographerActionStepRequestDTO(String actionStepName, List<String> usedServiceNames, List<String> nextActionStepNames) {
        this.actionStepName = actionStepName;
        this.usedServiceNames = usedServiceNames;
        this.nextActionStepNames = nextActionStepNames;
    }

    public String getActionStepName() {
        return actionStepName;
    }

    public void setActionStepName(String actionStepName) {
        this.actionStepName = actionStepName;
    }

    public List<String> getUsedServiceNames() {
        return usedServiceNames;
    }

    public void setUsedServiceNames(List<String> usedServiceNames) {
        this.usedServiceNames = usedServiceNames;
    }

    public List<String> getNextActionStepNames() {
        return nextActionStepNames;
    }

    public void setNextActionStepNames(List<String> nextActionStepNames) {
        this.nextActionStepNames = nextActionStepNames;
    }
}
