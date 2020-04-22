package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

public class ChoreographerPlanResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 6500716813277535148L;

	private long id;
    private String name;
    private String firstActionName;
    private List<ChoreographerActionResponseDTO> actions;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlanResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlanResponseDTO(final long id, final String name, final String firstActionName, final List<ChoreographerActionResponseDTO> actions, final String createdAt, final String updatedAt) {
        this.id = id;
        this.name = name;
        this.firstActionName = firstActionName;
        this.actions = actions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
    public String getFirstActionName() { return firstActionName; }
    public List<ChoreographerActionResponseDTO> getActions() { return actions; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setFirstActionName(String firstActionName) { this.firstActionName = firstActionName; }
    public void setActions(final List<ChoreographerActionResponseDTO> actions) { this.actions = actions; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}