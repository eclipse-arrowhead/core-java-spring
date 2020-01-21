package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

public class ChoreographerActionStepRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8100852327039160839L;

	private String actionStepName;
    private List<String> usedServiceNames;
    private List<String> nextActionStepNames;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStepRequestDTO(final String actionStepName, final List<String> usedServiceNames, final List<String> nextActionStepNames) {
        this.actionStepName = actionStepName;
        this.usedServiceNames = usedServiceNames;
        this.nextActionStepNames = nextActionStepNames;
    }

    //-------------------------------------------------------------------------------------------------
	public String getActionStepName() { return actionStepName; }
	public List<String> getUsedServiceNames() { return usedServiceNames; }
	public List<String> getNextActionStepNames() { return nextActionStepNames; }

    //-------------------------------------------------------------------------------------------------
	public void setActionStepName(final String actionStepName) { this.actionStepName = actionStepName; }
    public void setUsedServiceNames(final List<String> usedServiceNames) { this.usedServiceNames = usedServiceNames; }
    public void setNextActionStepNames(final List<String> nextActionStepNames) { this.nextActionStepNames = nextActionStepNames; }
}