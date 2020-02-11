package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 305775285238451287L;

	private String actionName;
    private String nextActionName;
    private List<ChoreographerActionStepRequestDTO> actionSteps;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
    public ChoreographerActionRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionRequestDTO(final String actionName, final String nextActionName, final List<ChoreographerActionStepRequestDTO> actionSteps) {
        this.actionName = actionName;
        this.nextActionName = nextActionName;
        this.actionSteps = actionSteps;
    }

    //-------------------------------------------------------------------------------------------------
	public String getActionName() { return actionName; }
	public String getNextActionName() { return nextActionName; }
	public List<ChoreographerActionStepRequestDTO> getActionSteps() { return actionSteps; }

    //-------------------------------------------------------------------------------------------------
	public void setActionName(final String actionName) { this.actionName = actionName; }
    public void setNextActionName(final String nextActionName) { this.nextActionName = nextActionName; }
    public void setActionSteps(final List<ChoreographerActionStepRequestDTO> actionSteps) { this.actionSteps = actionSteps; }
}