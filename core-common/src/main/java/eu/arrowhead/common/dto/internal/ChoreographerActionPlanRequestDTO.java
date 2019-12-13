package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionPlanRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 7757993756490969411L;

	private String actionPlanName;
    private List<ChoreographerActionRequestDTO> actions;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlanRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlanRequestDTO(final String actionPlanName, final List<ChoreographerActionRequestDTO> actions) {
        this.actionPlanName = actionPlanName;
        this.actions = actions;
    }

    //-------------------------------------------------------------------------------------------------
	public String getActionPlanName() { return actionPlanName; }
	public List<ChoreographerActionRequestDTO> getActions() { return actions; }

    //-------------------------------------------------------------------------------------------------
	public void setActionPlanName(final String actionPlanName) { this.actionPlanName = actionPlanName; }
    public void setActions(final List<ChoreographerActionRequestDTO> actions) { this.actions = actions; }
}