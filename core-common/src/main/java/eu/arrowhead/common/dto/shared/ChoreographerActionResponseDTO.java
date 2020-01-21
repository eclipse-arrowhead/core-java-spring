package eu.arrowhead.common.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -2097830712150683779L;

	private long id;
    private String actionName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nextActionName;

    private List<ChoreographerActionStepResponseDTO> actionSteps;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionResponseDTO(final long id, final String actionName, final String nextActionName, final List<ChoreographerActionStepResponseDTO> actionSteps, final String createdAt,
										  final String updatedAt) {
        this.id = id;
        this.actionName = actionName;
        this.nextActionName = nextActionName;
        this.actionSteps = actionSteps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getActionName() { return actionName; }
	public String getNextActionName() { return nextActionName; }
	public List<ChoreographerActionStepResponseDTO> getActionSteps() { return actionSteps; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setActionName(final String actionName) { this.actionName = actionName; }
    public void setNextActionName(final String nextActionName) { this.nextActionName = nextActionName; }
    public void setActionSteps(final List<ChoreographerActionStepResponseDTO> actionSteps) { this.actionSteps = actionSteps; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}