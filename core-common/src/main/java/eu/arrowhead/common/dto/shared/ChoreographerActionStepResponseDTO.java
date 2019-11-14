package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3665162578177568728L;

	private long id;
    private String stepName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ServiceDefinitionResponseDTO> serviceDefinitions;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChoreographerNextActionStepResponseDTO> nextActionSteps;

    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepResponseDTO(final long id, final String stepName, final List<ServiceDefinitionResponseDTO> serviceDefinitions, 
											  final List<ChoreographerNextActionStepResponseDTO> nextActionSteps, final String createdAt, final String updatedAt) {
        this.id = id;
        this.serviceDefinitions = serviceDefinitions;
        this.stepName = stepName;
        this.nextActionSteps = nextActionSteps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getStepName() { return stepName; }
	public List<ServiceDefinitionResponseDTO> getServiceDefinitions() { return serviceDefinitions; }
	public List<ChoreographerNextActionStepResponseDTO> getNextActionSteps() { return nextActionSteps; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setStepName(final String stepName) { this.stepName = stepName; }
    public void setServiceDefinitions(final List<ServiceDefinitionResponseDTO> serviceDefinitions) { this.serviceDefinitions = serviceDefinitions; }
    public void setNextActionSteps(final List<ChoreographerNextActionStepResponseDTO> nextActionSteps) { this.nextActionSteps = nextActionSteps; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}