package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionPlanResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 6500716813277535148L;

	private long id;
    private String actionPlanName;
    private List<ChoreographerActionResponseDTO> actions;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlanResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionPlanResponseDTO(final long id, final String actionPlanName, final List<ChoreographerActionResponseDTO> actions, final String createdAt, final String updatedAt) {
        this.id = id;
        this.actionPlanName = actionPlanName;
        this.actions = actions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getActionPlanName() { return actionPlanName; }
	public List<ChoreographerActionResponseDTO> getActions() { return actions; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setActionPlanName(final String actionPlanName) { this.actionPlanName = actionPlanName; }
    public void setActions(final List<ChoreographerActionResponseDTO> actions) { this.actions = actions; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}