package eu.arrowhead.common.dto.choreographer;

import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepResponseDTO implements Serializable {

    private long id;
    private List<ServiceDefinitionResponseDTO> serviceDefinitions;
    private List<ChoreographerActionStep> nextActionSteps;
    private String createdAt;
    private String updatedAt;

    public ChoreographerActionStepResponseDTO() {}

    public ChoreographerActionStepResponseDTO(long id, List<ServiceDefinitionResponseDTO> serviceDefinitions, List<ChoreographerActionStep> nextActionSteps, String createdAt, String updatedAt) {
        this.id = id;
        this.serviceDefinitions = serviceDefinitions;
        this.nextActionSteps = nextActionSteps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ServiceDefinitionResponseDTO> getServiceDefinitions() {
        return serviceDefinitions;
    }

    public void setServiceDefinitions(List<ServiceDefinitionResponseDTO> serviceDefinitions) {
        this.serviceDefinitions = serviceDefinitions;
    }

    public List<ChoreographerActionStep> getNextActionSteps() {
        return nextActionSteps;
    }

    public void setNextActionSteps(List<ChoreographerActionStep> nextActionSteps) {
        this.nextActionSteps = nextActionSteps;
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
}
