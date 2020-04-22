package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 305775285238451287L;

	private String name;
    private String nextActionName;
    private List<String> firstStepNames;
    private List<ChoreographerStepRequestDTO> steps;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
    public ChoreographerActionRequestDTO() {}

    //-------------------------------------------------------------------------------------------------

    public ChoreographerActionRequestDTO(String name, String nextActionName, List<String> firstStepNames, List<ChoreographerStepRequestDTO> steps) {
        this.name = name;
        this.nextActionName = nextActionName;
        this.firstStepNames = firstStepNames;
        this.steps = steps;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public String getNextActionName() { return nextActionName; }
	public List<ChoreographerStepRequestDTO> getSteps() { return steps; }
    public List<String> getFirstStepNames() { return firstStepNames; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setNextActionName(final String nextActionName) { this.nextActionName = nextActionName; }
    public void setSteps(final List<ChoreographerStepRequestDTO> steps) { this.steps = steps; }
    public void setFirstStepNames(List<String> firstStepNames) { this.firstStepNames = firstStepNames; }
}