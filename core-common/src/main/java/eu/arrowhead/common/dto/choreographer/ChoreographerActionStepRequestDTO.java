package eu.arrowhead.common.dto.choreographer;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepRequestDTO implements Serializable {

    private String name;

    private List<String> usedServices;

    public ChoreographerActionStepRequestDTO() {}

    public ChoreographerActionStepRequestDTO(String name, List<String> usedServices) {
        this.name = name;
        this.usedServices = usedServices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsedServices() {
        return usedServices;
    }

    public void setUsedServices(List<String> usedServices) {
        this.usedServices = usedServices;
    }
}
