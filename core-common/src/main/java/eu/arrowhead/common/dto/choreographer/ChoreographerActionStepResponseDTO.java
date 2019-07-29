package eu.arrowhead.common.dto.choreographer;

import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.database.entity.ServiceDefinition;

import java.util.List;

public class ChoreographerActionStepResponseDTO {

    private long id;

    private String name;

    private String createdAt;

    private String updatedAt;

    private List<ServiceDefinition> usedServices;

    private List<ChoreographerActionStep> nextActionSteps;

    public ChoreographerActionStepResponseDTO() {}

    public ChoreographerActionStepResponseDTO(long id, String name, String createdAt, String updatedAt, List<ServiceDefinition> usedServices, List<ChoreographerActionStep> nextActionSteps) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.usedServices = usedServices;
        this.nextActionSteps = nextActionSteps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ServiceDefinition> getUsedServices() {
        return usedServices;
    }

    public void setUsedServices(List<ServiceDefinition> usedServices) {
        this.usedServices = usedServices;
    }

    public List<ChoreographerActionStep> getNextActionSteps() {
        return nextActionSteps;
    }

    public void setNextActionSteps(List<ChoreographerActionStep> nextActionSteps) {
        this.nextActionSteps = nextActionSteps;
    }
}
